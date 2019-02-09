package tv.letsrobot.android.api.components

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
        return ComponentType.CHAT_SOCKET
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
                processMessage(`object`)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun processMessage(jsonObject: JSONObject) {
        getMessageFromRaw(jsonObject.getString("message"))?.let { rawMessage ->
            val user = jsonObject.getString("name")
            var pitch = 1f
            var isCommand = false
            val speakingText : String? = if(isSpeakableText(rawMessage)) {
                rawMessage
            }
            else{
                isCommand = true
                pitch = .5f
                processCommand(rawMessage, user)
            }
            val ttsObject = TTSBaseComponent.TTSObject( rawMessage,
                    pitch,
                    user = user,
                    isSpeakable = !isCommand,
                    isMod = user == MainSocketComponent.owner,
                    color = jsonObject.getString("username_color"),
                    message_id = jsonObject.getString("_id"))
            sendChatEvents(ttsObject, speakingText, isCommand)
        }
    }

    private fun sendChatEvents(ttsObject: TTSBaseComponent.TTSObject, speakingText: String?, isCommand : Boolean) {
        //send the packet via Local Broadcast. Anywhere in this app can intercept this
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(Intent(LR_CHAT_MESSAGE_WITH_NAME_BROADCAST)
                        .also { intent ->
                            intent.putExtra("json", ttsObject)
                        })
        if(isCommand){ //send it once for command
            sendText(ttsObject)
        }
        speakingText?.let { ttsText -> //now send it again for speakable text
            sendText(ttsObject.also { ttsObject ->
                ttsObject.text = ttsText
            })
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
        const val LR_CHAT_MESSAGE_WITH_NAME_BROADCAST = "tv.letsrobot.chat.chat_message_with_name"
        const val LR_CHAT_MESSAGE_REMOVED_BROADCAST = "tv.letsrobot.chat.message_removed"
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
