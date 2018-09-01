package com.runmyrobot.android_robot_for_phone.activities

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import com.runmyrobot.android_robot_for_phone.BuildConfig
import com.runmyrobot.android_robot_for_phone.R
import com.runmyrobot.android_robot_for_phone.api.Core
import kotlinx.android.synthetic.main.activity_main_robot.*

/**
 * Based off of this sample
 * https://github.com/vanevery/Android-MJPEG-Video-Capture-FFMPEG/blob/master/src/com/mobvcasting/mjpegffmpeg/MJPEGFFMPEGTest.java
 */
class MainRobotActivity : Activity(){
    private var recording = false
    var core: Core? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        setContentView(R.layout.activity_main_robot)
        val builder = Core.Builder(applicationContext)
        builder.holder = cameraSurfaceView.holder
        builder.robotId = BuildConfig.ROBOT_ID
        builder.cameraId = BuildConfig.CAMERA_ID
        try {
            core = builder.build()
        } catch (e: Core.InitializationException) {
            e.printStackTrace()
        }
        recordButtonMain.setOnClickListener{
            if (recording) {
                recording = false
                if (core != null)
                    core!!.disable()
                Log.v(LOGTAG, "Recording Stopped")
            } else {
                recording = true
                if (core != null)
                    core!!.enable()
                Log.v(LOGTAG, "Recording Started")
            }
        }
    }

    companion object {
        const val LOGTAG = "MainRobot"
    }
}