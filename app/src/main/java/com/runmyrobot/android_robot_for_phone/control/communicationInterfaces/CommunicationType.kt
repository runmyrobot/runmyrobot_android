package com.runmyrobot.android_robot_for_phone.control.communicationInterfaces

import android.content.Context
import com.runmyrobot.android_robot_for_phone.control.CommunicationInterface

/**
 * Communication types will reside in here
 */
enum class CommunicationType {
    BLUETOOTH_CLASSIC,
    USB_SERVICE;

    fun setup(context: Context): CommunicationInterface? {
        return when(this){
            BLUETOOTH_CLASSIC -> {
                null //TODO
            }
            USB_SERVICE -> {
                null //TODO
            }
            else -> {
                null //TODO
            }
        }
    }
}