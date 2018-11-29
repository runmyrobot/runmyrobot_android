package tv.letsrobot.controller.android.robot

import android.content.Context
import tv.letsrobot.android.api.enums.CameraDirection
import tv.letsrobot.android.api.enums.CommunicationType
import tv.letsrobot.android.api.enums.ProtocolType
import tv.letsrobot.android.api.utils.RobotConfig
import java.util.*

/**
 * Created by Brendon on 11/28/2018.
 */
data class RobotSettingsObject(val robotId : String,
                               val robotProtocol : ProtocolType,
                               val robotCommunication : CommunicationType,
                               val cameraId : String,
                               val cameraPassword : String,
                               val cameraOrientation: CameraDirection,
                               val cameraBitrate : Int,
                               val cameraResolution : String,
                               val cameraEnabled : Boolean,
                               val cameraLegacy : Boolean,
                               val enableMic : Boolean,
                               val enableTTS : Boolean,
                               val screenTimeout : Boolean,
                               var version : Int = 1 /*Version of the QR Code*/){

    override fun toString() : String{
        val variables = ArrayList<Any>()
        variables.add(version) /*Version always first, event though it is last in constructor*/
        variables.add(robotId)
        variables.add(robotProtocol)
        variables.add(robotCommunication)
        variables.add(cameraId)
        variables.add(cameraPassword)
        variables.add(cameraOrientation)
        variables.add(cameraBitrate)
        variables.add(cameraResolution)
        variables.add(cameraEnabled)
        variables.add(cameraLegacy)
        variables.add(enableMic)
        variables.add(enableTTS)
        variables.add(screenTimeout)
        return createDelimitedString(variables)
    }

    private fun createDelimitedString(variables: ArrayList<Any>): String {
        val builder = StringBuilder()
        for(data in variables){
            if(!builder.isEmpty())
                builder.append(SEPARATOR)
            when(data){
                is String -> builder.append(data)
                is Boolean -> builder.append(data.toInt())
                is Int -> builder.append(data)
                is Enum<*> -> builder.append(data.ordinal)
                else -> {
                    throw NotImplementedError("Variable type not supported yet!")
                }
            }
        }
        return builder.toString()
    }

    companion object {
        private const val SEPARATOR = ';'

        /**
         * Handle parsing of the SEPARATOR delimited string of variables. Add a version int
         */
        fun fromString(data : String) : RobotSettingsObject?{
            val splitData = data.split(SEPARATOR)
            return splitData[0].toIntOrNull()?.let {
                when(it){
                    1 -> {
                       convertApi1(splitData)
                    }
                    else -> null
                }
            }
        }

        private fun convertApi1(splitData: List<String>): RobotSettingsObject? {
            return try{
                //version is index 0, so we offset
                return RobotSettingsObject(
                        splitData[1],
                        ProtocolType.values()[splitData[2].toInt()],
                        CommunicationType.values()[splitData[3].toInt()],
                        splitData[4],
                        splitData[5],
                        CameraDirection.values()[splitData[6].toInt()],
                        splitData[7].toInt(),
                        splitData[8],
                        splitData[9].fromNumericBoolean()!!,
                        splitData[10].fromNumericBoolean()!!,
                        splitData[11].fromNumericBoolean()!!,
                        splitData[12].fromNumericBoolean()!!,
                        splitData[13].fromNumericBoolean()!!,
                        1 /*Add the version here*/)
            }catch (e : Exception){
                //fail for any reason. We don't trust the data.
                null
            }
        }

        fun save(context: Context, settings: RobotSettingsObject) {
            saveTextViewToRobotConfig(context, settings.robotId, RobotConfig.RobotId)
            saveTextViewToRobotConfig(context, settings.cameraId, RobotConfig.CameraId)
            saveTextViewToRobotConfig(context, settings.cameraPassword, RobotConfig.CameraPass)
            saveTextViewToRobotConfig(context, settings.cameraBitrate.toString(), RobotConfig.VideoBitrate)
            saveTextViewToRobotConfig(context, settings.cameraResolution, RobotConfig.VideoResolution)
            RobotConfig.UseLegacyCamera.saveValue(context, settings.cameraLegacy)
            RobotConfig.CameraEnabled.saveValue(context, settings.cameraEnabled)
            RobotConfig.MicEnabled.saveValue(context, settings.enableMic)
            RobotConfig.TTSEnabled.saveValue(context, settings.enableTTS)
            RobotConfig.Communication.saveValue(context, settings.robotCommunication)
            RobotConfig.Protocol.saveValue(context, settings.robotProtocol)
            RobotConfig.Orientation.saveValue(context, settings.cameraOrientation)
            RobotConfig.SleepMode.saveValue(context, settings.screenTimeout)
        }

        private fun saveTextViewToRobotConfig(context: Context, value : String, setting : RobotConfig){
            value.takeIf { !it.isBlank() }?.let {
                setting.saveValue(context, it)
            } ?: setting.reset(context)
        }
    }
}

private fun Boolean.toInt() = if (this) 1 else 0
private fun String.fromNumericBoolean() : Boolean?{
    return this.toIntOrNull() == 1
}

