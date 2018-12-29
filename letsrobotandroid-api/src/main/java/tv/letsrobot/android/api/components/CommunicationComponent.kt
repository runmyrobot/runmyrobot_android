package tv.letsrobot.android.api.components

import android.content.Context
import android.os.Handler
import android.os.Looper
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.interfaces.CommunicationInterface
import tv.letsrobot.android.api.interfaces.Component

/**
 * Main Communication Component that holds a reference to CommunicationInterface and controls it
 */
class CommunicationComponent(context: Context, val communicationInterface: CommunicationInterface) : Component(context) , Runnable{
    override fun getType(): Int {
        return Component.CONTROL_DRIVER
    }

    val uiHandler = Handler(Looper.getMainLooper())
    init {
        communicationInterface.initConnection(context)
        uiHandler.post(this)
    }

    @Synchronized
    override fun enableInternal(){
        communicationInterface.enable()
    }

    @Synchronized
    override fun disableInternal(){
        communicationInterface.disable()
    }

    var errorCounter = 0

    override fun run() {
        if(enabled.get())
            status = communicationInterface.getStatus()
        if(communicationInterface.getAutoReboot()
                && communicationInterface.getStatus() == ComponentStatus.ERROR){
            errorCounter++
            if(errorCounter > 10){
                reset()
                errorCounter = 0
            }
        }
        else{
            errorCounter = 0
        }
        uiHandler.postDelayed(this, 200)
    }
}
