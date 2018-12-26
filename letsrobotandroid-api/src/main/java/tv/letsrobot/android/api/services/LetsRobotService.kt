package tv.letsrobot.android.api.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Toast

/**
 * The main LetsRobot control service.
 * This handles the lifecycle and communication to components that come from outside the sdk
 */
class LetsRobotService : Service(){
    /**
     * Target we publish for clients to send messages to MessageHandler.
     */
    private lateinit var mMessenger: Messenger

    /**
     * Handler of incoming messages from clients.
     */
    internal class MessageHandler(
            context: Context,
            private val applicationContext: Context = context.applicationContext,
            handlerThread : HandlerThread = HandlerThread("LetsRobotControlApi").also { it.start() }
    ) : Handler(handlerThread.looper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                START ->
                    Toast.makeText(applicationContext, "Starting LetRobot Controller", Toast.LENGTH_SHORT).show()
                STOP ->
                    Toast.makeText(applicationContext, "Stopping LetRobot Controller", Toast.LENGTH_SHORT).show()
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
        mMessenger = Messenger(MessageHandler(this))
        return mMessenger.binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId).also {

        }
    }

    companion object {
        const val START = 1
        const val STOP = 2
        const val QUEUE_UPDATE_TO_SERVER = 3
        const val RESET = 4
        const val SUBSCRIBE = 5
    }
}