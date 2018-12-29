package tv.letsrobot.android.api.models

import android.os.Handler
import android.os.Message
import tv.letsrobot.android.api.interfaces.IComponent

/**
 * Message container for components
 */
data class ComponentMessage<T>(
        val target: Class<IComponent>,
        val message: Message,
        val originalHandler: Handler
){

    companion object {
        const val SOCKET_MESSAGE = 0
    }
}
