package tv.letsrobot.android.api.components

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import tv.letsrobot.android.api.Core
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.EventManager.Companion.CHAT
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.interfaces.Component
import tv.letsrobot.android.api.utils.PhoneBatteryMeter
import tv.letsrobot.android.api.utils.ValueUtil
import java.io.IOException
import java.net.URISyntaxException
import java.util.*

/**
 * Text to speech component class
 *
 * TODO different voice or voice options?
 */
class TextToSpeechComponent internal constructor(context: Context, private val robotId : String) : Component(context){
    private var ttobj: TextToSpeech = TextToSpeech(context, TextToSpeech.OnInitListener {})
    private var mSocket: Socket? = null
    val connected: Boolean
        get() = mSocket != null && mSocket!!.connected()

    override fun enable() : Boolean{
        if(!super.enable()){
            return false
        }
        ttobj.language = Locale.US
        var host: String? = null
        var port: String? = null
        val client = OkHttpClient()
        val call = client.newCall(Request.Builder().url(String.format("https://letsrobot.tv/get_chat_host_port/%s", robotId)).build())
        try {
            val response = call.execute()
            if (response.body() != null) {
                val `object` = JSONObject(response.body()!!.string())
                Log.d("CHAT", `object`.toString())
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
                    val messageRaw = `object`.getString("message")
                    getMessageFromRaw(messageRaw)?.let {
                        //TODO use non-deprecated call? Does not support 4.4 though
                        if(isSpeakableText(it) && !ttobj.isSpeaking) {
                            val pitch = 1f
                            ttobj.setPitch(pitch)
                            EventManager.invoke(CHAT, it)
                            ttobj.speak(it, TextToSpeech.QUEUE_FLUSH, null)
                        }
                        else{
                            if(`object`["name"] == Core.owner){
                                val pitch = .5f
                                ttobj.setPitch(pitch)
                                when(it){
                                    ".table on" -> {
                                        ttobj.speak("Table top mode on", TextToSpeech.QUEUE_FLUSH, null)
                                    }
                                    ".table off" -> {
                                        ttobj.speak("Table top mode off", TextToSpeech.QUEUE_FLUSH, null)
                                    }
                                    ".motors off" -> {
                                        ttobj.speak("Motors turned off", TextToSpeech.QUEUE_FLUSH, null)
                                    }
                                    ".motors on" -> {
                                        ttobj.speak("Motors turned on", TextToSpeech.QUEUE_FLUSH, null)
                                    }
                                    ".battery level" ->{
                                        ttobj.speak("Internal battery ${PhoneBatteryMeter.getReceiver(context.applicationContext).batteryLevel} percent", TextToSpeech.QUEUE_FLUSH, null)
                                    }
                                }
                                EventManager.invoke(CHAT, it)
                            }
                            1
                        }
                    }
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
        return true
    }

    private fun getPitchFromUser(name : String): Float {
        val scale = Random(name.hashCode().toLong()).nextFloat()
        return ValueUtil.map(scale,0f, 1f, 0f, 10f, 1.0f)
    }

    override fun disable() : Boolean {
        if(!super.disable()) return false
        mSocket?.disconnect()
        return true
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
