package com.runmyrobot.android_robot_for_phone.control.deviceProtocols

import android.content.Context
import android.util.Log
import com.runmyrobot.android_robot_for_phone.api.ControlComponent
import com.runmyrobot.android_robot_for_phone.control.EventManager
import com.runmyrobot.android_robot_for_phone.control.EventManager.Companion.STOP_EVENT
import com.runmyrobot.android_robot_for_phone.utils.SabertoothDriverUtil

/**
 * Created by Brendon on 9/5/2018.
 */

class BluetoothControl(context: Context) : ControlComponent(context) {
    val TAG = "BluetoothControl"
    private val motorForwardSpeed = 50.toByte()
    private val motorBackwardSpeed = (-50).toByte()

    override fun enable() {
        super.enable()
        EventManager.subscribe(EventManager.COMMAND, onCommand)
        EventManager.subscribe(STOP_EVENT, onStop)
    }

    override fun disable() {
        super.disable()
        EventManager.unsubscribe(EventManager.COMMAND, onCommand)
        EventManager.unsubscribe(STOP_EVENT, onStop)
    }

    private val onCommand: (Any?) -> Unit = {
        it?.takeIf { it is String }?.let{
            when(it as String){
                "F" -> {onForward()}
                "B" -> {onBack()}
                "L" -> {onLeft()}
                "R" -> {onRight()}
            }
        }
    }

    private fun onForward() {
        Log.d(TAG, "onForward")
        val data = ByteArray(2)
        data[0] = SabertoothDriverUtil.getDriveSpeed(motorForwardSpeed, 0)
        data[1] = SabertoothDriverUtil.getDriveSpeed(motorForwardSpeed, 1)
        sendToDevice(data)
    }

    private fun onBack() {
        Log.d(TAG, "onBack")
        val data = ByteArray(2)
        data[0] = SabertoothDriverUtil.getDriveSpeed(motorBackwardSpeed, 0)
        data[1] = SabertoothDriverUtil.getDriveSpeed(motorBackwardSpeed, 1)
        sendToDevice(data)
    }

    private fun onLeft() {
        Log.d(TAG, "onLeft")
        val data = ByteArray(2)
        data[0] = SabertoothDriverUtil.getDriveSpeed(motorForwardSpeed, 0)
        data[1] = SabertoothDriverUtil.getDriveSpeed(motorBackwardSpeed, 1)
        sendToDevice(data)
    }

    private fun onRight() {
        Log.d(TAG, "onRight")
        val data = ByteArray(2)
        data[0] = SabertoothDriverUtil.getDriveSpeed(motorBackwardSpeed, 0)
        data[1] = SabertoothDriverUtil.getDriveSpeed(motorForwardSpeed, 1)
        sendToDevice(data)
    }

    private val onStop : (Any?) -> Unit  = {
        Log.d(TAG, "onStop")
        val data = ByteArray(1)
        data[0] = 0x00
        sendToDevice(data)
    }

    /**
     * Timeout function. Will be called if we have not received anything recently,
     * in case a movement command gets stuck
     */
    override fun timeout() {
        onStop(null)
    }
}