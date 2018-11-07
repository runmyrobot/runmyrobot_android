package com.runmyrobot.android_robot_for_phone

import android.app.Application
import android.widget.Toast
import tv.letsrobot.android.api.TelemetryManager
import tv.letsrobot.android.api.utils.PhoneBatteryMeter

/**
 * Application class
 */
class RobotApplication : Application() {
    var meter : PhoneBatteryMeter? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        meter = PhoneBatteryMeter.getReceiver(applicationContext)
    }

    /**
     * Report an error. Do not report if user has reporting turned off
     *
     * Tying every error through here to make it easy to switch error reporting
     */
    fun reportError(e: Exception) {
        Toast.makeText(this, "ERROR:\n${e.message} " +
                "\n", Toast.LENGTH_LONG).show()
        TelemetryManager.Instance?.invoke("error", e.toString())
    }

    companion object {
        private lateinit var instance : RobotApplication
        val Instance : RobotApplication
            get() {
                return instance
            }
    }
}
