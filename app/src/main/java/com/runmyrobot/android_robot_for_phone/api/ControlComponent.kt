package com.runmyrobot.android_robot_for_phone.api

import android.content.Context
import com.runmyrobot.android_robot_for_phone.control.CommunicationInterface

/**
 *
 * Created by Brendon on 9/9/2018.
 */
abstract class ControlComponent(val controlOut : CommunicationInterface?, context: Context) : Component(context){

    /**
     * Send message out to device through whatever path is used
     */
    fun sendToDevice(byteArray: ByteArray){
        controlOut?.send(byteArray)
    }
}