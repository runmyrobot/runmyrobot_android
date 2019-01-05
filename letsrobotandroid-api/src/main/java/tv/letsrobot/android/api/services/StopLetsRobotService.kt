package tv.letsrobot.android.api.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder

class StopLetsRobotService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId).also {
            System.exit(0) //just kill the app with no regard as to what the state was. Should be fine...
            stopSelf()
        }
    }

    companion object {
        fun getIntent(context: Context) : Intent{
            return Intent(context, StopLetsRobotService::class.java)
        }
    }
}
