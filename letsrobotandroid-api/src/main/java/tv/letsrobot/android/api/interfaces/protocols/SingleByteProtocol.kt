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
    private val motorForwardSpeed = 90
    private val motorBackwardSpeed = -90

    private val motorForwardTurnSpeed = 30
    private val motorBackwardTurnSpeed = -30
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

    /**
     * Create a single byte packet of duplicate motors. Passing in one argument will make
     * both motors move at the same speed
     *
     * Values must be in the byte range, or it may not work correctly
     */
    private fun createPacket(motor0Speed : Int, motor1Speed : Int = Int.MAX_VALUE) : ByteArray{
        //allow for passing in a single variable if both are the same
        var motor1 = motor1Speed
        if(motor1 == Int.MAX_VALUE)
            motor1 = motor0Speed
        val data = ByteArray(2)
        data[0] = SingleByteUtil.getDriveSpeed(motor0Speed.toByte(), 0)
        data[1] = SingleByteUtil.getDriveSpeed(motor1.toByte(), 1)
        return data
    }

    private fun onForward() {
        Log.d(TAG, "onForward")
        sendToDevice(createPacket(motorForwardSpeed))
    }

    private fun onBack() {
        Log.d(TAG, "onBack")
        sendToDevice(createPacket(motorBackwardSpeed))
    }

    private fun onLeft() {
        Log.d(TAG, "onLeft")
        sendToDevice(createPacket(motorBackwardTurnSpeed, motorForwardTurnSpeed))
    }

    private fun onRight() {
        Log.d(TAG, "onRight")
        sendToDevice(createPacket(motorForwardTurnSpeed, motorBackwardTurnSpeed))
    }

    companion object {
        const val TAG = "SingleByteProtocol"
    }
}