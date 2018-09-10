package com.runmyrobot.android_robot_for_phone.control.deviceProtocols

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.runmyrobot.android_robot_for_phone.api.ControlComponent
import com.runmyrobot.android_robot_for_phone.control.CommunicationInterface
import com.runmyrobot.android_robot_for_phone.control.ControllerMessageManager
import com.runmyrobot.android_robot_for_phone.control.communicationInterfaces.UsbService
import com.runmyrobot.android_robot_for_phone.utils.SabertoothDriverUtil
import java.lang.ref.WeakReference

/**
 * Sample Motor control component.
 *
 * Added to RobotComponentList to actually be registered with Core
 *
 * Designed to run on a SaberTooth Serial Motor controller via USB Serial
 *
 * Settings Used:
 * - 9600 Baud
 * - Simplified Serial
 * - 1 Motor controller
 * - 2 Motors
 * - DIP Configuration (Assumes Lead acid battery): From 1 to 6: 101011
 */
class SabertoothMotorProtocol(communicationInterface: CommunicationInterface, context: Context) :
        ControlComponent(communicationInterface, context) {
    private val TAG = "SabertoothMotorProtocol"
    private val motorForwardSpeed = 70.toByte() //scale (-127)-128
    private val motorBackwardSpeed = (-70).toByte() //scale (-127)-128
    override fun enable() {
        super.enable()
        Log.d(TAG, "enable")
        ControllerMessageManager.subscribe(ControllerMessageManager.COMMAND, onCommand)
        ControllerMessageManager.subscribe(ControllerMessageManager.STOP_EVENT, onStop)
        setFilters()
        startService(UsbService::class.java, usbConnection, null) // Start UsbService(if it was not started before) and Bind it
    }

    override fun disable() {
        super.disable()
        Log.d(TAG, "disable")
        ControllerMessageManager.unsubscribe(ControllerMessageManager.COMMAND, onCommand)
        ControllerMessageManager.unsubscribe(ControllerMessageManager.STOP_EVENT, onStop)
    }

    fun sendData(array: ByteArray){
        usbService?.write(array)
    }

    private val onCommand: (Any?) -> Unit = {
        it?.takeIf { it is String }?.let{
            //We only worry about the simple motor commands. We could add in half turns though
            when(it as String){
                "F" -> {onForward()}
                "B" -> {onBack()}
                "L" -> {onLeft()}
                "R" -> {onRight()}
                "stop" -> {onStop(null)}
                else -> {
                    //Control not supported
                }
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

    //Below is all USB Service code from com.felhr.usbservice
    //https://github.com/felHR85/UsbSerial/

    private fun startService(service: Class<*>, serviceConnection: ServiceConnection, extras: Bundle?) {
        if (!UsbService.SERVICE_CONNECTED) {
            val startService = Intent(context, service)
            if (extras != null && !extras.isEmpty) {
                val keys = extras.keySet()
                for (key in keys) {
                    val extra = extras.getString(key)
                    startService.putExtra(key, extra)
                }
            }
            context.startService(startService)
        }
        val bindingIntent = Intent(context, service)
        context.bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    //Some Intent filters for listening for USB events
    private fun setFilters() {
        val filter = IntentFilter()
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED)
        filter.addAction(UsbService.ACTION_NO_USB)
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED)
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED)
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED)
        context.registerReceiver(mUsbReceiver, filter)
    }

    /*
         * Notifications from UsbService will be received here.
         */
    private val mUsbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbService.ACTION_USB_PERMISSION_GRANTED // USB PERMISSION GRANTED
                -> Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show()
                UsbService.ACTION_USB_PERMISSION_NOT_GRANTED // USB PERMISSION NOT GRANTED
                -> Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show()
                UsbService.ACTION_NO_USB // NO USB CONNECTED
                -> Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show()
                UsbService.ACTION_USB_DISCONNECTED // USB DISCONNECTED
                -> Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show()
                UsbService.ACTION_USB_NOT_SUPPORTED // USB NOT SUPPORTED
                -> Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var usbService: UsbService? = null
    private var mHandler: MyHandler? = null
    private val usbConnection = object : ServiceConnection {
        override fun onServiceConnected(arg0: ComponentName, arg1: IBinder) {
            usbService = (arg1 as UsbService.UsbBinder).service
            usbService!!.setHandler(mHandler)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            usbService = null
        }
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private class MyHandler(activity: Context) : Handler() {
        private val mActivity: WeakReference<Context> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                UsbService.MESSAGE_FROM_SERIAL_PORT -> {
                    val data = msg.obj as String
                    Log.d("handleMessage", data)
                }
                UsbService.CTS_CHANGE -> Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show()
                UsbService.DSR_CHANGE -> Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show()
            }
        }
    }
}
