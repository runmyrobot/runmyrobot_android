package tv.letsrobot.android.api.components

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.EventManager.Companion.CHAT
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
 *
 * TODO different voice or voice options?
 *
 * TODO split into TTSComponent and ChatSocketComponent
 */
class TextToSpeechComponent internal constructor(context: Context, private val robotId : String) : Component(context){
    override fun getType(): ComponentType {
        return ComponentType.TTS
    }

    private var ttobj: TextToSpeech = TextToSpeech(context, TextToSpeech.OnInitListener {})
    private var mSocket: Socket? = null
    val connected: Boolean
        get() = mSocket != null && mSocket!!.connected()

    override fun enableInternal(){
        ttobj.language = Locale.US
        var host: String? = null
        var port: String? = null
        JsonObjectUtils.getJsonObjectFromUrl(
                String.format("https://letsrobot.tv/get_chat_host_port/%s", robotId)
        )?.let {
            Log.d("CHAT", it.toString())
            host = it.getString("host")
            port = it.getString("port")
        }

        try {
            val url = String.format("http://%s:%s", host, port)
            mSocket = IO.socket(url)
        } catch (e: URISyntaxException) {
            status = ComponentStatus.ERROR
            e.printStackTrace()
        }
        mSocket?.on(Socket.EVENT_CONNECT) {
            mSocket!!.emit("identify_robot_id", robotId)
            status = ComponentStatus.STABLE
            ttobj.setPitch(.5f)
            ttobj.speak("OK", TextToSpeech.QUEUE_FLUSH, null)
        }?.on(Socket.EVENT_CONNECT_ERROR) {
            Log.d("Robot", "Err")
            status = ComponentStatus.ERROR
        }?.on("chat_message_with_name"){
            if (it[0] is JSONObject) {
                val `object` = it[0] as JSONObject
                try {
                    processMessage(`object`.getString("message"), `object`.getString("name"))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }?.on(Socket.EVENT_DISCONNECT){
            val pitch = .5f
            ttobj.setPitch(pitch)
            ttobj.speak("Disconnected", TextToSpeech.QUEUE_FLUSH, null)
            if(status != ComponentStatus.DISABLED)
                status = ComponentStatus.INTERMITTENT
        }
        mSocket?.connect()
    }

    private fun processMessage(messageRaw: String?, user : String?) {
        getMessageFromRaw(messageRaw)?.let {
            var pitch = 1f
            val speakingText : String? = if(isSpeakableText(it) && !ttobj.isSpeaking) {
                it
            }
            else{
                pitch = .5f
                processCommand(it, user)
            }
            ttobj.setPitch(pitch)
            EventManager.invoke(CHAT, it)
            speakingText?.let{t ->
                ttobj.speak(t, TextToSpeech.QUEUE_FLUSH, null)
            }
        }
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

    override fun disableInternal(){
        mSocket?.disconnect()
    }

    companion object {
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
