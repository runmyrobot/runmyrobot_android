package tv.letsrobot.android.api.interfaces.communications

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.util.Log
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.EventManager.Companion.ROBOT_BYTE_ARRAY
import tv.letsrobot.android.api.activities.ChooseBluetoothActivity
import tv.letsrobot.android.api.drivers.bluetooth.BluetoothClassic
import tv.letsrobot.android.api.drivers.bluetooth.Connection
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.interfaces.CommunicationInterface

/**
 * Communication class that works with bluetooth classic
 * and takes control data via EventManager.ROBOT_BYTE_ARRAY event
 */
class BluetoothClassicCommunication : CommunicationInterface {
    var bluetoothClassic : BluetoothClassic? = null
    var addr : String? = null
    var name : String? = null

    override fun clearSetup(context: Context) {
        context.getSharedPreferences(CONFIG_PREFS, 0).edit().clear().apply()
    }

    override fun needsSetup(activity: Activity): Boolean {
        val pairingRequired = !activity.applicationContext.getSharedPreferences(CONFIG_PREFS, 0).contains(BLUETOOTH_ADDR)
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        //Return that additional setup is needed if no preferred device OR bluetooth is off
        return pairingRequired || !mBluetoothAdapter.isEnabled
    }

    override fun setupComponent(activity: Activity): Int {
        //Make sure we turn bluetooth on for setup
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(!mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.enable()
        }
        //Start an activity to select our preferred device
        val pairingRequired = !activity.applicationContext.getSharedPreferences(CONFIG_PREFS, 0).contains(BLUETOOTH_ADDR)
        if(pairingRequired) {
            activity.startActivityForResult(
                    Intent(activity, ChooseBluetoothActivity::class.java), RESULT_CODE)
            return RESULT_CODE
        }
        return -1
    }

    override fun receivedComponentSetupDetails(context: Context, intent: Intent?) {
        intent?.let {
            val addr = intent.extras.getString(ChooseBluetoothActivity.EXTRA_DEVICE_ADDRESS, null)
            val name = intent.extras.getString(ChooseBluetoothActivity.EXTRA_DEVICE_NAME, null)
            val prefsEdit = context.applicationContext.getSharedPreferences(CONFIG_PREFS, 0).edit()
            prefsEdit.putString(BLUETOOTH_ADDR, addr)
            prefsEdit.putString(BLUETOOTH_NAME, name)
            prefsEdit.apply()
        }
    }

    override fun isConnected(): Boolean {
        return bluetoothClassic?.status == Connection.STATE_CONNECTED
    }

    override fun send(byteArray: ByteArray): Boolean {
        Log.d("Bluetooth", "message out = $byteArray")
        bluetoothClassic?.writeBytes(byteArray)
        return true
    }

    override fun initConnection(context: Context) {
        Log.d("Bluetooth","initConnection")
        addr = context.getSharedPreferences(CONFIG_PREFS, 0).getString(BLUETOOTH_ADDR, null)
        addr?.let {
            bluetoothClassic = BluetoothClassic(it)
        } ?: throw Exception("No bluetooth address supplied!")
    }

    override fun enable() {
        Log.d("Bluetooth","enable")
        bluetoothClassic?.connect()
        EventManager.subscribe(ROBOT_BYTE_ARRAY, onControlEvent)
    }

    private val onControlEvent: (Any?) -> Unit = {
        Log.d("Bluetooth","onControlEvent")
        (it as? ByteArray)?.let{ data ->
            send(data)
            Log.d("Bluetooth","onControlEvent sent")
        }
    }

    override fun disable() {
        Log.d("Bluetooth","disable")
        bluetoothClassic?.disconnect()
        EventManager.unsubscribe(ROBOT_BYTE_ARRAY, onControlEvent)
    }

    override fun getStatus(): ComponentStatus {
        bluetoothClassic?.status?.let{
            return when(it){
                Connection.STATE_CONNECTED -> ComponentStatus.STABLE
                Connection.STATE_CONNECTING -> ComponentStatus.CONNECTING
                Connection.STATE_ERROR -> ComponentStatus.ERROR
                Connection.STATE_IDLE -> ComponentStatus.DISABLED
                Connection.STATE_DISCONNECTED -> ComponentStatus.ERROR
                else -> ComponentStatus.ERROR
            }
        }
        return ComponentStatus.DISABLED
    }

    override fun getAutoReboot(): Boolean {
        return true
    }

    companion object {
        val BLUETOOTH_ADDR = "addr"
        val BLUETOOTH_NAME = "name"
        val CONFIG_PREFS = "BluetoothClassicConfig"
        val RESULT_CODE = 312
    }
}
