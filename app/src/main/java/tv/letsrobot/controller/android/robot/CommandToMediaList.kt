package tv.letsrobot.controller.android.robot

import android.content.Context
import android.net.Uri

/**
 * List that contains string references to media urls
 */
class CommandToMediaList{
    private val list = HashMap<String, Uri>()

    /**
     * Bind a string command to an audio file passed in as a Uri to the file.
     * This will not bind to an already used key
     */
    fun bindAudioFileToCommand(command : String, mediaUri: Uri){
        if(!list.containsKey(command))
            list[command] = mediaUri
    }

    /**
     * Bind a string command to an audio file passed in as a Uri to the file.
     * This will not bind to an already used key.
     *
     * This will grab the audio from the resources raw folder
     */
    fun bindRawResourcesAudioFileToCommand(command : String, context: Context, resId : String){
        bindAudioFileToCommand(command, rawNameToUri(context, resId))
    }

    /**
     * Parse an audio file in the R.raw directory
     */
    fun rawNameToUri(context: Context, idName: String) : Uri{
        return Uri.parse("android.resource://${context.packageName}/raw/$idName")
    }

    /**
     * unbind the audio from the control command
     */
    fun removeAudioBinding(command: String){
        list.remove(command)
    }

    /**
     * Remove all bound commands
     */
    fun removeAll(){
        list.clear()
    }

    /**
     * Return a non-modifiable list to the references
     */
    fun getList() : MutableMap<String, Uri>{
        return list.toMutableMap()
    }
}