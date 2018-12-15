package tv.letsrobot.android.api.drivers.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.HandlerThread
import tv.letsrobot.android.api.drivers.bluetooth.Connection.STATE_CONNECTED
import tv.letsrobot.android.api.drivers.bluetooth.Connection.STATE_CONNECTING
import tv.letsrobot.android.api.drivers.bluetooth.Connection.STATE_DISCONNECTED
import tv.letsrobot.android.api.drivers.bluetooth.Connection.STATE_ERROR
import tv.letsrobot.android.api.drivers.bluetooth.Connection.STATE_IDLE
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.random.Random

/**
 * Created by Brendon on 12/15/2018.
 */
internal class BluetoothClassicHandler {
    private var btAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var selectedDevice : BluetoothDevice? = null

    private var mmInStream: InputStream? = null
    private var mmOutStream: OutputStream? = null
    private var socket: BluetoothSocket? = null
    private var buffer = ByteArray(66)  // buffer store for the stream

    @Connection.Status
    var status = STATE_IDLE

    private var messageListener: ((ByteArray) -> Unit)? = null
    private var stateListener: ((Int) -> Unit)? = null

    private val handlerThread = HandlerThread("Bluetooth ${Random.nextInt()}").also {
        it.start()
    }
    internal val serviceHandler = Handler(handlerThread.looper){
        when(it.what){
            SEND_MESSAGE -> tryWriteBytes(it.obj as ByteArray)
            REQUEST_CONNECT -> tryConnect(it.obj as String)
            REQUEST_DISCONNECT -> tryDisconnect()
            HANDLE_LOOP -> handleLoop()
        }
        true
    }

    private fun tryPublishState(@Connection.Status state : Int) {
        if(status == state) return
        stateListener?.invoke(status)
    }

    var errCount = 0
    private fun handleLoop() {
        if(status != STATE_CONNECTED) return
        try {
            //we are expecting mmInStream not to be null, so let it catch that error when it is null
            mmInStream!!.let {
                // Read from the InputStream
                if(it.available() > 0)
                    it.read(buffer)
                parseMessage(buffer)
                errCount = 0
            }
        } catch (e: Exception) {
            errCount += 1
            if (errCount > 50) {
                enqueueDisconnect()
            }
        }
        serviceHandler.sendEmptyMessage(HANDLE_LOOP)
    }

    private fun parseMessage(buffer: ByteArray) {
        //TODO read incoming messages
    }

    private fun tryConnect(address : String){
        tryPublishState(STATE_CONNECTING)
        if (!btAdapter.isEnabled) {
            //TODO ASK INSTEAD OF ENABLING
            if(!btAdapter.enable()){
                tryPublishState(STATE_ERROR)
                return
            }
        }
        btAdapter.cancelDiscovery()
        selectedDevice = btAdapter.getRemoteDevice(address)
        socket = selectedDevice?.createRfcommSocketToServiceRecord(MY_UUID)
        socket?.let {
            try {
                handleConnect(it)
            } catch (e: Exception) {
                e.printStackTrace()
                tryPublishState(STATE_ERROR)
                try {
                    it.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } ?: tryPublishState(STATE_ERROR)
    }

    @Throws(IOException::class)
    private fun handleConnect(socket: BluetoothSocket) {
        socket.connect()
        mmInStream = socket.inputStream
        mmOutStream = socket.outputStream
        tryPublishState(STATE_CONNECTED)
        serviceHandler.obtainMessage(HANDLE_LOOP).sendToTarget()
    }

    private fun tryDisconnect(){
        selectedDevice = null
        try {
            socket?.close()
        } catch (e: Exception) {
            //assume disconnect went fine
        }
        tryPublishState(STATE_DISCONNECTED)
    }

    /**
     * Attempt to write bytes. Failure points are if we are not connected
     */
    private fun tryWriteBytes(value : ByteArray) {
        if(status != STATE_CONNECTED || mmOutStream == null) return
        try {
            mmOutStream?.write(value)
        } catch (e: Exception) {
            enqueueDisconnect()
        }
    }

    fun enqueueDisconnect(){
        serviceHandler.sendMessageAtFrontOfQueue(serviceHandler.obtainMessage(REQUEST_DISCONNECT))
    }

    fun onMessage(function : (bytes:ByteArray)->Unit){
        messageListener = function
    }

    fun onStateChange(function: (state: Int) -> Unit){
        stateListener = function
    }



    companion object {
        private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        internal const val SEND_MESSAGE = 0
        internal const val REQUEST_CONNECT = 1
        internal const val REQUEST_DISCONNECT = 2
        internal const val HANDLE_LOOP = 3


    }
}