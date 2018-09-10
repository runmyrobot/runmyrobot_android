package com.runmyrobot.android_robot_for_phone.control.deviceProtocols

import android.content.Context
import com.runmyrobot.android_robot_for_phone.api.ControlComponent
import com.runmyrobot.android_robot_for_phone.control.CommunicationInterface

/**
 * Protocol types will reside in here
 */
enum class ProtocolType {
    ArduinoRaw,
    SabertoothSimplifiedSerial;

    fun setup(commClass: CommunicationInterface?, context: Context): ControlComponent {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}