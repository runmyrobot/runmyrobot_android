package tv.letsrobot.controller.android

import android.app.Application
import android.widget.Toast
import com.squareup.leakcanary.LeakCanary
import tv.letsrobot.android.api.utils.PhoneBatteryMeter

/**
 * Application class
 */
class RobotApplication : Application() {
    var meter : PhoneBatteryMeter? = null

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
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
    }

    companion object {
        private lateinit var instance : RobotApplication
        val Instance : RobotApplication
            get() {
                return instance
            }
    }
}
