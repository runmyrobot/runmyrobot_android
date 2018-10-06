package tv.letsrobot.android.api.components

import android.content.Context
import android.util.Log
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.EventManager.Companion.ROBOT_BYTE_ARRAY
import tv.letsrobot.android.api.interfaces.Component

/**
 *
 * Created by Brendon on 9/9/2018.
 */
abstract class ControlComponent(context: Context) : Component(context){

    /**
     * Send message out to device though our event manager.
     */
    fun sendToDevice(byteArray: ByteArray?){
        byteArray?.let {
            Log.d("ControlComponent","sendToDevice")
            EventManager.invoke(ROBOT_BYTE_ARRAY, byteArray)
        }
    }
}