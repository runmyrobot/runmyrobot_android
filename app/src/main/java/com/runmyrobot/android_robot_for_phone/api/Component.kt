package com.runmyrobot.android_robot_for_phone.api

import android.content.Context
import android.support.annotation.CallSuper
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Brendon on 8/26/2018.
 */
abstract class Component(val context: Context){

    protected val coreInstance : Core? = null
    protected val enabled = AtomicBoolean(false)

    /**
     * Called when component should startup. Multiple calls will not trigger setup more than once
     */
    @CallSuper
    open fun enable(){
        if(enabled.getAndSet(true)){
            return
        }

    }

    /**
     * Called when component should shut down
     */
    @CallSuper
    open fun disable(){
        if(enabled.getAndSet(true)){
            return
        }
    }

    /**
     * Called when we have not received a response from the server in a while
     */
    open fun timeout(){}
}
