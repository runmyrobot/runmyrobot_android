package tv.letsrobot.android.api.components

import android.content.Context
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import tv.letsrobot.android.api.interfaces.Component

/**
 * Abstracted class for different camera implementations
 */
abstract class CameraBaseComponent(context: Context, val cameraId: String) : Component(context), FFmpegExecuteResponseHandler {

    override fun enableInternal() {
        //TODO
    }

    override fun disableInternal() {
        //TODO
    }

    //TODO setup
}