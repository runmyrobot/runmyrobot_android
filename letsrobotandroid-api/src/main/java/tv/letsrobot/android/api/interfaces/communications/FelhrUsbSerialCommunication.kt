package tv.letsrobot.android.api.interfaces.communications

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import android.widget.Toast
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.drivers.UsbService
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.interfaces.CommunicationInterface
import java.lang.ref.WeakReference

/**
 * This is the communication component that interfaces with USBService.class (com.felhr.usbserial)
 */
class FelhrUsbSerialCommunication : CommunicationInterface {
    private val TAG = "FelhrUsb"
    private var componentStatus = ComponentStatus.DISABLED

    override fun initConnection(context: Context) {
        setFilters(context)
        startService(context, UsbService::class.java, usbConnection, null) // Start UsbService(if it was not started before) and Bind it
    }

    override fun enable() {
        Log.d(TAG, "enable")
        EventManager.subscribe(EventManager.ROBOT_BYTE_ARRAY, onSendRobotCommand)
    }

    override fun disable() {
        Log.d(TAG, "disable")
        EventManager.unsubscribe(EventManager.ROBOT_BYTE_ARRAY, onSendRobotCommand)
    }

    override fun clearSetup(context: Context) {
        //not used for USB
    }

    override fun needsSetup(activity: Activity): Boolean {
        return false
    }

    override fun setupComponent(activity: Activity): Int {
        return -1
    }

    override fun receivedComponentSetupDetails(context: Context, intent: Intent?) {
        //ignore
    }

    override fun isConnected(): Boolean {
        return true
    }

    override fun send(byteArray: ByteArray): Boolean {
        usbService?.write(byteArray) //TODO actually get result?
        return true
    }

    private val onSendRobotCommand: (Any?) -> Unit = {
        Log.d(TAG, "onSendRobotCommand")
        it?.takeIf { it is ByteArray }?.let{ data ->
            send(data as ByteArray)
        }
    }

    override fun getStatus(): ComponentStatus {
        return componentStatus
    }

    override fun getAutoReboot(): Boolean {
        return false //Auto reboot useless, as we have to wait for something to connect to us
    }

    //Below is all USB Service code from com.felhr.usbservice
    //https://github.com/felHR85/UsbSerial/

    private fun startService(context: Context, service: Class<*>, serviceConnection: ServiceConnection, extras: Bundle?) {
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
    private fun setFilters(context: Context) {
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
                -> {
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show()
                    componentStatus = ComponentStatus.STABLE
                }
                UsbService.ACTION_USB_PERMISSION_NOT_GRANTED // USB PERMISSION NOT GRANTED
                -> {
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show()
                    componentStatus = ComponentStatus.ERROR
                }
                UsbService.ACTION_NO_USB // NO USB CONNECTED
                -> {
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show()
                    //componentStatus = ComponentStatus.DISABLED
                }
                UsbService.ACTION_USB_DISCONNECTED // USB DISCONNECTED
                -> {
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show()
                    componentStatus = ComponentStatus.ERROR
                }
                UsbService.ACTION_USB_NOT_SUPPORTED // USB NOT SUPPORTED
                -> {
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show()
                    componentStatus = ComponentStatus.ERROR
                }
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