package tv.letsrobot.android.api.drivers.bluetooth
import tv.letsrobot.android.api.drivers.bluetooth.BluetoothClassicHandler.Companion.SEND_MESSAGE

/**
 * Bluetooth class to handle classic bluetooth (serial) connections
 */
class BluetoothClassic(val address : String){
    private val bluetoothService = BluetoothClassicHandler()

    @Connection.Status
    val status : Int
        get() = bluetoothService.status

    /**
     * Connect to given device
     */
    fun connect(){
        bluetoothService.serviceHandler
                .obtainMessage(BluetoothClassicHandler.REQUEST_CONNECT, address).sendToTarget()
    }

    /**
     * Disconnect from given device
     */
    fun disconnect(){
        bluetoothService.serviceHandler.sendEmptyMessage(BluetoothClassicHandler.REQUEST_DISCONNECT)
    }

    /**
     * Send ByteArray to bluetooth processing
     */
    fun writeBytes(bytes : ByteArray){
        bluetoothService.serviceHandler.obtainMessage(SEND_MESSAGE, bytes).sendToTarget()
    }

    /**
     * Lambda for receiving messages
     */
    fun onMessage(function : (bytes:ByteArray)->Unit){
        bluetoothService.onMessage(function)
    }

    /**
     * Lambda for state changes
     */
    fun onStateChange(function: (state: Int) -> Unit){
        bluetoothService.onStateChange(function)
    }

    companion object {
        private const val TAG = "BluetoothClassic"
    }
}
