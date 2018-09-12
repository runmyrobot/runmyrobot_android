package com.runmyrobot.android_robot_for_phone.control

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 * Created by Brendon on 9/9/2018.
 */
interface CommunicationInterface{
    fun initConnection(context: Context)
    fun enable()
    fun disable()
    /**
     * Query this component to see if it needs custom setup
     *
     * Pass in activity
     */
    fun needsSetup(activity: Activity) : Boolean

    /**
     * Start setting up a component. Must pass in the current activity as it may
     * try launching an activity and wait for result
     */
    fun setupComponent(activity: Activity) : Int

    /**
     * This gets called in onActivityResult in parent activity. Probably not a good way to do this.
     * May get refactored at some point
     */
    fun receivedComponentSetupDetails(context: Context, intent: Intent?)
    fun isConnected() : Boolean
    fun send(byteArray: ByteArray) : Boolean
}
