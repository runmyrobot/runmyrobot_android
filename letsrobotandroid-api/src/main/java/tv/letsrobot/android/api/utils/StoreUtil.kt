package tv.letsrobot.android.api.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import tv.letsrobot.android.api.enums.CameraDirection
import tv.letsrobot.android.api.enums.CommunicationType
import tv.letsrobot.android.api.enums.ProtocolType

/**
 * Created by Brendon on 9/5/2018.
 */
object StoreUtil {
    private fun saveBoolean(context: Context, key : String, value : Boolean){
        getSharedPrefs(context).edit().putBoolean(key, value).apply()
    }

    private fun getBoolean(context: Context, key : String, default : Boolean = false) : Boolean{
        return getSharedPrefs(context).getBoolean(key, default)
    }

    fun getSharedPrefs(context: Context) : SharedPreferences{
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

    fun setScreenSleepOverlayEnabled(context: Context, sleepMode : Boolean){
        getSharedPrefs(context).edit().putBoolean("sleepMode", sleepMode).apply()
    }

    fun getScreenSleepOverlayEnabled(context: Context) : Boolean{
        return getSharedPrefs(context).getBoolean("sleepMode", true)
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

    /**
     * Set bitrate in kbps
     */
    fun setBitrate(context: Context, videoBitrate : String){
        getSharedPrefs(context).edit().putString("videoBitrate", videoBitrate).apply()
    }

    /**
     * Get bitrate in kbps. Defaults to 512 if nothing setup
     */
    fun getBitrate(context: Context): String {
        return getSharedPrefs(context).getString("videoBitrate", null)
                ?.let { it }
                ?: "512"
    }

    /**
     * Set bitrate in kbps
     */
    fun setResolution(context: Context, videoResolution : String){
        getSharedPrefs(context).edit().putString("videoResolution", videoResolution).apply()
    }

    /**
     * Get bitrate in kbps
     */
    fun getResolution(context: Context): String {
        return getSharedPrefs(context).getString("videoResolution", "640x480")
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

    fun setConfigured(context: Context, b: Boolean) {
        getSharedPrefs(context).edit().putBoolean("configured", b).apply()
    }

    fun getConfigured(context: Context) : Boolean{
        return getSharedPrefs(context).getBoolean("configured", false)
    }

    fun setCommunicationType(context: Context, type: CommunicationType){
        getSharedPrefs(context).edit().putString("CommunicationType",type.name).apply()
    }

    fun getCommunicationType(context: Context): CommunicationType? {
        return getSharedPrefs(context).getString("CommunicationType", null)?.let{
            try {
                CommunicationType.valueOf(it) //Could throw IllegalArgumentException
            } catch (e: Exception) {
                null
            }
        } //returns null if let does not go through
    }

    fun setProtocolType(context: Context, type: ProtocolType){
        getSharedPrefs(context).edit().putString("ProtocolType",type.name).apply()
    }

    fun getProtocolType(context: Context): ProtocolType? {
        return getSharedPrefs(context).getString("ProtocolType", null)?.let{
            try {
                ProtocolType.valueOf(it) //Could throw IllegalArgumentException
            } catch (e: Exception) {
                null
            }
        } //returns null if let does not go through
    }

    fun setOrientation(context: Context, direction: CameraDirection){
        getSharedPrefs(context).edit().putString("orientation",direction.name).apply()
    }

    fun getOrientation(context: Context): CameraDirection {
        getSharedPrefs(context).getString("orientation", CameraDirection.DIR_90.name)?.let {
            try {
                return CameraDirection.valueOf(it) //Could throw IllegalArgumentException
            } catch (_: Exception) {

            }
        }
        return CameraDirection.DIR_90
    }

    /**
     * Use legacy camera1 api.
     * Useless when api is less than 21, since newer camera api is not supported on those versions
     */
    fun setUseLegacyCamera(context: Context, b: Boolean) {
        getSharedPrefs(context).edit().putBoolean("camera1api", b).apply()
    }

    /**
     * Use legacy camera1 api.
     * Automatically evaluates to true and is ignored if less than api 21
     */
    fun getUseLegacyCamera(context: Context): Boolean {
        return if(Build.VERSION.SDK_INT >= 21){
            getSharedPrefs(context).getBoolean("camera1api", false)
        }
        else{
            true
        }
    }
}
