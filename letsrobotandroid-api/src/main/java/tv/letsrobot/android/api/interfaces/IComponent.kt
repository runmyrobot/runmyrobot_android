package tv.letsrobot.android.api.interfaces

import kotlinx.coroutines.Deferred

/**
 * Base methods that any component requires
 */
interface IComponent{
    fun enable() : Deferred<Boolean>
    fun disable() : Deferred<Boolean>
}