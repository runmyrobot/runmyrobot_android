package tv.letsrobot.android.api.control.communicationInterfaces

import tv.letsrobot.android.api.api.CommunicationInterface

/**
 * Communication types will reside in here
 */
enum class CommunicationType {
    BluetoothClassic,
    UsbSerial;

    val getInstantiatedClass : CommunicationInterface?
        get() = when(this){
            CommunicationType.BluetoothClassic -> BluetoothClassicCommunication()
            CommunicationType.UsbSerial -> FelhrUsbSerialCommunication()
        }
}