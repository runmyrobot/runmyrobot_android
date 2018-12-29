package tv.letsrobot.android.api.interfaces

import java.util.*

interface ComponentEventListener : EventListener{
    fun handleMessage(eventObject: ComponentEventObject)
    /**
     * Send an event to whatever is listening.
     *
     * if source not the same class as type, assume that we want to send a message to type instead
     * of a broadcast to all components
     */
    fun handleMessage(@Component.Companion.Event type: Int, what: Int, data: Any?, source : Any?) {
        handleMessage(ComponentEventObject(type, what, data, source))
    }
}
