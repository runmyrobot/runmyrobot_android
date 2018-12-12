package tv.letsrobot.android.api.interfaces.protocols

import android.content.Context
import android.util.Log
import com.google.common.primitives.Bytes.concat
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.components.ControlComponent

/**
 * Created by Brendon on 12/11/2018.
 */
class NXTJoystickDriverProtocol(context: Context) : ControlComponent(context) {
    override fun enableInternal(){
        Log.d(TAG, "enable")
        EventManager.subscribe(EventManager.COMMAND, onCommand)
        EventManager.subscribe(EventManager.STOP_EVENT, onStop)
    }

    override fun disableInternal(){
        Log.d(TAG, "disable")
        EventManager.unsubscribe(EventManager.COMMAND, onCommand)
        EventManager.unsubscribe(EventManager.STOP_EVENT, onStop)
    }

    override fun timeout() {
        super.timeout()
        onStop(null)
    }

    private val onCommand: (Any?) -> Unit = {
        it?.takeIf { it is String}?.let{ command ->
            val joy1 = Joystick()
            when(command){
                "F" -> {
                    joy1.topHat = 0.toByte()
                }
                "B" -> {
                    joy1.topHat = 4.toByte()
                }
                "R" -> {
                    joy1.topHat = 2.toByte()
                }
                "L" -> {
                    joy1.topHat = 6.toByte()
                }
                else -> {
                    joy1.topHat = (-1).toByte()
                }
            }
            sendToDevice(getPacket(joy1))
        }
    }

    private val onStop : (Any?) -> Unit  = {
        Log.d(TAG, "onStop")
        sendToDevice(getPacket()) //sends the default packet
    }

    class Joystick{
        var X1 : Byte = 0x00
        var X2 : Byte = 0x00
        var Y1 : Byte = 0x00
        var Y2 : Byte = 0x00
        var buttons = ByteArray(2)
        var topHat : Byte = (-1).toByte()
    }

    fun getPacket(joy1 : Joystick = Joystick(), joy2 : Joystick = Joystick()) : ByteArray{
        var bytePackage = byteArrayOf(0x00.toByte(),
                0x09,
                0x00,
                0x12,
                0x00,
                //start of joystick driver packet
                0x01, //true
                0x00, //false
                joy1.X1, joy1.Y1, //stick 1
                joy1.X2, joy1.Y2, //stick 2
                joy1.buttons[0],
                joy1.buttons[1],
                joy1.topHat,
                joy2.X1, joy2.Y1, //stick 1
                joy2.X2, joy2.Y2, //stick 2
                joy2.buttons[0],
                joy2.buttons[1],
                joy2.topHat,
                0x00)
        val bluetoothPacket = byteArrayOf(0x16, 0x00) // TODO make it find out size 22 ¯\_(ツ)_/¯
        bytePackage = concat(bluetoothPacket, bytePackage)
        return bytePackage
    }

    companion object {
        const val TAG = "NXTProtocol"
    }
}