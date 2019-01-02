package tv.letsrobot.android.api.models

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import tv.letsrobot.android.api.components.*
import tv.letsrobot.android.api.components.camera.ExtCameraInterface
import tv.letsrobot.android.api.components.camera.api19.Camera1SurfaceTextureComponent
import tv.letsrobot.android.api.components.camera.api21.Camera2SurfaceTextureComponent
import tv.letsrobot.android.api.components.tts.SystemDefaultTTSComponent
import tv.letsrobot.android.api.enums.LogLevel
import tv.letsrobot.android.api.interfaces.IComponent
import tv.letsrobot.android.api.robot.CommunicationType
import tv.letsrobot.android.api.robot.ProtocolType
import tv.letsrobot.android.api.services.LetsRobotService

/**
 * Created by Brendon on 12/26/2018.
 */
/**
 * Builder for the Core api.
 * Call build() after changing settings to have the actual Core class returned.
 */
class ServiceComponentGenerator
/**
 * Instantiate for a Builder instance. After settings have been confirmed,
 * please call build() to receive an instance to Core
 *
 * Will throw NullPointerException if context is null
 * @param context Application context
 */
(context: Context) {

    class InitializationException : Exception()


    internal var context: Context = context.applicationContext

    private var logLevel = LogLevel.NONE

    /**
     * Communication type to use. Ex. Bluetooth or USB
     */
    var communication : CommunicationType? = null

    /**
     * Protocol type to use. Ex. Arduino Raw or Sabertooth Serial
     */
    var protocol : ProtocolType? = null

    /**
     * Id that should be used to receive chat messages from server
     */
    var robotId: String? = null

    /**
     * Should chat messages be piped through the android speaker?
     */
    var useTTS = false

    var useMic = false
    var externalComponents: ArrayList<IComponent>? = null
    var cameraSettings: CameraSettings? = null

    /**
     * Build a configured instance of Core.
     * @return Core
     * @throws InitializationException
     */
    @Throws(InitializationException::class)
    fun build(): ArrayList<IComponent> {
        //TODO define preconditions that will throw errors
        val componentList = ArrayList<IComponent>()
        //RobotId MUST be defined, cameraId can be ignored
        validateSettings(robotId, cameraSettings) //will throw if bad
        componentList.add(MainSocketComponent(context))
        robotId?.let{
            val robotController = ControlSocketComponent(context, it)
            componentList.add(robotController)
            //Setup our protocol, if it exists
            val protocolClass = protocol?.getInstantiatedClass(context)
            protocolClass?.let { protocol ->
                //Add it to the component list
                externalComponents?.add(protocol)
            }
            //Setup our communication, if it exists
            val communicationClass = communication?.getInstantiatedClass
            communicationClass?.let { communication ->
                //Add it to the component list
                externalComponents?.add(CommunicationComponent(context, communication))
            }
        }
        cameraSettings?.let{ config ->
            if(useMic) {
                val audioComponent = AudioComponent(context, config.cameraId, config.pass)
                componentList.add(audioComponent)
            }
            val camera = if(false/*TODO StoreUtil or autodetect*/){
                ExtCameraInterface(context, config)
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !config.useLegacyApi) {
                Camera2SurfaceTextureComponent(context, config)
            }
            else{
                Camera1SurfaceTextureComponent(context, config)
            }
            componentList.add(camera)
        }
        if (useTTS) {
            val textToSpeech = ChatSocketComponent(context, robotId!!)
            val ttsEngine = SystemDefaultTTSComponent(context)
            componentList.add(ttsEngine)
            componentList.add(textToSpeech)
        }
        //Get list of external components, such as LED code, or more customized motor control
        externalComponents?.let { componentList.addAll(it) }
        //Set the log level
        LetsRobotService.logLevel = logLevel
        return componentList
    }

    @Throws(InitializationException::class)
    fun validateSettings(robotId : String?, cameraSettings: CameraSettings?){
        if (robotId == null && cameraSettings?.cameraId == null || robotId == null) {
            throw InitializationException()
        }
    }

    companion object {
        fun initDependencies(context: Context, done: () -> Unit) {
            val ffmpeg = FFmpeg.getInstance(context.applicationContext)
            try {
                ffmpeg.loadBinary(object : LoadBinaryResponseHandler() {
                    override fun onFinish() {
                        super.onFinish()
                        Log.d("FFMPEG", "onFinish")
                        done() //run next action
                    }
                })
            } catch (e: FFmpegNotSupportedException) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                e.printStackTrace()
                done() //run next action
            }
        }

        /**
         * Clear all stored config data for our Communication components, which would trigger setup again
         */
        fun resetCommunicationConfig(context: Context) {
            CommunicationType.values().forEach {
                it.getInstantiatedClass?.clearSetup(context.applicationContext)
            }
        }
    }
}