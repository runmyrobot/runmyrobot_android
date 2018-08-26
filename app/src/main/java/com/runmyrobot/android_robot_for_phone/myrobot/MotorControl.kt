package com.runmyrobot.android_robot_for_phone.myrobot

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.runmyrobot.android_robot_for_phone.api.Component
import com.runmyrobot.android_robot_for_phone.control.ControllerMessageManager
import com.runmyrobot.android_robot_for_phone.control.UsbService
import java.lang.ref.WeakReference

/**
 * Designed to run on a SaberTooth Serial Motor controller via USB Serial
 * Created by Brendon on 8/26/2018.
 */
class MotorControl(context: Context) : Component(context) {
    val TAG = "MotorControl"
    override fun enable() {
        super.enable()
        Log.d(TAG, "enable")
        ControllerMessageManager.subscribe("F", onForward)
        ControllerMessageManager.subscribe("B", onBack)
        ControllerMessageManager.subscribe("L", onLeft)
        ControllerMessageManager.subscribe("R", onRight)
        ControllerMessageManager.subscribe("stop", onStop)
        setFilters()
        startService(UsbService::class.java, usbConnection, null) // Start UsbService(if it was not started before) and Bind it
    }

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
            usbService = (arg1 as UsbService.UsbBinder).getService()
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

    override fun disable() {
        super.disable()
        Log.d(TAG, "disable")
        ControllerMessageManager.unsubscribe("F", onForward)
        ControllerMessageManager.unsubscribe("B", onBack)
        ControllerMessageManager.unsubscribe("L", onLeft)
        ControllerMessageManager.unsubscribe("R", onRight)
        ControllerMessageManager.unsubscribe("stop", onStop)
    }

    private val onForward: (Any?) -> Unit = {
        Log.d(TAG, "onForward")
        val data = ByteArray(2)
        data[0] = 255.toByte()
        data[1] = 127.toByte()
        usbService?.write(data)
    }

    private val onBack: (Any?) -> Unit = {
        Log.d(TAG, "onBack")
        val data = ByteArray(2)
        data[0] = 128.toByte()
        data[1] = 1.toByte()
        usbService?.write(data)
    }

    private val onLeft: (Any?) -> Unit = {
        Log.d(TAG, "onLeft")
        val data = ByteArray(2)
        data[0] = 128.toByte()
        data[1] = 127.toByte()
        usbService?.write(data)
    }

    private val onRight: (Any?) -> Unit = {
        Log.d(TAG, "onRight")
        val data = ByteArray(2)
        data[0] = 255.toByte()
        data[1] = 1.toByte()
        usbService?.write(data)
    }

    private val onStop : (Any?) -> Unit  = {
        Log.d(TAG, "onStop")
        val data = ByteArray(1)
        data[0] = 0x00
        usbService?.write(data)
    }

    override fun timeout() {
        onStop(null)
    }
}
