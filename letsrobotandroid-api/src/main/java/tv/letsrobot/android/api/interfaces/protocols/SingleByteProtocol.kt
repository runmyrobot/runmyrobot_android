package tv.letsrobot.android.api.interfaces.protocols

import android.content.Context
import android.util.Log
import tv.letsrobot.android.api.components.ControlComponent
import tv.letsrobot.android.api.utils.SingleByteUtil

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
    private val motorForwardSpeed = 90.toByte()
    private val motorBackwardSpeed = (-90).toByte()

    private val motorForwardTurnSpeed = 30.toByte()
    private val motorBackwardTurnSpeed = (-30).toByte()
    override fun onStringCommand(command: String) {
        super.onStringCommand(command)
        when(command){
            "F" -> {onForward()}
            "B" -> {onBack()}
            "L" -> {onLeft()}
            "R" -> {onRight()}
        }
    }

    override fun onStop(any: Any?) {
        super.onStop(any)
        val data = ByteArray(1)
        data[0] = 0x00
        sendToDevice(data)
    }

    private fun onForward() {
        Log.d(TAG, "onForward")
        val data = ByteArray(2)
        data[0] = SingleByteUtil.getDriveSpeed(motorForwardSpeed, 0)
        data[1] = SingleByteUtil.getDriveSpeed(motorForwardSpeed, 1)
        sendToDevice(data)
    }

    private fun onBack() {
        Log.d(TAG, "onBack")
        val data = ByteArray(2)
        data[0] = SingleByteUtil.getDriveSpeed(motorBackwardSpeed, 0)
        data[1] = SingleByteUtil.getDriveSpeed(motorBackwardSpeed, 1)
        sendToDevice(data)
    }

    private fun onLeft() {
        Log.d(TAG, "onLeft")
        val data = ByteArray(2)
        data[0] = SingleByteUtil.getDriveSpeed(motorBackwardTurnSpeed, 0)
        data[1] = SingleByteUtil.getDriveSpeed(motorForwardTurnSpeed, 1)
        sendToDevice(data)
    }

    private fun onRight() {
        Log.d(TAG, "onRight")
        val data = ByteArray(2)
        data[0] = SingleByteUtil.getDriveSpeed(motorForwardTurnSpeed, 0)
        data[1] = SingleByteUtil.getDriveSpeed(motorBackwardTurnSpeed, 1)
        sendToDevice(data)
    }
    companion object {
        const val TAG = "SingleByteProtocol"
    }
}