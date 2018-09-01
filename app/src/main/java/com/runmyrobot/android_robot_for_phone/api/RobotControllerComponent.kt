package com.runmyrobot.android_robot_for_phone.api

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.runmyrobot.android_robot_for_phone.control.ControllerMessageManager
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level

/**
 * Handles robot control Socket IO messages and broadcasts them through ControllerMessageManager
 *
 * Also grabs chat messages for TTS and sends it to ControllerMessageManager
 * Created by Brendon on 8/25/2018.
 */
class RobotControllerComponent internal constructor(private val robotId: String) : Emitter.Listener {
    var running = AtomicBoolean(false)
    private var mSocket: Socket? = null
    internal var handler: Handler

    internal var runnable: Runnable = Runnable { ControllerMessageManager.invoke("messageTimeout", null) }

    val connected: Boolean
        get() = mSocket != null && mSocket!!.connected()

    init {
        java.util.logging.Logger.getLogger(IO::class.java.name).level = Level.FINEST
        try {
            Looper.prepare() //Known to throw if already initialized. No way other than this to do it
        } catch (ignored: Exception) {
        }

        handler = Handler(Looper.myLooper())
    }

    fun enable() {
        if (running.getAndSet(true)) {
            return
        }

        var host: String? = null
        var port: String? = null
        val client = OkHttpClient()
        val call = client.newCall(Request.Builder().url(String.format("https://letsrobot.tv/get_control_host_port/%s", robotId)).build())
        try {
            val response = call.execute()
            if (response.body() != null) {
                val `object` = JSONObject(response.body()!!.string())
                Log.d("ROBOT", `object`.toString())
                host = `object`.getString("host")
                port = `object`.getString("port")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        try {
            val url = String.format("http://%s:%s", host, port)
            mSocket = IO.socket(url)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
        mSocket!!.on(Socket.EVENT_CONNECT) {
            mSocket!!.emit("identify_robot_id", robotId)
            ControllerMessageManager.invoke(ROBOT_CONNECTED, null)
        }.on(Socket.EVENT_CONNECT_ERROR) { Log.d("Robot", "Err") }.on(Socket.EVENT_DISCONNECT) {
            ControllerMessageManager.invoke(ROBOT_DISCONNECTED, null)
            ControllerMessageManager.invoke("stop", null)
        }.on("command_to_robot") { args ->
            if (args != null && args[0] is JSONObject) {
                val `object` = args[0] as JSONObject
                Log.d("Log", `object`.toString())
                resetTimer() //TODO validate that this actually works
                try {
                    //broadcast what message was sent ex. F, stop, etc
                    ControllerMessageManager.invoke(`object`.getString("command"), null)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                //{"command":"F","robot_id":"55555555","user":{"username":"user","anonymous":false},"key_position":"down"}
                //{"command":"stop","robot_id":"55555555","user":{"username":"user","anonymous":false},"key_position":"up"}
            }
        }
        /**
         * controlSocketIO.on('command_to_robot', onHandleCommand)
         * controlSocketIO.on('disconnect', onHandleControlDisconnect)
         *
         * appServerSocketIO.on('exclusive_control', onHandleExclusiveControl)
         * appServerSocketIO.on('connect', onHandleAppServerConnect)
         * appServerSocketIO.on('reconnect', onHandleAppServerReconnect)
         * appServerSocketIO.on('disconnect', onHandleAppServerDisconnect)
         *
         * if commandArgs.tts_delay_enabled:
         * userSocket.on('message_removed', onHandleChatMessageRemoved)
         *
         * if commandArgs.enable_chat_server_connection:
         * chatSocket.on('chat_message_with_name', onHandleChatMessage)
         * chatSocket.on('connect', onHandleChatConnect)
         * chatSocket.on('reconnect', onHandleChatReconnect)
         * chatSocket.on('disconnect', onHandleChatDisconnect)
         */
        mSocket!!.connect()
        //TODO
    }

    private fun resetTimer() {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 200)
    }

    fun disable() {
        if (!running.getAndSet(false)) {
            return
        }
        mSocket!!.disconnect()
        //TODO
    }

    override fun call(vararg args: Any) {
        Log.d("Controller", Arrays.toString(args))
    }

    companion object {
        val ROBOT_DISCONNECTED = "robot_disconnect"
        val ROBOT_CONNECTED = "robot_connect"
    }
}
