package com.runmyrobot.android_robot_for_phone.control

import android.util.Log

/**
 * Created by Brendon on 8/26/2018.
 */
class ControllerMessageManager{
    companion object {
        private val subscribers = HashMap<String, ArrayList<(Any?) -> Unit>>()

        fun subscribe(event : String, listener: (Any?) -> Unit){
            var list = subscribers[event]
            if(list == null)
                list = ArrayList()
            if(!list.contains(listener))
                list.add(listener)
            subscribers[event] = list
        }

        fun unsubscribe(event : String, listener: (Any?) -> Unit){
            subscribers[event]?.remove(listener)
        }

        fun invoke(event: String, message : Any? = null){
            val list = subscribers[event]
            Log.d("invoke", event)
            list?.let {
                it.forEach {
                    it(message)
                }
            }
        }
    }
}
