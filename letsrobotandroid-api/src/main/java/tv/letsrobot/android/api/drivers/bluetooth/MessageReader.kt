package tv.letsrobot.android.api.drivers.bluetooth

import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Read the input stream
 */
class MessageReader(val inputStream: InputStream?){
    val data = ArrayDeque<ByteArray>()

    private val _error = AtomicBoolean(false)
    val error : Boolean
        get() {
            return _error.get()
        }

    val thread = Thread{
        while (!Thread.currentThread().isInterrupted){
            tryBlockingRead()
        }
    }

    fun startMonitor(){
        thread.start()
    }

    fun stopMonitor(){
        thread.interrupt()
    }

    @Synchronized
    fun next() : ByteArray{
        return data.poll()
    }

    @Synchronized
    fun put(array: ByteArray){
        data.offer(array)
    }

    private fun tryBlockingRead() : Int{
        val array = ByteArray(64)
        var bytesRead = -1
        try {
            bytesRead = inputStream?.read(array) ?: -1
            put(array)
        } catch (e: IOException) {
            //stream closed
        }
        return bytesRead
    }
}
