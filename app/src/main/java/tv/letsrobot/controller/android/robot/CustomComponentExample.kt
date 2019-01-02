package tv.letsrobot.controller.android.robot

import android.content.Context
import tv.letsrobot.android.api.enums.ComponentType
import tv.letsrobot.android.api.interfaces.Component

/**
 * Example of a custom component
 */
class CustomComponentExample(context: Context, customParam : String) : Component(context){
    override fun getType(): ComponentType {
        return ComponentType.CUSTOM
    }

    override fun enableInternal() {
        //add code here
    }

    override fun disableInternal() {
        //Destroy or disable code
    }
}