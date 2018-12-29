package tv.letsrobot.android.api

import android.util.Log

/**
 * Event Manager for controls
 *
 * Subscribe to this to get raw input events from buttons on the LetsRobot platform
 *
 * For example, the default forward button outputs 'F', so we will use this code
 *
 * {
 *
 * ...
 *
 * <br>
 *
 * ``` EventManager.subscribe("F", onForward) ```
 * <br>
 *
 * ...
 *
 * }
 *
 * A more thorough example can be seen in SabertoothMotorProtocol.kt
 */
class EventManager{
    companion object {
        private val subscribers = HashMap<String, ArrayList<(Any?) -> Unit>>()
        private val globalListener = ArrayList<(String, Any?) -> Unit>()
        const val COMMAND: String = "command"
        const val TIMEOUT: String = "timeout"
        const val ROBOT_DISCONNECTED = "robot_disconnect"
        const val ROBOT_CONNECTED = "robot_connect"
        const val STOP_EVENT = "stop_event"
        const val ROBOT_BYTE_ARRAY = "robot_byte_array"
        const val CHAT = "chat"

        /**
         * Subscribe to a control
         */
        fun subscribe(event : String, listener: (Any?) -> Unit){
            var list = subscribers[event]
            if(list == null)
                list = ArrayList()
            if(!list.contains(listener))
                list.add(listener)
            subscribers[event] = list
        }

        fun subscribe(listener: (String, Any?) -> Unit){
            if(!globalListener.contains(listener))
                globalListener.add(listener)
        }

        fun unsubscribe(listener: (String, Any?) -> Unit){
            globalListener.remove(listener)
        }

        /**
         * unsubscribe from a control
         */
        fun unsubscribe(event : String, listener: (Any?) -> Unit){
            subscribers[event]?.remove(listener)
        }

        /**
         * Call a control event. Open to be called anywhere,
         * but mainly used by the ControlSocketComponent
         */
        fun invoke(event: String, message : Any? = null){
            val list = subscribers[event]
            Log.d("invoke", "$event : ${message.toString()}")
            list?.let {
                //Loop through subscribers
                it.forEach {
                    try {
                        it(message)
                    } catch (e: Exception) {
                    }
                }
            }
            globalListener.forEach{
                try {
                    it(event, message)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
