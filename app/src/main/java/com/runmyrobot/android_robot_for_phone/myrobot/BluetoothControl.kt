package com.runmyrobot.android_robot_for_phone.myrobot

import android.content.Context
import android.os.Message
import android.util.Log
import com.runmyrobot.android_robot_for_phone.api.Component
import com.runmyrobot.android_robot_for_phone.control.EventManager
import com.runmyrobot.android_robot_for_phone.control.EventManager.Companion.STOP_EVENT
import com.runmyrobot.android_robot_for_phone.control.communicationInterfaces.BluetoothClassic
import com.runmyrobot.android_robot_for_phone.utils.SabertoothDriverUtil
import com.runmyrobot.android_robot_for_phone.utils.StoreUtil

/**
 * Created by Brendon on 9/5/2018.
 */
@Deprecated("Uses old control system. This class will only work with bluetooth")
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
        EventManager.subscribe(EventManager.COMMAND, onCommand)
        EventManager.subscribe(STOP_EVENT, onStop)
    }

    override fun disable() {
        super.disable()
        bluetoothClassic.serviceHandler.sendEmptyMessage(BluetoothClassic.DISCONNECT_MESSAGE)
        EventManager.unsubscribe(EventManager.COMMAND, onCommand)
        EventManager.unsubscribe(STOP_EVENT, onStop)
    }

    fun sendData(array: ByteArray){
        bluetoothClassic.serviceHandler.sendMessage(Message.obtain().also {
            it.obj = array
            it.what = BluetoothClassic.SEND_MESSAGE
        })
    }

    private val onCommand: (Any?) -> Unit = {
        it?.takeIf { it is String }?.let{
            when(it as String){
                "F" -> {onForward()}
                "B" -> {onBack()}
                "L" -> {onLeft()}
                "R" -> {onRight()}
            }
        }
    }

    private fun onForward() {
        Log.d(TAG, "onForward")
        val data = ByteArray(2)
        data[0] = SabertoothDriverUtil.getDriveSpeed(motorForwardSpeed, 0)
        data[1] = SabertoothDriverUtil.getDriveSpeed(motorForwardSpeed, 1)
        sendData(data)
    }

    private fun onBack() {
        Log.d(TAG, "onBack")
        val data = ByteArray(2)
        data[0] = SabertoothDriverUtil.getDriveSpeed(motorBackwardSpeed, 0)
        data[1] = SabertoothDriverUtil.getDriveSpeed(motorBackwardSpeed, 1)
        sendData(data)
    }

    private fun onLeft() {
        Log.d(TAG, "onLeft")
        val data = ByteArray(2)
        data[0] = SabertoothDriverUtil.getDriveSpeed(motorForwardSpeed, 0)
        data[1] = SabertoothDriverUtil.getDriveSpeed(motorBackwardSpeed, 1)
        sendData(data)
    }

    private fun onRight() {
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