package tv.letsrobot.android.api.components

import android.content.Context
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.EventManager.Companion.COMMAND
import tv.letsrobot.android.api.EventManager.Companion.ROBOT_CONNECTED
import tv.letsrobot.android.api.EventManager.Companion.ROBOT_DISCONNECTED
import tv.letsrobot.android.api.EventManager.Companion.STOP_EVENT
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.interfaces.Component
import tv.letsrobot.android.api.utils.JsonObjectUtils
import java.net.URISyntaxException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Handles robot control Socket IO messages and broadcasts them through EventManager
 *
 * Also grabs chat messages for TTS and sends it to EventManager
 */
class ControlSocketComponent internal constructor(context : Context, private val robotId: String) : Component(context){
    override fun getType(): Int {
        return Component.CONTROL_SOCKET
    }

    var running = AtomicBoolean(false)
    private var mSocket: Socket? = null

    /**
     * Sends a timeout message via EventManager when run
     */
    private var runnable: Runnable = Runnable { EventManager.invoke(EventManager.TIMEOUT, null) }

    val connected: Boolean
        get() = mSocket != null && mSocket!!.connected()

    private var table = false

    private var allowControl = true

    override fun enableInternal(){
        var host: String? = null
        var port: String? = null
        JsonObjectUtils.getJsonObjectFromUrl(
                String.format("https://letsrobot.tv/get_control_host_port/%s?version=2", robotId)
        )?.let {
            host = it.getString("host")
            port = it.getString("port")
        }

        try {
            mSocket = IO.socket(
                String.format("http://%s:%s", host, port)
            )
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
        EventManager.subscribe(EventManager.CHAT){
            (it as? String)?.let {message ->
                print(message)
                when(message){
                    TABLE_ON_COMMAND -> table = true
                    TABLE_OFF_COMMAND -> table = false
                    MOTORS_OFF_COMMAND -> allowControl = false
                    MOTORS_ON_COMMAND -> allowControl = true
                }
            }
        }
        setupSocketEvents()
        mSocket?.connect()
    }

    private fun setupSocketEvents() {
        mSocket?.let { socket ->
            socket.on(Socket.EVENT_CONNECT) {
                onRobotConnected()
            }.on(Socket.EVENT_RECONNECT) {
                onRobotConnected()
            }.on(Socket.EVENT_CONNECT_ERROR) {
                Log.d("Robot", "Err")
                status = ComponentStatus.ERROR
                reset()
            }.on(Socket.EVENT_DISCONNECT) {
                EventManager.invoke(ROBOT_DISCONNECTED, null)
                EventManager.invoke(STOP_EVENT, null)
                if(status != ComponentStatus.DISABLED)
                    status = ComponentStatus.INTERMITTENT
            }.on("command_to_robot") { args ->
                if (args != null && args[0] is JSONObject) {
                    val `object` = args[0] as JSONObject
                    parseCommand(`object`)?.let{
                        resetTimer() //resets a timer to prevent a timeout message
                        EventManager.invoke(COMMAND, it)
                    }
                }
            }
        }
    }

    private fun parseCommand(jsonObject: JSONObject): String? {
        return try {
            //broadcast what message was sent ex. F, stop, etc
            val command = jsonObject.getString("command")
            if(!allowControl){ //TODO Allow non-movement commands pass
                print("Trashing movement. Controls disabled")
                return null
            }
            if(table){ //check for table top mode
                when(command.toLowerCase()){
                    "f" -> {
                        print("f, Trashing movement. On Table")
                        return null
                    }
                    "b" -> {
                        print("b, Trashing movement. On Table")
                        return null
                    }
                }
            }
            command
        } catch (e: JSONException) {
            e.printStackTrace() //Message format must be wrong, ignore it
            null
        }
    }

    private fun onRobotConnected() {
        mSocket?.emit("robot_id", robotId)
        EventManager.invoke(ROBOT_CONNECTED, null)
        status = ComponentStatus.STABLE
    }

    /**
     * Reset timer by clearing handler of runnable, and posting another that is delayed.
     * When the timer runnable is triggered, a timeout message will be sent to whatever is listening
     */
    private fun resetTimer() {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 200)
    }

    override fun disableInternal(){
        mSocket?.disconnect()
    }

    companion object {
        const val TABLE_ON_COMMAND = ".table on"
        const val TABLE_OFF_COMMAND = ".table off"
        const val MOTORS_ON_COMMAND = ".motors on"
        const val MOTORS_OFF_COMMAND = ".motors off"
    }
}
