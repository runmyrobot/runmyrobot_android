package tv.letsrobot.android.api.enums

enum class CameraDirection {
    DIR_0, DIR_90, DIR_180, DIR_270;

    override fun toString(): String {
        return when(this){
            DIR_0 -> "0"
            DIR_90 -> "90"
            DIR_180 -> "180"
            DIR_270 -> "270"
        }
    }
}
