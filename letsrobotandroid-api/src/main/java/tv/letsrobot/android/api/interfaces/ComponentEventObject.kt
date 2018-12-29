package tv.letsrobot.android.api.interfaces

import java.util.*

/**
 * Object for storing event details
 */
class ComponentEventObject(
        @Component.Companion.Event val type : Int,
        val what : Int,
        val data : Any?,
        source: Any?) : EventObject(source)