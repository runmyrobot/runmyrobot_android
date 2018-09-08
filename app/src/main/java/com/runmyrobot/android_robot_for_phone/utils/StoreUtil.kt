package com.runmyrobot.android_robot_for_phone.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Brendon on 9/5/2018.
 */
object StoreUtil {

    private fun getSharedPrefs(context: Context) : SharedPreferences{
        return context.getSharedPreferences("robotConfig", 0)
    }

    fun SetBluetoothDevice(context: Context, name : String, address : String) {
        getSharedPrefs(context).edit().putString("BTName", name).apply()
        getSharedPrefs(context).edit().putString("BTAddress", address).apply()
    }

    fun GetBluetoothDevice(context: Context) : Pair<String, String>?{
        val prefs = getSharedPrefs(context)
        val name = prefs.getString("BTName", null)
        val address = prefs.getString("BTAddress", null)
        if(name != null && address != null){
            return Pair(name, address)
        }
        return null
    }
}
