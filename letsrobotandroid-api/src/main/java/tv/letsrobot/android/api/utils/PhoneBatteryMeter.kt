package tv.letsrobot.android.api.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import tv.letsrobot.android.api.EventManager

/**
 * Monitor Battery level by attaching to the battery broadcast receiver, ans storing the results
 * for everything that wants to access it
 */
class PhoneBatteryMeter : BroadcastReceiver() {
    private var _batteryLevel = -1
    val batteryLevel : Int
        get() {return _batteryLevel}

    private var _scale = -1
    val scale : Int
        get() {return _scale}

    override fun onReceive(context: Context, intent: Intent) {
        val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        //int scale = intent.getIntExtra("scale", -1);
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        var level = -1
        if (rawLevel >= 0 && scale > 0) {
            level = rawLevel * 100 / scale
        }
        _batteryLevel = level
        _scale = scale
        EventManager.invoke(PhoneBatteryMeter::javaClass.name, this)
        Log.d("PhoneBatteryMeter", "rawLevel=$rawLevel : level=$level : scale=$scale")
    }

    companion object {
        private var receiver : PhoneBatteryMeter? = null
        private var intent : Intent? = null

        fun getReceiver(context: Context) : PhoneBatteryMeter {
            receiver ?: kotlin.run { //if meter is null...
                receiver = PhoneBatteryMeter() //register a broadcast receiver to the battery
                val batteryLevelFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                intent = context.registerReceiver(receiver, batteryLevelFilter)
                receiver!!.onReceive(context, intent!!)
            }
            return receiver!!
        }

        fun destroyReceiver(context: Context){
            receiver?.let {
                context.unregisterReceiver(it)
            }
            receiver = null
        }
    }
}
