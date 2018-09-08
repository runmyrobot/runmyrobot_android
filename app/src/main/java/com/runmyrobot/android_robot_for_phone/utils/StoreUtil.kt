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

    fun setRobotId(context: Context, robotId : String){
        getSharedPrefs(context).edit().putString("robotId", robotId).apply()
    }

    fun getRobotId(context: Context) : String?{
        return getSharedPrefs(context).getString("robotId", null)
    }

    fun setCameraId(context: Context, cameraId : String){
        getSharedPrefs(context).edit().putString("cameraId", cameraId).apply()
    }

    fun getCameraId(context: Context) : String?{
        return getSharedPrefs(context).getString("cameraId", null)
    }

    fun setCameraPass(context: Context, cameraId : String){
        getSharedPrefs(context).edit().putString("cameraPass", cameraId).apply()
    }

    fun getCameraPass(context: Context) : String{
        return getSharedPrefs(context).getString("cameraPass", "hello")
    }

    fun setCameraEnabled(context: Context, cameraEnabled : Boolean){
        getSharedPrefs(context).edit().putBoolean("cameraEnabled", cameraEnabled).apply()
    }

    fun getCameraEnabled(context: Context) : Boolean{
        return getSharedPrefs(context).getBoolean("cameraEnabled", false)
    }

    fun setMicEnabled(context: Context, micEnabled : Boolean){
        getSharedPrefs(context).edit().putBoolean("micEnabled", micEnabled).apply()
    }

    fun getMicEnabled(context: Context) : Boolean{
        return getSharedPrefs(context).getBoolean("micEnabled", false)
    }

    fun setTTSEnabled(context: Context, TTSEnabled : Boolean){
        getSharedPrefs(context).edit().putBoolean("TTSEnabled", TTSEnabled).apply()
    }

    fun getTTSEnabled(context: Context) : Boolean{
        return getSharedPrefs(context).getBoolean("TTSEnabled", false)
    }

    fun setErrorReportingEnabled(context: Context, errorReporting : Boolean){
        getSharedPrefs(context).edit().putBoolean("errorReporting", errorReporting).apply()
    }

    fun getErrorReportingEnabled(context: Context) : Boolean{
        return getSharedPrefs(context).getBoolean("errorReporting", false)
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
