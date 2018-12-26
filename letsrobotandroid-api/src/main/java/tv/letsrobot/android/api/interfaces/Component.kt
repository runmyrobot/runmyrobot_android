package tv.letsrobot.android.api.interfaces

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.suspendCancellableCoroutine
import tv.letsrobot.android.api.Core
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.enums.ComponentStatus
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Base component object to use to extend functionality of your robot.
 * Ex. can be used as an interface for LEDs based off of control messages
 */
abstract class Component(val context: Context) : IComponent{
    private var handlerThread = HandlerThread(
            javaClass.simpleName
    ).also { it.start() }
    private var handler = Handler(handlerThread.looper)
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
    override fun enable() = GlobalScope.async{
        if(enabled.getAndSet(true)) return@async false
        status = ComponentStatus.CONNECTING
        awaitCallback<Boolean> { enableWithCallback(it) }
        return@async true
    }

    fun enableWithCallback(callback: Callback<Boolean>){
        handler.post {
            enableInternal()
            callback.onComplete(true)
        }
    }

    fun disableWithCallback(callback: Callback<Boolean>){
        handler.post {
            disableInternal()
            callback.onComplete(true)
        }
    }

    interface Callback<T> {
        fun onComplete(result: T)
        fun onException(e: Exception?)
    }

    suspend fun <T> awaitCallback(block: (Callback<T>) -> Unit) : T =
            suspendCancellableCoroutine { cont ->
                block(object : Callback<T> {
                    override fun onComplete(result: T) = cont.resume(result)
                    override fun onException(e: Exception?) {
                        e?.let { cont.resumeWithException(it) }
                    }
                })
            }

    /**
     * Called when component should shut down
     *
     * Will return without action if already enabled
     */
    override fun disable() = GlobalScope.async{
        if(!enabled.getAndSet(false)) return@async false
        awaitCallback<Boolean> { disableWithCallback(it) }
        status = ComponentStatus.DISABLED
        return@async true
    }

    /**
     * Called when we have not received a response from the server in a while
     */
    open fun timeout(){}
}
