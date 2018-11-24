package tv.letsrobot.android.api.utils

import tv.letsrobot.android.api.enums.CameraDirection
import tv.letsrobot.android.api.enums.CommunicationType
import tv.letsrobot.android.api.enums.ProtocolType

enum class VariableType{
    BooleanClass,
    StringClass,
    ProtocolTypeEnum,
    CommunicationTypeEnum,
    CameraDirectionEnum;

    companion object {
        fun forValue(value : Any?) : VariableType?{
            return when(value){
                is Boolean -> BooleanClass
                is String -> StringClass
                is ProtocolType -> ProtocolTypeEnum
                is CommunicationType -> CommunicationTypeEnum
                is CameraDirection -> CameraDirectionEnum
                else -> null
            }
        }
    }
}