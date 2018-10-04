package tv.letsrobot.android.api.api

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.EventManager.Companion.COMMAND
import tv.letsrobot.android.api.EventManager.Companion.ROBOT_CONNECTED
import tv.letsrobot.android.api.EventManager.Companion.ROBOT_DISCONNECTED
import tv.letsrobot.android.api.EventManager.Companion.STOP_EVENT
import tv.letsrobot.android.api.enums.ComponentStatus
import java.io.IOException
import java.net.URISyntaxException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Handles robot control Socket IO messages and broadcasts them through EventManager
 *
 * Also grabs chat messages for TTS and sends it to EventManager
 */
class RobotControllerComponent internal constructor(context : Context, private val robotId: String) : Component(context){
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

    private var table = false

    private var allowControl = true

    override fun enable() : Boolean{
        if(!super.enable()){
            return false
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
        EventManager.subscribe(EventManager.CHAT){
            print(it as String)
            if(it as? String == ".table on"){
                table = true
            }
            else if(it as? String == ".table off"){
                table = false
            }
            if(it as? String == ".motors off"){
                allowControl = false
            }
            if(it as? String == ".motors on"){
                allowControl = true
            }
        }
        mSocket?.let { socket ->
            socket.on(Socket.EVENT_CONNECT) {
                mSocket?.emit("identify_robot_id", robotId)
                EventManager.invoke(ROBOT_CONNECTED, null)
                status = ComponentStatus.STABLE
            }.on(Socket.EVENT_CONNECT_ERROR) {
                Log.d("Robot", "Err")
                status = ComponentStatus.ERROR
            }.on(Socket.EVENT_DISCONNECT) {
                EventManager.invoke(ROBOT_DISCONNECTED, null)
                EventManager.invoke(STOP_EVENT, null)
                if(status != ComponentStatus.DISABLED)
                    status = ComponentStatus.INTERMITTENT
            }.on("command_to_robot") { args ->
                if (args != null && args[0] is JSONObject) {
                    val `object` = args[0] as JSONObject
                    try {
                        //broadcast what message was sent ex. F, stop, etc
                        val command = `object`.getString("command")
                        if(!allowControl){
                            print("Trashing movement. Controls disabled")
                            return@on
                        }
                        if(table){ //check for table top mode
                            when(command.toLowerCase()){
                                "f" -> {
                                    print("f, Trashing movement. On Table")
                                    return@on
                                }
                                "b" -> {
                                    print("b, Trashing movement. On Table")
                                    return@on
                                }
                                else -> {}
                            }
                        }
                        resetTimer() //resets a timer to prevent a timeout message
                        EventManager.invoke(COMMAND, command)
                    } catch (e: JSONException) {
                        e.printStackTrace() //Message format must be wrong, ignore it
                    }
                }
            }
            socket.connect()
        }
        return true
    }

    /**
     * Reset timer by clearing handler of runnable, and posting another that is delayed.
     * When the timer runnable is triggered, a timeout message will be sent to whatever is listening
     */
    private fun resetTimer() {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 200)
    }

    override fun disable() : Boolean {
        if(!super.disable()) return false
        mSocket?.disconnect()
        return true
    }
}
