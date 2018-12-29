package tv.letsrobot.android.api.interfaces

import android.os.Message
import kotlinx.coroutines.Deferred

/**
 * Base methods that any component requires
 */
interface IComponent{
    /**
     * Enables the component asynchronously, and will return the result to a listening co-routine
     */
    fun enable() : Deferred<Boolean>
    /**
     * Disables the component asynchronously, and will return the result to a listening co-routine
     */
    fun disable() : Deferred<Boolean>

    /**
     * Dispatches a message to the component's handler.
     */
    fun dispatchMessage(message: Message)

    /**
     * Set an event listener for this component
     */
    fun setEventListener(listener : ComponentEventListener?)

    /**
     * Gets the current type of the component based on Component.Companion.Event
     */
    @Component.Companion.Event
    fun getType() : Int
}