package tv.letsrobot.android.api.models

import android.os.Handler
import android.os.Message
import tv.letsrobot.android.api.components.WatchDogComponent
import kotlin.reflect.KClass

/**
 * Message container for components
 */
data class ComponentMessage(
        val target: KClass<WatchDogComponent>,
        val message: Message,
        val originalHandler: Handler
){

    companion object {
        const val SOCKET_MESSAGE = 0
    }
}
