package tv.letsrobot.android.api.components

import android.content.Context
import android.os.Handler
import android.os.Looper
import tv.letsrobot.android.api.Core
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.interfaces.CommunicationInterface
import tv.letsrobot.android.api.interfaces.Component

/**
 * Created by Brendon on 9/11/2018.
 */
class CommunicationComponent(context: Context, val communicationInterface: CommunicationInterface) : Component(context) , Runnable{
    val uiHandler = Handler(Looper.getMainLooper())
    init {
        communicationInterface.initConnection(context)
        uiHandler.post(this)
    }

    @Synchronized
    override fun enable() : Boolean {
        if(!super.enable()) return false
        communicationInterface.enable()
        return true
    }

    @Synchronized
    override fun disable() : Boolean{
        if(!super.disable()) return false
        communicationInterface.disable()
        return true
    }

    var errorCounter = 0

    override fun run() {
        if(enabled.get())
            status = communicationInterface.getStatus()
        if(communicationInterface.getAutoReboot()
                && communicationInterface.getStatus() == ComponentStatus.ERROR){
            errorCounter++
            if(errorCounter > 10){
                Core.handler?.post {
                    disable()
                    enable()
                }
                errorCounter = 0
            }
        }
        else{
            errorCounter = 0
        }
        uiHandler.postDelayed(this, 200)
    }
}
