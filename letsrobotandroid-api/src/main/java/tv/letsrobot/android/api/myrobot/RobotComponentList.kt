package tv.letsrobot.android.api.myrobot

import android.content.Context
import tv.letsrobot.android.api.interfaces.Component

/**
 * Use this class to inject other Component objects. This will initialize them, but not enable them
 */
object RobotComponentList{
    val components = ArrayList<Component>()
    fun init(context: Context){
        components.clear()
        //components.add(SingleByteProtocol(context)) //SaberTooth Simplified Serial Motor control through USB
        //Add other custom components here to be added to the Core
    }
}