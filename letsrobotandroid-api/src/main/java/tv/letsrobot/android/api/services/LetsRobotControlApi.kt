package tv.letsrobot.android.api.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import tv.letsrobot.android.api.interfaces.IComponent
import tv.letsrobot.android.api.interfaces.ILetsRobotControl
import tv.letsrobot.android.api.models.Operation

/**
 * Binder for LetsRobot Service that allows us to put all of the communication code in one class
 */
class LetsRobotControlApi private constructor(private val context: Context) : ServiceConnection, ILetsRobotControl{

    private val serviceState: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    private val serviceConnectionStatus: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    private var mService: Messenger? = null

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        // This is called when the connection with the service has been
        // established, giving us the object we can use to
        // interact with the service.  We are communicating with the
        // service using a Messenger, so here we get a client-side
        // representation of that from the raw IBinder object.
        mService = Messenger(service)
        serviceConnectionStatus.value = Operation.OK
    }

    override fun onServiceDisconnected(className: ComponentName) {
        // This is called when the connection with the service has been
        // unexpectedly disconnected -- that is, its process crashed.
        mService = null
        serviceConnectionStatus.value = Operation.NOT_OK
    }

    @Throws(IllegalStateException::class)
    override fun enable() {
        serviceState.value = Operation.LOADING
        sendStateUnsafe(LetsRobotService.START)
    }

    @Throws(IllegalStateException::class)
    override fun disable() {
        serviceState.value = Operation.LOADING
        sendStateUnsafe(LetsRobotService.STOP)
    }

    @Throws(IllegalStateException::class)
    override fun reset() {
        sendStateUnsafe(LetsRobotService.RESET)
    }

    @Throws(IllegalStateException::class)
    private fun sendStateUnsafe(what : Int){
        mService?.send(Message.obtain(null, what)) ?: throw IllegalStateException()
    }

    override fun getServiceStateObserver(): LiveData<Int> {
        return serviceState
    }

    override fun getServiceConnectionStatusObserver(): LiveData<Int> {
        return serviceConnectionStatus
    }

    override fun attachToLifecycle(component: IComponent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun detachFromLifecycle(component: IComponent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun connectToService() {
        Intent(context, LetsRobotService::class.java).also { intent ->
            context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        }
    }

    override fun disconnectFromService() {
        context.unbindService(this)
    }

    companion object {
        fun getNewInstance(context: Context) : ILetsRobotControl{
            return LetsRobotControlApi(context)
        }
    }
}