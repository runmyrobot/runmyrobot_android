package tv.letsrobot.android.api.components.tts

import android.content.Context
import tv.letsrobot.android.api.interfaces.Component
import java.io.Serializable
import java.util.*


/**
 * Main component for TTS that all text to speech components should extend if possible
 */
abstract class TTSBaseComponent(context: Context) : Component(context) {
    data class TTSObject(var text: String,
                         var pitch: Float = 1.0f,
                         var user: String? = null,
                         var anonymous : Boolean = false,
                         var shouldFlush: Boolean = false,
                         var isSpeakable: Boolean = true,
                         var isMod : Boolean = false,
                         var color : String = "#111111",
                         val message_id : String = UUID.randomUUID().toString()) : Serializable{
        companion object {
            const val serialversionUID = 684646874641L
        }
    }
    companion object {
        var TTS_OK = "ok"
        var TTS_DISCONNECTED = "disconnected"
        const val COMMAND_PITCH = 0.5f
    }
}