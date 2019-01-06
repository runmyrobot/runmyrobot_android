package tv.letsrobot.android.api.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * Helper for BroadcastReceiver
 */
class InlineBroadcastReceiver(val intentFilter: String,
                              val callback : (context: Context?, intent: Intent?) -> Unit) {
    private var receiver : Receiver? = null

    fun register(context: Context){
        receiver = Receiver(callback)
        context.registerReceiver(receiver, IntentFilter(intentFilter))
    }

    fun unregister(context: Context){
        context.unregisterReceiver(receiver)
        receiver = null
    }

    private class Receiver(val callback : (context: Context?, intent: Intent?) -> Unit) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            callback(context, intent)
        }
    }
}