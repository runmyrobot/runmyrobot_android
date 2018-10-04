package tv.letsrobot.android.api.api

import android.content.Context
import android.util.Log
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.EventManager.Companion.ROBOT_BYTE_ARRAY

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