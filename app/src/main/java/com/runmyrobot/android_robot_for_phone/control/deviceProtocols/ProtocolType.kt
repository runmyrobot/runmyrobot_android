package com.runmyrobot.android_robot_for_phone.control.deviceProtocols

import android.content.Context
import com.runmyrobot.android_robot_for_phone.api.ControlComponent

/**
 * Protocol types will reside in here
 */
enum class ProtocolType {
    ArduinoRaw,
    SabertoothSimplifiedSerial;

    fun getInstantiatedClass(context: Context) : ControlComponent{
        return when(this){
            ArduinoRaw -> ArduinoSendBytesProtocol(context)
            SabertoothSimplifiedSerial -> SabertoothMotorProtocol(context)
        }
    }
}