package tv.letsrobot.android.api

import android.content.Context
import android.util.Log
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.utils.PhoneBatteryMeter
import java.io.File
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Brendon on 10/16/2018.
 */
class TelemetryManager(val context: Context) : (String, Any?) -> Unit {
    private var sessionStart: Date = Date()

    private var telemetryFile: File
    private var out : PrintStream
    init {
        telemetryFile = File(context.getExternalFilesDir(null), dateFormat.format(sessionStart))
        Log.d("TelemetryManager", "fileDir = ${telemetryFile.absolutePath}")
        telemetryFile.createNewFile()
        out = PrintStream(telemetryFile.outputStream())
        EventManager.subscribe(this)
        out.println("time,eventName,value,battery")
    }

    /**
     * Interceptor for all events
     */
    override fun invoke(eventName: String, data: Any?) {
        out.print("${System.currentTimeMillis()}")
        out.print(",")
        (data as? ComponentStatus)?.let {
            out.print("$eventName,${it.name}")
        }?: run{
            out.print("$eventName,${data.toString()}")
        }
        out.print(",")
        out.println(PhoneBatteryMeter.getReceiver(context.applicationContext).batteryLevel)
    }

    companion object {
        private var _instance: TelemetryManager? = null

        /**
         * Get instance of TelemetryManager
         *
         * Will return null if never initialized!
         */
        val Instance : TelemetryManager?
            get() {
                if(_instance == null)
                    Log.e("TelemetryManager"
                            ,"Instance not yet instantiated. Nothing will happen!"
                            , ExceptionInInitializerError())
                return _instance
            }

        val dateFormat = SimpleDateFormat("yyyyMMddhhmm", Locale.US)

        /**
         * Initialize telemetry manager
         */
        fun init(context: Context){
            _instance = TelemetryManager(context)
        }
    }
}
