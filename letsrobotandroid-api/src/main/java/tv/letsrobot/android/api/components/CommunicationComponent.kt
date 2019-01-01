package tv.letsrobot.android.api.components

import android.content.Context
import android.os.Handler
import android.os.Looper
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.enums.ComponentType
import tv.letsrobot.android.api.interfaces.CommunicationInterface
import tv.letsrobot.android.api.interfaces.Component
import tv.letsrobot.android.api.interfaces.ComponentEventObject

/**
 * Main Communication Component that holds a reference to CommunicationInterface and controls it
 */
class CommunicationComponent(context: Context, val communicationInterface: CommunicationInterface) : Component(context) , Runnable{
    override fun getType(): ComponentType {
        return ComponentType.CONTROL_DRIVER
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

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        if(message.type == ComponentType.CONTROL_TRANSLATOR){
            when(message.what){
                EVENT_MAIN -> {
                    communicationInterface.send(message.data as ByteArray)
                    return true
                }
            }
        }
        return super.handleExternalMessage(message)
    }
}
