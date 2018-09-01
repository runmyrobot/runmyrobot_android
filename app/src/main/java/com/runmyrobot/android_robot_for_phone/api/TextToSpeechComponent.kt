package com.runmyrobot.android_robot_for_phone.api

import android.content.Context

/**
 * Created by Brendon on 8/25/2018.
 */
class TextToSpeechComponent internal constructor(context: Context)
/** TODO
 *
 * ttobj = TextToSpeech(context, //TODO update to newer method
 * TextToSpeech.OnInitListener { })
 * ttobj.language = Locale.US
 * .on("chat_message_with_name") { args ->
 * //TODO relocate to TextToSpeechComponent.java
 * Log.d("Log", "chat_message_with_name")
 * if (args != null && args[0] is JSONObject) {
 * val `object` = args[0] as JSONObject
 * Log.d("Log", `object`.toString())
 * ControllerMessageManager.invoke("chat", `object`)
 * try {
 * val split = `object`.getString("message").split("]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
 * ttobj.speak(split[split.size - 1], TextToSpeech.QUEUE_FLUSH, null)
 * } catch (e: JSONException) {
 * e.printStackTrace()
 * }
 *
 * }
 * }
 */
{

    fun enable() {
        //TODO enable camera code
    }

    fun disable() {

    }
}
