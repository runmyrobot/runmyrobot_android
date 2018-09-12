package com.runmyrobot.android_robot_for_phone.api

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.runmyrobot.android_robot_for_phone.control.EventManager
import com.runmyrobot.android_robot_for_phone.control.EventManager.Companion.COMMAND
import com.runmyrobot.android_robot_for_phone.control.EventManager.Companion.ROBOT_CONNECTED
import com.runmyrobot.android_robot_for_phone.control.EventManager.Companion.ROBOT_DISCONNECTED
import com.runmyrobot.android_robot_for_phone.control.EventManager.Companion.STOP_EVENT
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URISyntaxException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Handles robot control Socket IO messages and broadcasts them through EventManager
 *
 * Also grabs chat messages for TTS and sends it to EventManager
 */
class RobotControllerComponent internal constructor(private val robotId: String){
    var running = AtomicBoolean(false)
    private var mSocket: Socket? = null
    private var handler: Handler

    /**
     * Sends a timeout message via EventManager when run
     */
    private var runnable: Runnable = Runnable { EventManager.invoke(EventManager.TIMEOUT, null) }

    val connected: Boolean
        get() = mSocket != null && mSocket!!.connected()

    init {
        try {
            Looper.prepare() //Setup our looper
        } catch (ignored: Exception) {
            //catch exception if looper is already setup
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

        mSocket?.let { socket ->
            socket.on(Socket.EVENT_CONNECT) {
                mSocket?.emit("identify_robot_id", robotId)
                EventManager.invoke(ROBOT_CONNECTED, null)
            }.on(Socket.EVENT_CONNECT_ERROR) { Log.d("Robot", "Err") }.on(Socket.EVENT_DISCONNECT) {
                EventManager.invoke(ROBOT_DISCONNECTED, null)
                EventManager.invoke(STOP_EVENT, null)
            }.on("command_to_robot") { args ->
                if (args != null && args[0] is JSONObject) {
                    val `object` = args[0] as JSONObject
                    resetTimer() //resets a timer to prevent a timeout message
                    try {
                        //broadcast what message was sent ex. F, stop, etc
                        EventManager.invoke(COMMAND, `object`.getString("command"))
                    } catch (e: JSONException) {
                        e.printStackTrace() //Message format must be wrong, ignore it
                    }
                }
            }
            socket.connect()
        }
    }

    /**
     * Reset timer by clearing handler of runnable, and posting another that is delayed.
     * When the timer runnable is triggered, a timeout message will be sent to whatever is listening
     */
    private fun resetTimer() {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 200)
    }

    fun disable() {
        if (!running.getAndSet(false)) {
            return
        }
        mSocket?.disconnect()
    }
}
