package tv.letsrobot.android.api.enums

/**
 * Enums for potential component types, and their potential global events
 */
enum class ComponentType {
    CAMERA,
    CONTROL_DRIVER,
    CONTROL_TRANSLATOR{
        val BYTE_ARRAY_COMMAND = 0
    },
    CONTROL_SOCKET{
        val COMMAND = 0
    },
    CHAT_SOCKET{
        /**
         * Any chat command by the owner that is preceded by a '.'
         */
        val COMMAND = 0
        /**
         * Any speakable text that is not preceded by '.'
         */
        val CHAT = 1
    },
    APP_SOCKET{
        val ROBOT_OWNER = 0
    },
    TTS,
    MICROPHONE,
}