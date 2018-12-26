package tv.letsrobot.android.api.interfaces

import androidx.lifecycle.LiveData
import tv.letsrobot.android.api.enums.Operation

/**
 * Interface for communicating with the robot service
 */
interface ILetsRobotControl {

    /**
     * Enable the connection
     */
    fun enable()

    /**
     * Disable the connection
     */
    fun disable()

    /**
     * Reset the service, and pull new info. Generally called after settings were changed
     */
    fun reset()

    /**
     * Attach a custom component to the lifecycle. Must call reset() for changes to take effect
     */
    fun attachToLifecycle(component: IComponent)

    /**
     * detach a custom component from the lifecycle. Must call reset() for changes to take effect
     */
    fun detachFromLifecycle(component: IComponent)

    /**
     * Subscribe to status events.
     * @return androidx.lifecycle.LiveData<Boolean>
     */
    fun getServiceStateObserver() : LiveData<Operation>

    /**
     * Subscribe to service connection events.
     * @return androidx.lifecycle.LiveData<Boolean>
     */
    fun getServiceConnectionStatusObserver() : LiveData<Operation>

    /**
     * Disconnect from service. Calling this does not terminate the service.
     */
    fun disconnectFromService()

    /**
     * Connect to the service
     */
    fun connectToService()
}