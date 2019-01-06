package tv.letsrobot.android.api.services

import android.content.*
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import tv.letsrobot.android.api.enums.Operation
import tv.letsrobot.android.api.interfaces.IComponent
import tv.letsrobot.android.api.interfaces.ILetsRobotControl

/**
 * Binder for LetsRobot Service that allows us to put all of the communication code in one class
 */
class LetsRobotControlApi private constructor(
        anyContext: Context,
        private val context : Context = anyContext.applicationContext
) : ServiceConnection, ILetsRobotControl{

    /**
     * LiveData object for whether or not the service has the components enabled
     */
    private val serviceStateObserver: MutableLiveData<Operation> by lazy {
        MutableLiveData<Operation>()
    }

    /**
     * LiveData object for whether or not we have a valid connection to the service
     */
    private val serviceBoundObserver: MutableLiveData<Operation> by lazy {
        MutableLiveData<Operation>()
    }

    private var mService: Messenger? = null

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        // This is called when the connection with the service has been
        // established, giving us the object we can use to
        // interact with the service.  We are communicating with the
        // service using a Messenger, so here we get a client-side
        // representation of that from the raw IBinder object.
        mService = Messenger(service)
        serviceBoundObserver.value = Operation.OK
    }

    override fun onServiceDisconnected(className: ComponentName) {
        // This is called when the connection with the service has been
        // unexpectedly disconnected -- that is, its process crashed.
        mService = null
        serviceBoundObserver.value = Operation.NOT_OK
    }

    @Throws(IllegalStateException::class)
    override fun enable() {
        serviceStateObserver.value = Operation.LOADING
        sendStateUnsafe(LetsRobotService.START)
    }

    @Throws(IllegalStateException::class)
    override fun disable(){
        serviceStateObserver.value = Operation.LOADING
        sendStateUnsafe(LetsRobotService.STOP)
    }

    @Throws(IllegalStateException::class)
    override fun reset() {
        sendStateUnsafe(LetsRobotService.RESET)
    }

    override fun attachToLifecycle(component: IComponent) {
        sendStateUnsafe(LetsRobotService.ATTACH_COMPONENT, component)
    }

    override fun detachFromLifecycle(component: IComponent) {
        sendStateUnsafe(LetsRobotService.DETACH_COMPONENT, component)
    }

    @Throws(IllegalStateException::class)
    private fun sendStateUnsafe(what : Int, obj : Any? = null) {
        val message = obj?.let {
            Message.obtain(null, what, obj)
        } ?: Message.obtain(null, what)
        mService?.send(message) ?: throw IllegalStateException()
    }

    override fun getServiceStateObserver(): LiveData<Operation> {
        return serviceStateObserver
    }

    override fun getServiceBoundObserver(): LiveData<Operation> {
        return serviceBoundObserver
    }

    private var receiver = Receiver(serviceStateObserver){ //onDisconnectRequest
        disconnectFromService()
    }

    override fun connectToService() {
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver,
                IntentFilter(LetsRobotService.SERVICE_STATUS_BROADCAST))
        Intent(context, LetsRobotService::class.java).also { intent ->
            context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        }
    }

    override fun disconnectFromService() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        context.unbindService(this)
    }

    class Receiver(val liveData: MutableLiveData<Operation>, val disconnectCallback : (() -> Unit)? = null) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                when(it.action){
                    LetsRobotService.SERVICE_STATUS_BROADCAST -> {
                        setLiveData(it.getBooleanExtra("value", false))
                    }
                    LetsRobotService.SERVICE_STOP_BROADCAST -> {
                        disconnectCallback?.invoke()
                    }
                    else ->{/*do nothing*/}
                }
            }
        }

        private fun setLiveData(value: Boolean){
            liveData.value = if(value){
                Operation.OK
            }else{
                Operation.NOT_OK
            }
        }
    }

    companion object {
        fun getNewInstance(context: Context) : ILetsRobotControl{
            return LetsRobotControlApi(context)
        }
    }
}