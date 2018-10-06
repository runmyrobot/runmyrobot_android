package tv.letsrobot.android.api.interfaces.protocols

import android.content.Context
import android.util.Log
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.components.ControlComponent
import java.nio.charset.Charset

/**
 * Created by Brendon on 9/9/2018.
 */
class ArduinoSendBytesProtocol(context: Context) :
        ControlComponent(context) {

    override fun enable() : Boolean{
        if(!super.enable()) return false
        Log.d(TAG, "enable")
        EventManager.subscribe(EventManager.COMMAND, onCommand)
        EventManager.subscribe(EventManager.STOP_EVENT, onStop)
        return true
    }

    override fun disable() : Boolean{
        if(!super.disable()) return false
        Log.d(TAG, "disable")
        EventManager.subscribe(EventManager.COMMAND, onCommand)
        EventManager.subscribe(EventManager.STOP_EVENT, onStop)
        return true
    }

    override fun timeout() {
        super.timeout()
        onStop(null)
    }

    private val onCommand: (Any?) -> Unit = {
        it?.takeIf { it is String}?.let{
            sendBytesWithTerminator(it as String)
        }
    }

    private val onStop : (Any?) -> Unit  = {
        Log.d(TAG, "onStop")
        val data = ByteArray(1)
        data[0] = 0x00
        sendBytesWithTerminator("stop")
    }

    private fun sendBytesWithTerminator(string : String){
        val messageWithTerminator = "$string\r\n"
        Log.d("Arduino", "message = $messageWithTerminator")
        sendToDevice(messageWithTerminator.toLowerCase().toByteArray(Charset.forName("UTF-8")))
    }

    companion object {
        const val TAG = "ArduinoProtocol"
    }
}