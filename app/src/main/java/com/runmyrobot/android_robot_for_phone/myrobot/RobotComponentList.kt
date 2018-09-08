package com.runmyrobot.android_robot_for_phone.myrobot

import android.content.Context
import com.runmyrobot.android_robot_for_phone.api.Component

/**
 * Use this class to inject other Component objects. This will initialize them, but not enable them
 */
object RobotComponentList{
    val components = ArrayList<Component>()
    fun init(context: Context){
        components.clear()
        components.add(BluetoothControl(context)) //SaberTooth Simplified Serial Motor control through USB
        //components.add(SabertoothTTLMotorControl(context)) //SaberTooth Simplified Serial motor control via GPIO
        //Add other custom components here to be added to the Core
    }
}