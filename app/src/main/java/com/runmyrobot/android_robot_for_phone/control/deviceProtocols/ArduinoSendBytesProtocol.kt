package com.runmyrobot.android_robot_for_phone.control.deviceProtocols

import android.content.Context
import android.util.Log
import com.runmyrobot.android_robot_for_phone.api.ControlComponent
import com.runmyrobot.android_robot_for_phone.control.EventManager
import java.nio.charset.Charset

/**
 * Created by Brendon on 9/9/2018.
 */
class ArduinoSendBytesProtocol(context: Context) :
        ControlComponent(context) {

    override fun enable() {
        super.enable()
        Log.d(TAG, "enable")
        EventManager.subscribe(EventManager.COMMAND, onCommand)
        EventManager.subscribe(EventManager.STOP_EVENT, onStop)
    }

    override fun disable() {
        super.disable()
        Log.d(TAG, "disable")
        EventManager.subscribe(EventManager.COMMAND, onCommand)
        EventManager.subscribe(EventManager.STOP_EVENT, onStop)
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
        sendToDevice(messageWithTerminator.toLowerCase().toByteArray(Charset.forName("UTF-8")))
    }

    companion object {
        const val TAG = "ArduinoProtocol"
    }
}