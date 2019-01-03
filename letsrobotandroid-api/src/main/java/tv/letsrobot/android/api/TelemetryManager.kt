package tv.letsrobot.android.api

import android.content.Context
import android.util.Log
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.utils.PhoneBatteryMeter
import java.io.File
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Brendon on 10/16/2018.
 */
class TelemetryManager(val context: Context) : (String, Any?) -> Unit {
    private var sessionStart: Date = Date()
    private var telemetryFile: File
    private var batteryFile: File
    private var out : PrintStream
    private var batteryOut : PrintStream
    init {
        //main telemetry file
        telemetryFile = File(context.getExternalFilesDir(null), "${dateFormat.format(sessionStart)}-logs")
        Log.d("TelemetryManager", "fileDir = ${telemetryFile.absolutePath}")
        telemetryFile.createNewFile()
        out = PrintStream(telemetryFile.outputStream())
        out.println("time,eventName,value,battery")
        out.println("0, sessionStart, $sessionStart, ")

        batteryFile = File(context.getExternalFilesDir(null), "${dateFormat.format(sessionStart)}-battery")
        batteryFile.createNewFile()
        batteryOut = PrintStream(batteryFile.outputStream())
        batteryOut.println("time,value")
        EventManager.subscribe(this)
    }

    /**
     * Interceptor for all events
     */
    override fun invoke(eventName: String, data: Any?) {
//        when(eventName){
//            CHAT -> {
//                (data as? String)?.let {
//                    //This is so the CSV cannot get messed up by comments, and because we probably don't need to store this
//                    log(eventName, "{REDACTED}")
//                }
//            } // do nothing
//            ROBOT_BYTE_ARRAY -> {} // do nothing
//            BATTERY_EVENT -> logBattery(data)
//            else-> log(eventName, data)
//        }
    }

    private var lastTime: Double = Double.NEGATIVE_INFINITY

    private fun logBattery(data: Any?) {
        val currTime = timeSinceStart
        (data as? PhoneBatteryMeter)?.takeIf {
            lastTime == Double.NEGATIVE_INFINITY
            || currTime - lastTime > TimeUnit.MINUTES.toSeconds(1)
        }?.let {
            batteryOut.println("$currTime,${it.batteryLevel}")
            lastTime = currTime
        }
    }

    private val timeSinceStart: Double
        get() {return (System.currentTimeMillis()-sessionStart.time)/1000.0}

    fun log(eventName : String, data : Any? = null){
        out.print(timeSinceStart)
        out.print(",")
        out.print(eventName)
        out.print(",")
        (data as? ComponentStatus)?.let {
            out.print(it.name)
        }?: run{
            out.print(data.toString())
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

        val BATTERY_EVENT = PhoneBatteryMeter::javaClass.name

        val dateFormat = SimpleDateFormat("yyyyMMddhhmm", Locale.US)

        /**
         * Initialize telemetry manager
         */
        fun init(context: Context){
            _instance = TelemetryManager(context)
        }
    }
}
