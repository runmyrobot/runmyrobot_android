package com.runmyrobot.android_robot_for_phone.control.communicationInterfaces

import android.content.Context
import com.runmyrobot.android_robot_for_phone.api.CommunicationInterface
import com.runmyrobot.android_robot_for_phone.api.Component

/**
 * Created by Brendon on 9/11/2018.
 */
class CommunicationComponent(context: Context, val communicationInterface: CommunicationInterface) : Component(context) {
    init {
        communicationInterface.initConnection(context)
    }

    override fun enable() : Boolean {
        if(!super.enable()) return false
        communicationInterface.enable()
        return true
    }

    override fun disable() : Boolean{
        if(!super.disable()) return false
        communicationInterface.disable()
        return true
    }
}
