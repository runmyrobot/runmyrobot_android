package com.runmyrobot.android_robot_for_phone.utils

/**
 * Created by Brendon on 9/7/2018.
 */
class SabertoothDriverUtil {
    companion object {
        val STOP_BYTE: Byte = 0

        fun getDriveSpeed(driveSpeed: Byte, motorNum: Int, scale : Float = 1f): Byte {
            return when (motorNum) {
                0 -> ValueUtil.map(driveSpeed.toFloat(), -128.0f, 127.0f, 1.0f, 127.0f, scale).toInt().toByte()
                1 -> ValueUtil.map(driveSpeed.toFloat(), -128.0f, 127.0f, -128.0f, -1.0f, scale).toInt().toByte()
                else -> ValueUtil.map(0.0f, -128.0f, 127.0f, 1.0f, 127.0f, scale).toInt().toByte()
            }
        }
    }
}
