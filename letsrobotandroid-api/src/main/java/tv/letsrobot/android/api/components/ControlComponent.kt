package tv.letsrobot.android.api.components

import android.content.Context
import android.util.Log
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.EventManager.Companion.ROBOT_BYTE_ARRAY
import tv.letsrobot.android.api.interfaces.Component

/**
 *  Base robot control component
 *  Extend from this to hook up to the core interface properly
 *
 *  Subscribes to EventManager.COMMAND and EventManager.STOP_EVENT automatically
 */
abstract class ControlComponent(context: Context) : Component(context){

    /**
     * Called when any command is received, including but not limited to strings
     */
    open fun onCommand(command : Any?){Log.d(TAG, "onCommand")}

    /**
     * Called when a command is received, and is a non-null String
     */
    open fun onStringCommand(command : String){Log.d(TAG, "onStringCommand $command")}
    open fun onStop(any : Any?){Log.d(TAG, "onStop")}

    override fun enableInternal(){
        Log.d(TAG, "enable")
        handleSubscriptions(true)
    }

    override fun disableInternal(){
        Log.d(TAG, "disable")
        handleSubscriptions(false)
    }

    override fun timeout() {
        super.timeout()
        onStopInternal(null)
    }

    /**
     * Enable or disable subscriptions to EventManager
     */
    private fun handleSubscriptions(enable : Boolean){
        if(enable){
            EventManager.subscribe(EventManager.COMMAND, onCommandInternal)
            EventManager.subscribe(EventManager.STOP_EVENT, onStopInternal)
        }
        else{
            EventManager.unsubscribe(EventManager.COMMAND, onCommandInternal)
            EventManager.unsubscribe(EventManager.STOP_EVENT, onStopInternal)
        }
    }

    private val onCommandInternal: (Any?) -> Unit = { command ->
        (command as? String)?.let{it ->
            onStringCommand(it)
        }
        onCommand(command)
    }

    private val onStopInternal : (Any?) -> Unit  = {
        onStop(it)
    }

    /**
     * Send message out to device though our event manager.
     */
    fun sendToDevice(byteArray: ByteArray?){
        byteArray?.let {
            Log.d("ControlComponent","sendToDevice")
            EventManager.invoke(ROBOT_BYTE_ARRAY, byteArray)
        }
    }

    companion object {
        const val TAG = "ControlComponent"
    }
}