package tv.letsrobot.android.api.services

import android.app.Service
import android.content.Intent
import android.os.*
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * The main LetsRobot control service.
 * This handles the lifecycle and communication to components that come from outside the sdk
 */
class LetsRobotService : Service(){

    private var running = false
    /**
     * Target we publish for clients to send messages to MessageHandler.
     */
    private lateinit var mMessenger: Messenger
    private var handlerThread : HandlerThread = HandlerThread("LetsRobotControl").also { it.start() }

    val handler = object : Handler(handlerThread.looper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                START ->
                    enable()
                STOP ->
                    disable()
                else -> super.handleMessage(msg)
            }
        }
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    override fun onBind(intent: Intent): IBinder? {
        Toast.makeText(applicationContext, "binding", Toast.LENGTH_SHORT).show()
        mMessenger = Messenger(handler)
        emitState()
        return mMessenger.binder
    }

    fun enable(){
        Toast.makeText(applicationContext, "Starting LetRobot Controller", Toast.LENGTH_SHORT).show()
        Thread.sleep(5000)
        setState(true)
    }

    fun disable(){
        Toast.makeText(applicationContext, "Stopping LetRobot Controller", Toast.LENGTH_SHORT).show()
        Thread.sleep(5000)
        setState(false)
    }

    fun setState(value : Boolean){
        running = value
        emitState()
    }

    private fun emitState() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(
                Intent(SERVICE_STATUS_BROADCAST).also {
                    it.putExtra("value", running)
                }
        )
    }

    companion object {
        const val START = 1
        const val STOP = 2
        const val QUEUE_UPDATE_TO_SERVER = 3
        const val RESET = 4
        const val SUBSCRIBE = 5

        const val SERVICE_STATUS_BROADCAST = "tv.letsrobot.android.api.ServiceStatus"
    }
}