package tv.letsrobot.android.api.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import tv.letsrobot.android.api.R
import tv.letsrobot.android.api.enums.ComponentType
import tv.letsrobot.android.api.enums.LogLevel
import tv.letsrobot.android.api.interfaces.ComponentEventListener
import tv.letsrobot.android.api.interfaces.ComponentEventObject
import tv.letsrobot.android.api.interfaces.IComponent
import tv.letsrobot.android.api.utils.InlineBroadcastReceiver
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

/**
 * The main LetsRobot control service.
 * This handles the lifecycle and communication to components that come from outside the sdk
 */
class LetsRobotService : Service(), ComponentEventListener {
    private var running = false
    private val componentList = ArrayList<IComponent>()
    private val activeComponentList = ArrayList<IComponent>()

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
                ATTACH_COMPONENT -> {
                    (msg.obj as? IComponent)?.let {
                        addToLifecycle(it)
                    }
                }
                DETACH_COMPONENT -> {
                    (msg.obj as? IComponent)?.let {
                        removeFromLifecycle(it)
                    }
                }
                RESET -> {
                    reset()
                }
                EVENT_BROADCAST ->{
                    sendToComponents(msg)
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    private var stopListenerReceiver: InlineBroadcastReceiver? = null
    private lateinit var mNotificationManager: NotificationManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(isStarted.getAndSet(true)) {
            startForeground(Random().nextInt(), Notification())
            stopForeground(true)
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }
        mNotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        stopListenerReceiver = InlineBroadcastReceiver(SERVICE_STOP_BROADCAST){ _, receiverIntent ->
            stopService()
            System.exit(0)
        }.also {
            it.register(this)
        }
        tryCreateNotificationChannel()
        return super.onStartCommand(intent, flags, startId).also {
            val intentHide = Intent(LetsRobotService.SERVICE_STOP_BROADCAST)
            val hide = PendingIntent.getBroadcast(this,
                    System.currentTimeMillis().toInt(), intentHide, PendingIntent.FLAG_CANCEL_CURRENT)
            val notification = NotificationCompat.Builder(this, LETSROBOT_SERVICE_CHANNEL)
                    .setContentTitle("LetsRobot Controller")
                    .setContentText("App is running in the background.")
                    .addAction(R.drawable.ic_power_settings_new_black_24dp, "Terminate app", hide)
                    .setSmallIcon(R.drawable.ic_settings_remote_black_24dp)
            startForeground(Random().nextInt(), notification.build())
            handler.obtainMessage(RESET).sendToTarget()
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

    private fun stopService(){
        if(running)
            runBlocking { disable() }
        stopListenerReceiver?.unregister(this)
        stopForeground(true)
        stopSelf()
        isStarted.set(false)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopService()
        super.onTaskRemoved(rootIntent)
    }

    /**
     * Message handler for components that we are controlling.
     * Best thing to do after is to push it to the service handler for processing,
     * as this could be from any thread
     */
    override fun handleMessage(eventObject: ComponentEventObject) {
        handler.obtainMessage(EVENT_BROADCAST, eventObject).sendToTarget()
    }

    private fun sendToComponents(msg: Message) {
        val obj = msg.obj as? ComponentEventObject
        var targetFilter : ComponentType? = null
        obj?.let {
            if((obj.source as? IComponent)?.getType() != obj.type){
                //send a message to all components of type obj.type
                targetFilter = obj.type
            }
        }

        activeComponentList.forEach { component ->
            targetFilter?.takeIf { component.getType() != it }
                    ?: component.dispatchMessage(msg)
        }
    }

    /**
     * Reset the service. If running, we will disable, reload, then start again
     */
    private fun reset() {
        if(running) {
            disable()
            reload()
            enable()
        }
        else{
            reload()
        }
    }

    /**
     * Reload the settings and prep for start
     */
    private fun reload() {

    }

    private fun addToLifecycle(component: IComponent) {
        if(!componentList.contains(component))
            componentList.add(component)
    }

    private fun removeFromLifecycle(component: IComponent) {
        componentList.remove(component)
    }

    /**
     * enable the components via co-routines. Calling this is only allowed via a co-routine,
     * and blocks the current thread
     * This prevents race conditions from happening between the UI and the service.
     * This also holds up any new messages until after all components are enabled
     */
    fun enable(){
        val componentListener : ComponentEventListener = this
        runBlocking {
            Toast.makeText(applicationContext, "Starting LetRobot Controller", Toast.LENGTH_SHORT).show()
            activeComponentList.clear()
            activeComponentList.addAll(componentList)
            val list = ArrayList<Deferred<Boolean>>()

            //enable all of our components
            activeComponentList.forEach{
                it.setEventListener(componentListener)
                val deferred = it.enable()
                list.add(deferred) //add their deferred result to a list
            }

            //now wait for each one to complete
            list.forEach {
                it.await()
            }
            setState(true)
        }
    }

    /**
     * Disables components, blocking the service messaging thread until complete
     */
    fun disable(){
        Toast.makeText(applicationContext, "Stopping LetRobot Controller", Toast.LENGTH_SHORT).show()
        runBlocking {
            activeComponentList.forEach{
                it.disable().await()
                it.setEventListener(null)
            }
            setState(false)
        }
    }

    private fun setState(value : Boolean){
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

    private fun tryCreateNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //visible to user
            val name = applicationContext.getString(R.string.channel_name)
            //visible to user
            val description = applicationContext.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            //create a notification channel
            val mChannel = NotificationChannel(LETSROBOT_SERVICE_CHANNEL, name, importance)
            mChannel.description = description
            mChannel.enableLights(false)
            mChannel.setSound(null, null)
            mChannel.setShowBadge(false)
            mChannel.enableVibration(false)
            try {
                mNotificationManager.createNotificationChannel(mChannel)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val START = 1
        const val STOP = 2
        const val RESET = 4
        const val ATTACH_COMPONENT = 5
        const val DETACH_COMPONENT = 6
        const val EVENT_BROADCAST = 7
        const val LETSROBOT_SERVICE_CHANNEL = "lr_service"
        const val SERVICE_STATUS_BROADCAST = "tv.letsrobot.android.api.ServiceStatus"
        const val SERVICE_STOP_BROADCAST = "tv.letsrobot.android.api.request.stop"
        lateinit var logLevel: LogLevel
        private val isStarted = AtomicBoolean(false)
    }
}