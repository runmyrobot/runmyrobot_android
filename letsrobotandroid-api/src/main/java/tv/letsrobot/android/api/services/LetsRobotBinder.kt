package tv.letsrobot.android.api.services

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Messenger
import tv.letsrobot.android.api.interfaces.ILetsRobotControl

/**
 * Binder for LetsRobot Service that allows us to put all of the communication code in one class
 */
sealed class LetsRobotBinder : ServiceConnection, ILetsRobotControl{

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

    fun getControlInterface() : ILetsRobotControl{
        return this
    }
}