package tv.letsrobot.android.api.interfaces

import android.os.Message
import kotlinx.coroutines.Deferred

/**
 * Base methods that any component requires
 */
interface IComponent{
    fun enable() : Deferred<Boolean>
    fun disable() : Deferred<Boolean>
    fun sendMessage(message: Message)
}