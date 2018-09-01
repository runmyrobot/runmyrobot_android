package com.runmyrobot.android_robot_for_phone.myrobot

import android.content.Context
import com.runmyrobot.android_robot_for_phone.api.Component

/**
 * Use this class to inject other Component objects. This will initialize them, but not enable them
 */
object RobotComponentList{
    val components = ArrayList<Component>()
    fun init(context: Context){
        components.add(MotorControl(context))
    }
}