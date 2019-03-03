package tv.letsrobot.controller.android.robot

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import tv.letsrobot.android.api.enums.ComponentType
import tv.letsrobot.android.api.interfaces.Component
import tv.letsrobot.android.api.interfaces.ComponentEventObject

/**
 * Example of a custom component
 */
class PlayAudioComponentExample(
        context: Context,
        val commandToMediaList : CommandToMediaList
) : Component(context), MediaPlayer.OnCompletionListener {
    override fun getType(): ComponentType {
        return ComponentType.CUSTOM
    }

    private var player: MediaPlayer? = null

    override fun enableInternal() {
        resetPlayer()
    }

    override fun disableInternal() {
        try {
            player?.release()
            player = null
        } catch (e: Exception) {
        }
    }

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        return super.handleExternalMessage(message).also {
            if(message.type == ComponentType.CONTROL_SOCKET){
                commandToMediaList.getList()[message.data as? String]?.let { audioFile ->
                    playAudioFromCommand(audioFile)
                }
            }
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        resetPlayer()
    }

    private fun resetPlayer(){
        try {
            player?.release()
        } catch (e: Exception) {
            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
        }
        player = MediaPlayer()
        player?.setOnCompletionListener(this)
    }

    private fun playAudioFromCommand(data: Uri) {
        player?.let {
            if(!it.isPlaying){
                it.setDataSource(context, data)
                it.prepare()
                it.start()
            }
        }
    }
}