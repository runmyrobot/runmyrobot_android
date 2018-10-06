package tv.letsrobot.android.api.interfaces

import android.content.Context
import tv.letsrobot.android.api.Core
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.enums.ComponentStatus
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Base component object to use to extend functionality of your robot.
 * Ex. can be used as an interface for LEDs based off of control messages
 */
abstract class Component(val context: Context){
    private var _status: ComponentStatus = ComponentStatus.DISABLED_FROM_SETTINGS
    var status : ComponentStatus
        get() = _status
        set(value) {
            if(_status == value) return //Only set state if changed
            _status = value
            EventManager.invoke(javaClass.name, value)
        }

    init {
        status = ComponentStatus.DISABLED
    }
    protected val coreInstance : Core? = null
    protected val enabled = AtomicBoolean(false)

    protected abstract fun enableInternal()
    protected abstract fun disableInternal()


    /**
     * Called when component should startup. Multiple calls will not trigger setup more than once
     *
     * Return false if value was already true
     */
    fun enable() : Boolean{
        if(enabled.getAndSet(true)) return false
        status = ComponentStatus.CONNECTING
        enableInternal()
        return true
    }

    /**
     * Called when component should shut down
     *
     * return false if value was already false
     */
    fun disable() : Boolean{
        if(!enabled.getAndSet(false)) return false
        disableInternal()
        status = ComponentStatus.DISABLED
        return true
    }

    /**
     * Called when we have not received a response from the server in a while
     */
    open fun timeout(){}
}
