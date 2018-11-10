package tv.letsrobot.android.api.models

import tv.letsrobot.android.api.enums.CameraDirection

/**
 * Holder to hold settings for camera
 */
data class CameraSettings(val cameraId : String,
                          val pass : String = "hello",
                          val width : Int = 640,
                          val height : Int = 480,
                          val bitrate : Int = 512,
                          val orientation: CameraDirection = CameraDirection.DIR_90,
                          val frameRate : Int = 25)