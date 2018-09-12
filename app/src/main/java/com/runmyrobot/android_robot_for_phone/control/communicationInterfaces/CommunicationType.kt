package com.runmyrobot.android_robot_for_phone.control.communicationInterfaces

import com.runmyrobot.android_robot_for_phone.control.CommunicationInterface

/**
 * Communication types will reside in here
 */
enum class CommunicationType {
    BluetoothClassic,
    UsbSerial;

    val getInstantiatedClass : CommunicationInterface?
        get() = when(this){
            CommunicationType.BluetoothClassic -> BluetoothControlComponent()
            CommunicationType.UsbSerial -> null
        }
}