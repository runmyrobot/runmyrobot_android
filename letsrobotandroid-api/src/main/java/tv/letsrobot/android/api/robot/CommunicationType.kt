package tv.letsrobot.android.api.robot

import tv.letsrobot.android.api.interfaces.CommunicationInterface
import tv.letsrobot.android.api.robot.communications.BluetoothClassicCommunication
import tv.letsrobot.android.api.robot.communications.FelhrUsbSerialCommunication

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