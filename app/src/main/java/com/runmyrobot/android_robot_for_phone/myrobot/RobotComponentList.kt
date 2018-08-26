package com.runmyrobot.android_robot_for_phone.myrobot

import android.content.Context
import com.runmyrobot.android_robot_for_phone.api.Component

/**
 * Created by Brendon on 8/26/2018.
 */
object RobotComponentList{
    val components = ArrayList<Component>()
    fun init(context: Context){
        components.add(MotorControl(context))
    }
}