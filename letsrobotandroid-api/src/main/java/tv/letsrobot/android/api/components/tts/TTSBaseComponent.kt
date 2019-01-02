package tv.letsrobot.android.api.components.tts

import android.content.Context
import tv.letsrobot.android.api.interfaces.Component


/**
 * Main component for TTS that all text to speech components should extend if possible
 */
abstract class TTSBaseComponent(context: Context) : Component(context) {
    data class TTSObject(val text: String,
                         val user: String? = null,
                         val pitch : Float = 1.0f,
                         val shouldFlush : Boolean = false,
                         val isSpeakable: Boolean = true)
    companion object {
        var TTS_OK = "ok"
        var TTS_DISCONNECTED = "disconnected"
        const val COMMAND_PITCH = 0.5f
        const val SPEAKABLE_TEXT = 0
        const val COMMAND_TEXT = 1
    }
}