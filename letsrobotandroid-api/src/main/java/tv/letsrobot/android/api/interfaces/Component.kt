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
abstract class Component(val context: Context) : IComponent{
    private var _status: ComponentStatus = ComponentStatus.DISABLED_FROM_SETTINGS
    var status : ComponentStatus
        get() = _status
        set(value) {
            if(_status == value) return //Only set state if changed
            _status = value
            EventManager.invoke(getName(), value)
        }

    init {
        status = ComponentStatus.DISABLED
    }
    protected val coreInstance : Core? = null
    protected val enabled = AtomicBoolean(false)

    protected abstract fun enableInternal()
    protected abstract fun disableInternal()

    open fun getName() : String{
        return javaClass.simpleName
    }


    /**
     * Called when component should startup. Will return without action if already enabled
     */
    override fun enable(){
        if(enabled.getAndSet(true)) return
        status = ComponentStatus.CONNECTING
        enableInternal()
    }

    /**
     * Called when component should shut down
     *
     * Will return without action if already enabled
     */
    override fun disable(){
        if(!enabled.getAndSet(false)) return
        disableInternal()
        status = ComponentStatus.DISABLED
    }

    /**
     * Called when we have not received a response from the server in a while
     */
    open fun timeout(){}
}
