package com.runmyrobot.android_robot_for_phone.robot

import android.content.Context
import tv.letsrobot.android.api.interfaces.Component

/**
 * Example of a custom component
 */
class CustomComponentExample(context: Context, customParam : String) : Component(context){
    override fun enableInternal() {
        //add code here
    }

    override fun disableInternal() {
        //Destroy or disable code
    }
}