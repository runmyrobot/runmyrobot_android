package tv.letsrobot.android.api.enums

import tv.letsrobot.android.api.interfaces.CommunicationInterface
import tv.letsrobot.android.api.interfaces.communications.BluetoothClassicCommunication
import tv.letsrobot.android.api.interfaces.communications.FelhrUsbSerialCommunication

/**
 * Communication types will reside in here
 */
enum class CommunicationType {
    UsbSerial,
    BluetoothClassic;

    val getInstantiatedClass : CommunicationInterface?
        get() = when(this){
            BluetoothClassic -> BluetoothClassicCommunication()
            UsbSerial -> FelhrUsbSerialCommunication()
        }
}