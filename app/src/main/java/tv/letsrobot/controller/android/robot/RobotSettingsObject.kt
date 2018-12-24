package tv.letsrobot.controller.android.robot

import android.content.Context
import tv.letsrobot.android.api.enums.CameraDirection
import tv.letsrobot.android.api.enums.CommunicationType
import tv.letsrobot.android.api.enums.ProtocolType
import tv.letsrobot.android.api.utils.RobotConfig
import java.util.*

/**
 * Wrapper object for RobotConfig
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
        variables.add(cameraBitrate.toString())
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
                        robotId = splitData[1],
                        robotProtocol = ProtocolType.values()[splitData[2].toInt()],
                        robotCommunication = CommunicationType.values()[splitData[3].toInt()],
                        cameraId =  splitData[4],
                        cameraPassword = splitData[5],
                        cameraOrientation = CameraDirection.values()[splitData[6].toInt()],
                        cameraBitrate = splitData[7].toInt(),
                        cameraResolution = splitData[8],
                        cameraEnabled = splitData[9].fromNumericBoolean()!!,
                        cameraLegacy = splitData[10].fromNumericBoolean()!!,
                        enableMic = splitData[11].fromNumericBoolean()!!,
                        enableTTS = splitData[12].fromNumericBoolean()!!,
                        screenTimeout = splitData[13].fromNumericBoolean()!!,
                        version = 1 /*Add the version here*/)
            }catch (e : Exception){
                //fail for any reason.
                null
            }
        }

        fun save(context: Context, settings: RobotSettingsObject) {
            //we don't allow higher resolution currently for legacy camera API right now, so prevent that
            val cameraRes = if(settings.cameraLegacy)
                RobotConfig.VideoResolution.default as String
            else
                settings.cameraResolution
            saveTextViewToRobotConfig(context, settings.robotId, RobotConfig.RobotId)
            saveTextViewToRobotConfig(context, settings.cameraId, RobotConfig.CameraId)
            saveTextViewToRobotConfig(context, settings.cameraPassword, RobotConfig.CameraPass)
            saveTextViewToRobotConfig(context, settings.cameraBitrate.toString(), RobotConfig.VideoBitrate)
            saveTextViewToRobotConfig(context, cameraRes, RobotConfig.VideoResolution)
            RobotConfig.UseLegacyCamera.saveValue(context, settings.cameraLegacy)
            RobotConfig.CameraEnabled.saveValue(context, settings.cameraEnabled)
            RobotConfig.MicEnabled.saveValue(context, settings.enableMic)
            RobotConfig.TTSEnabled.saveValue(context, settings.enableTTS)
            RobotConfig.Communication.saveValue(context, settings.robotCommunication)
            RobotConfig.Protocol.saveValue(context, settings.robotProtocol)
            RobotConfig.Orientation.saveValue(context, settings.cameraOrientation)
            RobotConfig.SleepMode.saveValue(context, settings.screenTimeout)
        }

        fun load(context: Context) : RobotSettingsObject{
            return RobotSettingsObject(
                    RobotConfig.RobotId.getValue(context),
                    RobotConfig.Protocol.getValue(context),
                    RobotConfig.Communication.getValue(context),
                    RobotConfig.CameraId.getValue(context),
                    RobotConfig.CameraPass.getValue(context),
                    RobotConfig.Orientation.getValue(context),
                    (RobotConfig.VideoBitrate.getValue(context) as String).toInt(),
                    RobotConfig.VideoResolution.getValue(context),
                    RobotConfig.CameraEnabled.getValue(context),
                    RobotConfig.UseLegacyCamera.getValue(context),
                    RobotConfig.MicEnabled.getValue(context),
                    RobotConfig.TTSEnabled.getValue(context),
                    RobotConfig.SleepMode.getValue(context)
            )
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

