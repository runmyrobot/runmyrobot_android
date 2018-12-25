package tv.letsrobot.android.api.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import tv.letsrobot.android.api.interfaces.IComponent
import tv.letsrobot.android.api.interfaces.ILetsRobotControl

/**
 * Binder for LetsRobot Service that allows us to put all of the communication code in one class
 */
class LetsRobotControlApi private constructor(private val context: Context) : ServiceConnection, ILetsRobotControl{
    private var mService: Messenger? = null

    private var mBound = false

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        // This is called when the connection with the service has been
        // established, giving us the object we can use to
        // interact with the service.  We are communicating with the
        // service using a Messenger, so here we get a client-side
        // representation of that from the raw IBinder object.
        mService = Messenger(service)
        mBound = true
    }

    override fun onServiceDisconnected(className: ComponentName) {
        // This is called when the connection with the service has been
        // unexpectedly disconnected -- that is, its process crashed.
        mService = null
        mBound = false
    }

    @Throws(IllegalStateException::class)
    override fun enable() {
        mService?.send(Message.obtain(null, LetsRobotService.START)) ?: throw IllegalStateException()
    }

    override fun disable() {
        mService?.send(Message.obtain(null, LetsRobotService.STOP))
    }

    override fun reset() {
        mService?.send(Message.obtain(null, LetsRobotService.RESET))
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