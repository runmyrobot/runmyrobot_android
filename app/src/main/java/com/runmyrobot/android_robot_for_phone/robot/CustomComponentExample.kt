package com.runmyrobot.android_robot_for_phone.robot

import android.content.Context
import tv.letsrobot.android.api.interfaces.Component

/**
 * Example of a custom component
 */
class CustomComponentExample(context: Context, customParam : String) : Component(context){
    override fun enable(): Boolean {
        if(!super.enable()) return false
        //add code here
        return true
    }

    override fun disable(): Boolean {
        if(!super.disable()) return false //required. Will be removed in future version
        //Destroy or disable code
        return true //required. Will be removed in future version
    }
}