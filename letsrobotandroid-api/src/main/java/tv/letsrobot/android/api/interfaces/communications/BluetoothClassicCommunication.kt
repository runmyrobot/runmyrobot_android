package tv.letsrobot.android.api.interfaces.communications

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Message
import android.util.Log
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.EventManager.Companion.ROBOT_BYTE_ARRAY
import tv.letsrobot.android.api.activities.ChooseBluetoothActivity
import tv.letsrobot.android.api.drivers.BluetoothClassic
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

    override fun clearSetup(activity: Activity) {
        activity.applicationContext.getSharedPreferences(CONFIG_PREFS, 0).edit().clear().apply()
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
        return bluetoothClassic?.BTStatus == BluetoothClassic.CONNECTION_STABLE
        //return BTStatus == BluetoothClassic.CONNECTION_STABLE;
    }

    override fun send(byteArray: ByteArray): Boolean {
        Log.d("Bluetooth", "message out = $byteArray")
        val message = Message.obtain()
        message.obj = byteArray
        message.what = BluetoothClassic.SEND_MESSAGE
        return isConnected() && bluetoothClassic?.serviceHandler?.sendMessage(message) ?: false
    }

    override fun initConnection(context: Context) {
        Log.d("Bluetooth","initConnection")
        bluetoothClassic = BluetoothClassic(context)
        addr = context.getSharedPreferences(CONFIG_PREFS, 0).getString(BLUETOOTH_ADDR, null)
    }

    override fun enable() {
        Log.d("Bluetooth","enable")
        bluetoothClassic?.ensurePoweredOn()
        bluetoothClassic?.connect(addr)
        EventManager.subscribe(ROBOT_BYTE_ARRAY, onControlEvent)
    }

    private val onControlEvent: (Any?) -> Unit = {
        Log.d("Bluetooth","onControlEvent")
        it?.takeIf { it is ByteArray }?.let{ data ->
            bluetoothClassic?.serviceHandler?.sendMessage(Message.obtain().also {
                it.obj = data as ByteArray
                it.what = BluetoothClassic.SEND_MESSAGE
            })
            Log.d("Bluetooth","onControlEvent sent")
        }
    }

    override fun disable() {
        Log.d("Bluetooth","disable")
        bluetoothClassic?.serviceHandler?.sendEmptyMessage(BluetoothClassic.DISCONNECT_MESSAGE)
        EventManager.unsubscribe(ROBOT_BYTE_ARRAY, onControlEvent)
    }

    override fun getStatus(): ComponentStatus {
        bluetoothClassic?.BTStatus?.let{
            return when(it){
                BluetoothClassic.CONNECTION_STABLE -> ComponentStatus.STABLE
                BluetoothClassic.CONNECTING -> ComponentStatus.CONNECTING
                BluetoothClassic.CONNECTION_LOST -> ComponentStatus.ERROR
                BluetoothClassic.CONNECTION_NON_EXISTENT -> ComponentStatus.DISABLED
                BluetoothClassic.CONNECTION_NOT_POSSIBLE -> ComponentStatus.ERROR
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
