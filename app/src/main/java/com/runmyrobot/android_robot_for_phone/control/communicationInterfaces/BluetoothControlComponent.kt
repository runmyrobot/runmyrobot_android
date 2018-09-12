package com.runmyrobot.android_robot_for_phone.control.communicationInterfaces

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Message
import com.runmyrobot.android_robot_for_phone.activities.ChooseBluetoothActivity
import com.runmyrobot.android_robot_for_phone.control.CommunicationInterface
import com.runmyrobot.android_robot_for_phone.control.EventManager
import com.runmyrobot.android_robot_for_phone.control.EventManager.Companion.ROBOT_BYTE_ARRAY

/**
 * Created by Brendon on 9/11/2018.
 */
class BluetoothControlComponent : CommunicationInterface {
    var bluetoothClassic : BluetoothClassic? = null
    var addr : String? = null
    var name : String? = null
    override fun needsSetup(activity: Activity): Boolean {
        return !activity.applicationContext.getSharedPreferences(CONFIG_PREFS, 0).contains(BLUETOOTH_ADDR)
    }

    override fun setupComponent(activity: Activity): Int {
        activity.startActivityForResult(
                Intent(activity, ChooseBluetoothActivity::class.java), RESULT_CODE)
        return RESULT_CODE
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
        return false
        //return BTStatus == BluetoothClassic.CONNECTION_STABLE;
    }

    override fun send(byteArray: ByteArray): Boolean {
        val message = Message.obtain()
        message.obj = byteArray
        message.what = BluetoothClassic.SEND_MESSAGE
        return isConnected() && bluetoothClassic?.serviceHandler?.sendMessage(message) ?: false
    }

    override fun initConnection(context: Context) {
        bluetoothClassic = BluetoothClassic(context)
        addr = context.getSharedPreferences(CONFIG_PREFS, 0).getString(BLUETOOTH_ADDR, null)
    }

    override fun enable() {
        bluetoothClassic?.connect(addr)
        EventManager.subscribe(ROBOT_BYTE_ARRAY, onControlEvent)
    }

    private val onControlEvent: (Any?) -> Unit = {
        it?.takeIf { it is ByteArray }?.let{ data ->
            bluetoothClassic?.serviceHandler?.sendMessage(Message.obtain().also {
                it.obj = data as ByteArray
                it.what = BluetoothClassic.SEND_MESSAGE
            })
        }
    }

    override fun disable() {
        bluetoothClassic?.serviceHandler?.sendEmptyMessage(BluetoothClassic.DISCONNECT_MESSAGE)
        EventManager.unsubscribe(ROBOT_BYTE_ARRAY, onControlEvent)
    }

    companion object {
        val BLUETOOTH_ADDR = "addr"
        val BLUETOOTH_NAME = "name"
        val CONFIG_PREFS = "BluetoothClassicConfig"
        val RESULT_CODE = 312
    }
}
