package tv.letsrobot.android.api.control.deviceProtocols

import android.content.Context
import tv.letsrobot.android.api.api.ControlComponent

/**
 * Protocol types will reside in here
 */
enum class ProtocolType {
    /**
     * Sends raw commands to Arduino, this will appear in the form of 'f', 'b', 'stop'
     */
    ArduinoRaw,
    /**
     * Single byte control. Can control SaberTooth devices through simplified mode or other devices
     */
    SingleByte;

    fun getInstantiatedClass(context: Context) : ControlComponent{
        return when(this){
            ArduinoRaw -> ArduinoSendBytesProtocol(context)
            SingleByte -> SingleByteProtocol(context)
        }
    }
}