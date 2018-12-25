package tv.letsrobot.android.api.interfaces

/**
 * Interface for communicating with the robot service
 */
interface ILetsRobotControl : IComponent {

    fun reset()

    /**
     * Attach a custom component to the lifecycle. Must call reset() for changes to take effect
     */
    fun attachToLifecycle(component: IComponent)

    /**
     * detach a custom component from the lifecycle. Must call reset() for changes to take effect
     */
    fun detachFromLifecycle(component: IComponent)
}