package tv.letsrobot.android.api.interfaces.protocols

import android.content.Context
import android.util.Log
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.components.ControlComponent
import java.nio.charset.Charset

/**
 * Sends the first character of desired command without sending /r/n
 *
 * Added in case somebody runs into a board that cannot properly handle parsing a multi character command
 */
class ArduinoSendSingleCharProtocol(context: Context) :
        ControlComponent(context) {

    override fun enableInternal(){
        Log.d(TAG, "enable")
        EventManager.subscribe(EventManager.COMMAND, onCommand)
        EventManager.subscribe(EventManager.STOP_EVENT, onStop)
    }

    override fun disableInternal(){
        Log.d(TAG, "disable")
        EventManager.unsubscribe(EventManager.COMMAND, onCommand)
        EventManager.unsubscribe(EventManager.STOP_EVENT, onStop)
    }

    override fun timeout() {
        super.timeout()
        onStop(null)
    }

    private val onCommand: (Any?) -> Unit = {
        it?.takeIf { it is String}?.let{
            sendByte(it as String)
        }
    }

    private val onStop : (Any?) -> Unit  = {
        Log.d(TAG, "onStop")
        sendByte("s")
    }

    private fun sendByte(string : String){
        Log.d("Arduino", "message = ${string[0]}")
        sendToDevice("${string[0]}".toLowerCase().toByteArray(Charset.forName("UTF-8")))
    }

    companion object {
        const val TAG = "ArduinoProtocolSingleCh"
    }
}