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

    override fun enable() {
        super.enable()
        communicationInterface.enable()
    }

    override fun disable() {
        super.disable()
        communicationInterface.disable()
    }
}
