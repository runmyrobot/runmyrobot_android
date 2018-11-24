package tv.letsrobot.android.api.utils

import android.content.Context
import android.content.SharedPreferences
import tv.letsrobot.android.api.enums.CameraDirection
import tv.letsrobot.android.api.enums.CommunicationType
import tv.letsrobot.android.api.enums.ProtocolType

internal enum class Settings(private val variable: VariableType) {
    RobotId(VariableType.StringClass),
    CameraId(VariableType.StringClass),
    CameraPass(VariableType.StringClass),
    CameraEnabled(VariableType.BooleanClass),
    SleepMode(VariableType.BooleanClass),
    MicEnabled(VariableType.BooleanClass),
    TTSEnabled(VariableType.BooleanClass),
    ErrorReporting(VariableType.BooleanClass),
    VideoBitrate(VariableType.StringClass),
    VideoResolution(VariableType.StringClass),
    Communication(VariableType.CommunicationTypeEnum),
    Protocol(VariableType.ProtocolTypeEnum),
    Orientation(VariableType.CameraDirectionEnum),
    UseLegacyCamera(VariableType.BooleanClass);

    @Throws(IllegalArgumentException::class)
    fun saveValue(context: Context, value: Any){
        if(VariableType.forValue(value) != variable)
            throw IllegalArgumentException("Expected type of $variable")
        val sharedPrefs = getSharedPrefs(context).edit()
        when(variable){
            VariableType.BooleanClass -> sharedPrefs.putBoolean(name, value as Boolean)
            VariableType.StringClass -> sharedPrefs.putString(name, value as String)
            VariableType.ProtocolTypeEnum -> sharedPrefs.putString(name, (value as ProtocolType).name)
            VariableType.CommunicationTypeEnum -> sharedPrefs.putString(name, (value as CommunicationType).name)
            VariableType.CameraDirectionEnum -> sharedPrefs.putString(name, (value as CameraDirection).name)
        }
        sharedPrefs.apply()
    }

    @Throws(IllegalArgumentException::class)
    fun getValue(context: Context, default : Any) : Any?{
        if(VariableType.forValue(default) != variable)
            throw IllegalArgumentException("Expected type of $variable")
        val sharedPrefs = getSharedPrefs(context)
        return when(variable){
            VariableType.BooleanClass -> sharedPrefs.getBoolean(name, default as Boolean)
            VariableType.StringClass -> sharedPrefs.getString(name, default as String)
            VariableType.ProtocolTypeEnum -> {
                sharedPrefs.getString(name, null)?.let {
                    ProtocolType.valueOf(it)
                } ?: default
            }
            VariableType.CommunicationTypeEnum -> {
                sharedPrefs.getString(name, null)?.let {
                    CommunicationType.valueOf(it)
                } ?: default
            }
            VariableType.CameraDirectionEnum -> {
                sharedPrefs.getString(name, null)?.let {
                    CameraDirection.valueOf(it)
                } ?: default
            }
        }
    }

    companion object {
        fun getSharedPrefs(context: Context) : SharedPreferences {
            return context.getSharedPreferences("robotConfig", 0)
        }
    }
}