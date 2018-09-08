package com.runmyrobot.android_robot_for_phone.myrobot

import android.content.Context
import android.os.Message
import android.util.Log
import com.runmyrobot.android_robot_for_phone.api.Component
import com.runmyrobot.android_robot_for_phone.control.BluetoothClassic
import com.runmyrobot.android_robot_for_phone.control.ControllerMessageManager
import com.runmyrobot.android_robot_for_phone.utils.SabertoothDriverUtil
import com.runmyrobot.android_robot_for_phone.utils.StoreUtil

/**
 * Created by Brendon on 9/5/2018.
 */
class BluetoothControl(context: Context) : Component(context) {
    val TAG = "BluetoothControl"
    val bluetoothClassic = BluetoothClassic(context)
    val address = StoreUtil.GetBluetoothDevice(context)
    private val motorForwardSpeed = 50.toByte()
    private val motorBackwardSpeed = (-50).toByte()

    override fun enable() {
        super.enable()
        address?.second?.let {
            bluetoothClassic.connect(address.second)
        }
        ControllerMessageManager.subscribe("F", onForward)
        ControllerMessageManager.subscribe("B", onBack)
        ControllerMessageManager.subscribe("L", onLeft)
        ControllerMessageManager.subscribe("R", onRight)
        ControllerMessageManager.subscribe("stop", onStop)
    }

    override fun disable() {
        super.disable()
        bluetoothClassic.serviceHandler.sendEmptyMessage(bluetoothClassic.DISCONNECT_MESSAGE)
        ControllerMessageManager.unsubscribe("F", onForward)
        ControllerMessageManager.unsubscribe("B", onBack)
        ControllerMessageManager.unsubscribe("L", onLeft)
        ControllerMessageManager.unsubscribe("R", onRight)
        ControllerMessageManager.unsubscribe("stop", onStop)
    }

    fun sendData(array: ByteArray){
        bluetoothClassic.serviceHandler.sendMessage(Message.obtain().also {
            it.obj = array
            it.what = bluetoothClassic.SEND_MESSAGE
        })
    }

    private val onForward: (Any?) -> Unit = {
        Log.d(TAG, "onForward")
        val data = ByteArray(2)
        data[0] = SabertoothDriverUtil.getDriveSpeed(motorForwardSpeed, 0)
        data[1] = SabertoothDriverUtil.getDriveSpeed(motorForwardSpeed, 1)
        sendData(data)
    }

    private val onBack: (Any?) -> Unit = {
        Log.d(TAG, "onBack")
        val data = ByteArray(2)
        data[0] = SabertoothDriverUtil.getDriveSpeed(motorBackwardSpeed, 0)
        data[1] = SabertoothDriverUtil.getDriveSpeed(motorBackwardSpeed, 1)
        sendData(data)
    }

    private val onLeft: (Any?) -> Unit = {
        Log.d(TAG, "onLeft")
        val data = ByteArray(2)
        data[0] = SabertoothDriverUtil.getDriveSpeed(motorForwardSpeed, 0)
        data[1] = SabertoothDriverUtil.getDriveSpeed(motorBackwardSpeed, 1)
        sendData(data)
    }

    private val onRight: (Any?) -> Unit = {
        Log.d(TAG, "onRight")
        val data = ByteArray(2)
        data[0] = SabertoothDriverUtil.getDriveSpeed(motorBackwardSpeed, 0)
        data[1] = SabertoothDriverUtil.getDriveSpeed(motorForwardSpeed, 1)
        sendData(data)
    }

    private val onStop : (Any?) -> Unit  = {
        Log.d(TAG, "onStop")
        val data = ByteArray(1)
        data[0] = 0x00
        sendData(data)
    }

    /**
     * Timeout function. Will be called if we have not received anything recently,
     * in case a movement command gets stuck
     */
    override fun timeout() {
        onStop(null)
    }
}