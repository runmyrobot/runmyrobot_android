package com.runmyrobot.android_robot_for_phone.myrobot

import android.content.Context
import android.util.Log
import com.runmyrobot.android_robot_for_phone.api.Component
import com.runmyrobot.android_robot_for_phone.control.ControllerMessageManager

/**
 * Designed to run on a SaberTooth Serial Motor controller via USB Serial
 * Created by Brendon on 8/26/2018.
 */
class MotorControl(context: Context) : Component(context) {
    val TAG = "MotorControl"
    override fun enable() {
        super.enable()
        Log.d(TAG, "enable")
        ControllerMessageManager.subscribe("F", onForward)
        ControllerMessageManager.subscribe("B", onBack)
        ControllerMessageManager.subscribe("L", onLeft)
        ControllerMessageManager.subscribe("R", onRight)
        ControllerMessageManager.subscribe("stop", onStop)
    }

    override fun disable() {
        super.disable()
        Log.d(TAG, "disable")
        ControllerMessageManager.unsubscribe("F", onForward)
        ControllerMessageManager.unsubscribe("B", onBack)
        ControllerMessageManager.unsubscribe("L", onLeft)
        ControllerMessageManager.unsubscribe("R", onRight)
        ControllerMessageManager.unsubscribe("stop", onStop)
    }

    private val onForward: (Any?) -> Unit = {
        Log.d(TAG, "onForward")
    }

    private val onBack: (Any?) -> Unit = {
        Log.d(TAG, "onBack")
    }

    private val onLeft: (Any?) -> Unit = {
        Log.d(TAG, "onLeft")
    }

    private val onRight: (Any?) -> Unit = {
        Log.d(TAG, "onRight")
    }

    private val onStop : (Any?) -> Unit  = {
        Log.d(TAG, "onStop")
    }

    override fun timeout() {
        onStop(null)
    }
}
