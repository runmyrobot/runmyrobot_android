package com.runmyrobot.android_robot_for_phone

import android.app.Application
import com.bugsnag.android.Bugsnag
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import com.runmyrobot.android_robot_for_phone.utils.StoreUtil

/**
 * Application class
 */
class RobotApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        Bugsnag.init(this)
        if(!StoreUtil.getErrorReportingEnabled(this)){
            Bugsnag.setNotifyReleaseStages("")
        }
        val ffmpeg = FFmpeg.getInstance(applicationContext)
        try {
            ffmpeg.loadBinary(object : LoadBinaryResponseHandler() {
                //TODO maybe catch some error to display to the user
            })
        } catch (e: FFmpegNotSupportedException) {
            e.printStackTrace()
        }
    }

    fun getCameraPass(): String {
        return StoreUtil.getCameraPass(this)
    }

    fun getBitrate() : String {
        return StoreUtil.getBitrate(this)
    }

    fun getResolution() : String {
        return StoreUtil.getResolution(this)
    }

    companion object {
        private lateinit var instance : RobotApplication
        val Instance : RobotApplication
            get() {
                return instance
            }
    }
}
