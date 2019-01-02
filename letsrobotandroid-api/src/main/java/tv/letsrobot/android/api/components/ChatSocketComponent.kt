package tv.letsrobot.android.api.components

import android.content.Context
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import tv.letsrobot.android.api.components.tts.TTSBaseComponent
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.enums.ComponentType
import tv.letsrobot.android.api.interfaces.Component
import tv.letsrobot.android.api.utils.JsonObjectUtils
import tv.letsrobot.android.api.utils.PhoneBatteryMeter
import tv.letsrobot.android.api.utils.ValueUtil
import java.net.URISyntaxException
import java.util.*

/**
 * Text to speech component class
 */
class ChatSocketComponent internal constructor(context: Context, private val robotId : String) : Component(context){
    override fun getType(): ComponentType {
        return ComponentType.TTS
    }

    private var mSocket: Socket? = null
    val connected: Boolean
        get() = mSocket != null && mSocket!!.connected()

    override fun enableInternal(){
        mSocket = setupSocket(robotId)
        mSocket?.on(Socket.EVENT_CONNECT) {
            onConnect()
        }?.on(Socket.EVENT_CONNECT_ERROR) {
            onConnectError()
        }?.on("chat_message_with_name"){
            onChatMessageWithName(it)
        }?.on(Socket.EVENT_DISCONNECT){
            onDisconnect()
        }
        mSocket?.connect()
    }

    override fun disableInternal(){
        mSocket?.disconnect()
    }

    /**
     * Setup a chat socket using robot id
     */
    private fun setupSocket(robotId : String) : Socket?{
        var host: String? = null
        var port: String? = null
        JsonObjectUtils.getJsonObjectFromUrl(
                String.format(CHAT_URL, robotId)
        )?.let {
            Log.d("CHAT", it.toString())
            host = it.getString("host")
            port = it.getString("port")
        }

        return try {
            val url = String.format(SOCKET_TEMPLATE, host, port)
            IO.socket(url)
        } catch (e: URISyntaxException) {
            status = ComponentStatus.ERROR
            e.printStackTrace()
            null
        }
    }

    private fun onConnect() {
        mSocket!!.emit("identify_robot_id", robotId)
        status = ComponentStatus.STABLE
        sendText(TTSBaseComponent.TTSObject(TTSBaseComponent.TTS_OK
                , TTSBaseComponent.COMMAND_PITCH, shouldFlush = true))
    }

    private fun onConnectError() {
        Log.d("Robot", "Err")
        status = ComponentStatus.ERROR
    }

    private fun onDisconnect() {
        sendText(TTSBaseComponent.TTSObject(TTSBaseComponent.TTS_DISCONNECTED
                , TTSBaseComponent.COMMAND_PITCH, shouldFlush = true))
        if(status != ComponentStatus.DISABLED)
            status = ComponentStatus.INTERMITTENT
    }

    private fun onChatMessageWithName(params: Array<out Any>) {
        if (params[0] is JSONObject) {
            val `object` = params[0] as JSONObject
            try {
                processMessage(`object`.getString("message"), `object`.getString("name"))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun processMessage(messageRaw: String?, user : String?) {
        getMessageFromRaw(messageRaw)?.let {
            var pitch = 1f
            val speakingText : String? = if(isSpeakableText(it)) {
                it
            }
            else{
                pitch = .5f
                processCommand(it, user)
            }
            speakingText?.let {
                sendText(TTSBaseComponent.TTSObject(speakingText, pitch))
            }
        }
    }

    private fun sendText(data : TTSBaseComponent.TTSObject) {
        eventDispatcher?.handleMessage(getType(), EVENT_MAIN, data, this)
    }

    private fun processCommand(it: String, user : String?): String? {
        if(user == MainSocketComponent.owner) {
            return when (it) {
                ".table on" -> {
                    "Table top mode on"
                }
                ".table off" -> {
                    "Table top mode off"
                }
                ".motors off" -> {
                    "Motors turned off"
                }
                ".motors on" -> {
                    "Motors turned on"
                }
                ".battery level" -> {
                    "Internal battery ${PhoneBatteryMeter.getReceiver(context.applicationContext).batteryLevel} percent"
                }
                else -> null
            }
        }
        return null
    }

    private fun getPitchFromUser(name : String): Float {
        val scale = Random(name.hashCode().toLong()).nextFloat()
        return ValueUtil.map(scale,0f, 1f, 0f, 10f, 1.0f)
    }

    companion object {
        const val CHAT_URL = "https://letsrobot.tv/get_chat_host_port/%s"
        const val SOCKET_TEMPLATE = "http://%s:%s"
        fun getMessageFromRaw(inVal : String?) : String?{
            inVal?.let{ //In case there is no message object
                val index = it.indexOf("]").takeIf {
                    //If no spaces or space is last in message, then assume no message
                    it != -1 && it < inVal.length
                } ?: return null
                return it.subSequence(index+1, it.length).trim().toString().takeIf {
                    !it.isBlank() //don't set if just blank
                }?.let { it }
            }
            return null
        }

        fun isSpeakableText(msg : String?) : Boolean{
            return msg?.let {
                !it.startsWith(".") && !it.startsWith(" .")
            } ?: false
        }
    }
}
