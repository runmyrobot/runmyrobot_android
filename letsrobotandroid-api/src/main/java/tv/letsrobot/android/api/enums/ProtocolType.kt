package tv.letsrobot.android.api.enums

import android.content.Context
import tv.letsrobot.android.api.components.ControlComponent
import tv.letsrobot.android.api.interfaces.protocols.ArduinoSendBytesProtocol
import tv.letsrobot.android.api.interfaces.protocols.SingleByteProtocol

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

    fun getInstantiatedClass(context: Context) : ControlComponent {
        return when(this){
            ArduinoRaw -> ArduinoSendBytesProtocol(context)
            SingleByte -> SingleByteProtocol(context)
        }
    }
}