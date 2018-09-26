package com.runmyrobot.android_robot_for_phone

import android.app.Application
import android.widget.Toast
import com.runmyrobot.android_robot_for_phone.utils.StoreUtil

/**
 * Application class
 */
class RobotApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        if(!StoreUtil.getErrorReportingEnabled(this)){
            //TODO enable crash reporting
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

    /**
     * Report an error. Do not report if user has reporting turned off
     *
     * Tying every error through here to make it easy to switch error reporting servers in the future
     */
    fun reportError(e: Exception) {
        if(StoreUtil.getErrorReportingEnabled(this)) {
            //TODO enable error reporting
            Toast.makeText(this, "ERROR:\n${e.message} " +
                    "\nError has been reported", Toast.LENGTH_LONG).show()
        }
        else
            Toast.makeText(this, "ERROR:\n${e.message} " +
                    "\nNot reporting since reporting is turned off", Toast.LENGTH_LONG).show()
    }

    companion object {
        private lateinit var instance : RobotApplication
        val Instance : RobotApplication
            get() {
                return instance
            }
    }
}
