package com.runmyrobot.android_robot_for_phone.api

import android.content.Context
import com.runmyrobot.android_robot_for_phone.control.EventManager
import com.runmyrobot.android_robot_for_phone.control.EventManager.Companion.ROBOT_BYTE_ARRAY

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
            EventManager.invoke(ROBOT_BYTE_ARRAY, byteArray)
        }
    }
}