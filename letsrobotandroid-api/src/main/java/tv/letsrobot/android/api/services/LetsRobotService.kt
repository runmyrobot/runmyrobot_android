package tv.letsrobot.android.api.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Toast

/**
 * Created by Brendon on 12/24/2018.
 */
class LetsRobotService : Service() {
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
            handlerThread : HandlerThread = HandlerThread("LetsRobotBinder").also { it.start() }
    ) : Handler(handlerThread.looper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 ->
                    Toast.makeText(applicationContext, "hello!", Toast.LENGTH_SHORT).show()
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
}