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
                          /**
                           * Camera orientation
                           */
                          val orientation: CameraDirection = CameraDirection.DIR_90,
                          /**
                           * Use the legacy camera1 api. Automatically uses if less than API 21
                           */
                          val useLegacyApi : Boolean = false,
                          val frameRate : Int = 25)