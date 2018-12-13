package tv.letsrobot.android.api.interfaces.protocols

import android.content.Context
import android.util.Log
import tv.letsrobot.android.api.components.ControlComponent
import java.nio.charset.Charset

/**
 * Created by Brendon on 9/9/2018.
 */
class ArduinoSendBytesProtocol(context: Context) :
        ControlComponent(context) {

    override fun onStringCommand(command: String) {
        super.onStringCommand(command)
        sendBytesWithTerminator(command)
    }

    override fun onStop(any: Any?) {
        super.onStop(any)
        sendBytesWithTerminator("stop")
    }

    private fun sendBytesWithTerminator(string : String){
        val messageWithTerminator = "$string\r\n"
        Log.d(TAG, "message = $messageWithTerminator")
        sendToDevice(messageWithTerminator.toLowerCase().toByteArray(Charset.forName("UTF-8")))
    }

    companion object {
        const val TAG = "ArduinoProtocol"
    }
}