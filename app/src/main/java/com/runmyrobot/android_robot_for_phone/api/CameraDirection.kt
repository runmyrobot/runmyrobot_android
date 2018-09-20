package com.runmyrobot.android_robot_for_phone.api

enum class CameraDirection {
    DIR_0, DIR_90, DIR_180, DIR_270;

    override fun toString(): String {
        return when(this){
            CameraDirection.DIR_0 -> "0"
            CameraDirection.DIR_90 -> "90"
            CameraDirection.DIR_180 -> "180"
            CameraDirection.DIR_270 -> "270"
        }
    }
}
