package com.runmyrobot.android_robot_for_phone.control.deviceProtocols

import android.content.Context
import android.util.Log
import com.runmyrobot.android_robot_for_phone.api.ControlComponent
import com.runmyrobot.android_robot_for_phone.control.EventManager
import com.runmyrobot.android_robot_for_phone.control.EventManager.Companion.STOP_EVENT
import com.runmyrobot.android_robot_for_phone.utils.SabertoothDriverUtil

/**
 * Uses the SaberTooth motor controller simplified serial protocol
 *
 * Snippet from a SaberTooth Motor Controller Manual
 *
 * Sending a character between 1 and 127 will
 * control motor 1. 1 is full reverse, 64 is stop and 127 is full forward. Sending a character between
 * 128 and 255 will control motor 2. 128 is full reverse, 192 is stop and 255 is full forward.
 * Character 0 (hex 0x00) is a special case. Sending this character will shut down both motors.
 *
 * Currently hardcoded for a certain speed, but can be changed
 */

class SingleByteProtocol(context: Context) : ControlComponent(context) {
    val TAG = "SingleByteProtocol"
    private val motorForwardSpeed = 127.toByte()
    private val motorBackwardSpeed = (-127).toByte()

    private val motorForwardTurnSpeed = 50.toByte()
    private val motorBackwardTurnSpeed = (-50).toByte()

    override fun enable() : Boolean {
        if(!super.enable()) return false
        EventManager.subscribe(EventManager.COMMAND, onCommand)
        EventManager.subscribe(STOP_EVENT, onStop)
        return true
    }

    override fun disable() : Boolean {
        if(!super.disable()) return false
        EventManager.unsubscribe(EventManager.COMMAND, onCommand)
        EventManager.unsubscribe(STOP_EVENT, onStop)
        return true
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
        data[0] = SabertoothDriverUtil.getDriveSpeed(motorBackwardTurnSpeed, 0)
        data[1] = SabertoothDriverUtil.getDriveSpeed(motorForwardTurnSpeed, 1)
        sendToDevice(data)
    }

    private fun onRight() {
        Log.d(TAG, "onRight")
        val data = ByteArray(2)
        data[0] = SabertoothDriverUtil.getDriveSpeed(motorForwardTurnSpeed, 0)
        data[1] = SabertoothDriverUtil.getDriveSpeed(motorBackwardTurnSpeed, 1)
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