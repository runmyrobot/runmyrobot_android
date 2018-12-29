package tv.letsrobot.android.api.models

import android.content.Context
import android.os.Build
import android.view.TextureView
import tv.letsrobot.android.api.Core.InitializationException
import tv.letsrobot.android.api.TelemetryManager
import tv.letsrobot.android.api.components.AudioComponent
import tv.letsrobot.android.api.components.CommunicationComponent
import tv.letsrobot.android.api.components.ControlSocketComponent
import tv.letsrobot.android.api.components.TextToSpeechComponent
import tv.letsrobot.android.api.components.camera.ExtCameraInterface
import tv.letsrobot.android.api.components.camera.api19.Camera1TextureComponent
import tv.letsrobot.android.api.components.camera.api21.Camera2TextureComponent
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
    var holder: TextureView? = null
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
        robotId?.let{
            val robotController = ControlSocketComponent(context, it)
            componentList.add(robotController)
            //Setup our protocol, if it exists
            val protocolClass = protocol?.getInstantiatedClass(context)
            protocolClass?.let { protocol ->
                TelemetryManager.Instance?.invoke("Protocol Selection", protocol::javaClass.name)
                //Add it to the component list
                externalComponents?.add(protocol)
            }
            //Setup our communication, if it exists
            val communicationClass = communication?.getInstantiatedClass
            communicationClass?.let { communication ->
                //Add it to the component list
                TelemetryManager.Instance?.invoke("Communication Selection", communication::javaClass.name)
                externalComponents?.add(CommunicationComponent(context, communication))
            }
        }
        cameraSettings?.let{ config ->
            if(useMic) {
                val audioComponent = AudioComponent(context, config.cameraId, config.pass)
                componentList.add(audioComponent)
            }
            holder?.let {
                val camera = if(false/*TODO StoreUtil or autodetect*/){
                    ExtCameraInterface(context, config)
                }
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !config.useLegacyApi) {
                    Camera2TextureComponent(context, config, holder!!)
                }
                else{
                    Camera1TextureComponent(context, config, holder!!)
                }
                componentList.add(camera)
                TelemetryManager.Instance?.invoke("Camera Selection", camera::class.java.simpleName)
            }
        }
        if (useTTS) {
            val textToSpeech = TextToSpeechComponent(context, robotId!!)
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
            TelemetryManager.Instance?.let { tm ->
                val robotIdStr = robotId?.let{
                    "Value"
                }
                val cameraIdStr = cameraSettings?.cameraId?.let{
                    "Value"
                }
                tm.invoke("InitializationException", "robotId=$robotIdStr, cameraId=$cameraIdStr")
            }
            throw InitializationException()
        }
    }
}