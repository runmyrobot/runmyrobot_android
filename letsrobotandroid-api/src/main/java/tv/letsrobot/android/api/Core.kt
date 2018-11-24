package tv.letsrobot.android.api

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.TextureView
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import tv.letsrobot.android.api.EventManager.Companion.TIMEOUT
import tv.letsrobot.android.api.components.AudioComponent
import tv.letsrobot.android.api.components.CommunicationComponent
import tv.letsrobot.android.api.components.RobotControllerComponent
import tv.letsrobot.android.api.components.TextToSpeechComponent
import tv.letsrobot.android.api.components.camera.CameraBaseComponent
import tv.letsrobot.android.api.components.camera.ExtCameraInterface
import tv.letsrobot.android.api.components.camera.api19.Camera1TextureComponent
import tv.letsrobot.android.api.components.camera.api21.Camera2TextureComponent
import tv.letsrobot.android.api.enums.CommunicationType
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.enums.ProtocolType
import tv.letsrobot.android.api.interfaces.Component
import tv.letsrobot.android.api.models.CameraSettings
import tv.letsrobot.android.api.utils.JsonObjectUtils
import java.util.concurrent.atomic.AtomicBoolean

/**
 * letsrobot.tv core android api.
 *
 * This is configured with a builder to make sure no settings are changed while Core is running.
 * Core consists of multiple components
 * - Camera (only enabled if camera id is given)
 * - Robot Controller (Only enabled if robot id is given)
 * - Text to Speech (Defaults to disabled)
 *
 * Builder can allow for configuration of these individual components.
 * Disabling individual components is possible.
 */
class Core
/**
 * Intentional private initializer. Use Builder to get an instance of Core
 */
private constructor(val robotId : String, val cameraId : String?) {
    private val handlerThread: HandlerThread = HandlerThread("bg-thread")
    private var logLevel = LogLevel.NONE
    var camera: CameraBaseComponent? = null
    var audio: AudioComponent? = null
    var robotController: RobotControllerComponent? = null
    var textToSpeech: TextToSpeechComponent? = null
    private var externalComponents: ArrayList<Component>? = null
    private var onControllerTimeout = fun(_: Any?) {
        for (component in externalComponents!!) {
            component.timeout()
        }
    }

    /**
     * The Class's log level
     */
    enum class LogLevel {
        TRACE,
        INFO,
        DEBUG,
        WARNING,
        ERROR,
        NONE
    }

    /**
     * Log internal class messages, and allow log level to be adjusted specifically for this class
     * @param logLevel Log level to use
     * @param message message
     */
    private fun log(logLevel: LogLevel, message: String) {
        if (logLevel.ordinal < this.logLevel.ordinal) {
            when (logLevel) {
                LogLevel.TRACE -> Log.d(TAG, message, Exception())
                LogLevel.INFO -> Log.i(TAG, message)
                LogLevel.DEBUG -> Log.d(TAG, message)
                LogLevel.WARNING -> Log.w(TAG, message)
                LogLevel.ERROR -> Log.e(TAG, message, Exception())
                LogLevel.NONE -> {
                }
            }
        }
    }

    init {
        EventManager.invoke(javaClass.simpleName, ComponentStatus.DISABLED)
        handlerThread.start()
        handler = Handler(handlerThread.looper){
            msg ->
            Log.d(TAG, "handleMessage")
            when (msg.what) {
                START -> enableInternal()
                STOP -> disableInternal()
                QUEUE_UPDATE_TO_SERVER -> {
                    if(running.get()) {
                        handler?.postDelayed(onUpdateServer, 1000)
                    }
                }
            }
            true
        }
    }

    private var count = 0
    private val onUpdateServer: () -> Unit = {
        Log.d(TAG, "onUpdateServer")
        appServerSocket?.emit("identify_robot_id", robotId)
        if(count % 60 == 0){
            camera?.let {
                val obj = JSONObject()
                obj.put("send_video_process_exists",true)
                obj.put("ffmpeg_process_exists", getCameraRunning())
                obj.put("camera_id",cameraId)
                appServerSocket?.emit("send_video_status", obj)
            }
            val ipInfo = JSONObject()
            ipInfo.put("ip", "169.254.25.110") //TODO actually use a different ip...
            ipInfo.put("robot_id", robotId)
            appServerSocket?.emit("ip_information", ipInfo)
        }
        handler?.sendEmptyMessage(QUEUE_UPDATE_TO_SERVER)
        count++
    }

    private fun getCameraRunning(): Boolean {
        camera?.let {
            return camera?.process != null
        }
        return false
    }

    private var appServerSocket: Socket? = null

    private fun enableInternal() {
        if(running.getAndSet(true)) return //already enabled
        EventManager.invoke(javaClass.simpleName, ComponentStatus.CONNECTING)
        log(LogLevel.INFO, "starting core...")
        JsonObjectUtils.getJsonObjectFromUrl(
                String.format("https://letsrobot.tv/get_robot_owner/%s", robotId)
        )?.let {
            owner = it.getString("owner")
        }

        camera?.enable()
        audio?.enable()
        robotController?.enable()
        textToSpeech?.enable()
        for (component in externalComponents!!) {
            component.enable()
        }
        appServerSocket = IO.socket("http://letsrobot.tv:8022")
        appServerSocket?.connect()
        appServerSocket?.on(Socket.EVENT_CONNECT_ERROR){
            Log.d(TAG, "appServerSocket EVENT_CONNECT_ERROR")
            EventManager.invoke(javaClass.simpleName, ComponentStatus.ERROR)
        }
        appServerSocket?.on(Socket.EVENT_CONNECT){
            Log.d(TAG, "appServerSocket is connected!")
            EventManager.invoke(javaClass.simpleName, ComponentStatus.STABLE)
            appServerSocket?.emit("identify_robot_id", robotId)
        }
        appServerSocket?.on(Socket.EVENT_DISCONNECT){
            EventManager.invoke(javaClass.simpleName, ComponentStatus.DISABLED)
        }
        //Ugly way of doing timeouts. Should find a better way
        EventManager.subscribe(TIMEOUT, onControllerTimeout)
        handler?.postDelayed(onUpdateServer, 1000)
        log(LogLevel.INFO, "core is started!")
    }

    private fun disableInternal() {
        if(!running.getAndSet(false)) return //already disabled
        log(LogLevel.INFO, "shutting down core...")
        if (robotController != null) {
            robotController!!.disable()
        }
        if (camera != null) {
            camera!!.disable()
        }
        if (textToSpeech != null) {
            textToSpeech!!.disable()
        }
        audio?.disable()
        for (component in externalComponents!!) {
            component.disable()
        }
        appServerSocket?.disconnect()
        EventManager.unsubscribe(TIMEOUT, onControllerTimeout)
        EventManager.invoke(javaClass.simpleName, ComponentStatus.DISABLED)
        log(LogLevel.INFO, "core is shut down!")
    }

    private val shouldRun = AtomicBoolean(false)

    /**
     * Enable the LetsRobot android core. This will start from scratch.
     * Nothing except settings have been initialized before this call.
     * @return true if successful, false if already started
     */
    fun enable(): Boolean {
        if (shouldRun.getAndSet(true)) {
            return false
        }
        handler?.sendEmptyMessage(START)
        return true
    }

    /**
     * Disables the api and resets it to before it was initialized.
     * This should be called in OnDestroy() when the app gets killed.
     * @return true if disable successful, or false if already in disabled state
     */
    fun disable(): Boolean {
        if (!shouldRun.getAndSet(false)) {
            return false
        }
        handler?.sendEmptyMessage(STOP)
        return true
    }

    fun onPause() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            if (!shouldRun.get()) {
                return
            }
            handler?.sendEmptyMessage(STOP)
        }
        TelemetryManager.Instance?.invoke("onPause", null)
    }

    fun onResume() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            if (!shouldRun.get()) {
                return
            }
            handler?.sendEmptyMessage(START)
        }
        TelemetryManager.Instance?.invoke("onResume", null)
    }

    /**
     * Builder for the Core api.
     * Call build() after changing settings to have the actual Core class returned.
     */
    class Builder
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
        var externalComponents: ArrayList<Component>? = null
        var cameraSettings: CameraSettings? = null

        /**
         * Build a configured instance of Core.
         * @return Core
         * @throws InitializationException
         */
        @Throws(InitializationException::class)
        fun build(): Core {
            //TODO define preconditions that will throw errors

            //RobotId MUST be defined, cameraId can be ignored
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
            val core = Core(robotId!!, cameraSettings?.cameraId)
            //Get list of external components, such as LED code, or more customized motor control
            core.externalComponents = externalComponents
            robotId?.let{
                core.robotController = RobotControllerComponent(context, it)
                //Setup our protocol, if it exists
                val protocolClass = protocol?.getInstantiatedClass(context)
                protocolClass?.let { protocol ->
                    TelemetryManager.Instance?.invoke("Protocol Selection", protocol::javaClass.name)
                    //Add it to the component list
                    core.externalComponents?.add(protocol)
                }
                //Setup our communication, if it exists
                val communicationClass = communication?.getInstantiatedClass
                communicationClass?.let { communication ->
                    //Add it to the component list
                    TelemetryManager.Instance?.invoke("Communication Selection", communication::javaClass.name)
                    core.externalComponents?.add(CommunicationComponent(context, communication))
                }
            }
            cameraSettings?.let{ config ->
                if(useMic) {
                    core.audio = AudioComponent(context, config.cameraId, config.pass)
                }
                holder?.let {
                    if(false/*TODO StoreUtil or autodetect*/){
                        TelemetryManager.Instance?.invoke("Camera Selection", "ExtCameraInterface")
                        core.camera = ExtCameraInterface(context, config)
                    }
                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !config.useLegacyApi) {
                        TelemetryManager.Instance?.invoke("Camera Selection", "Camera2TextureComponent")
                        core.camera = Camera2TextureComponent(context, config, holder!!)
                    }
                    else{
                        TelemetryManager.Instance?.invoke("Camera Selection", "Camera1TextureComponent")
                        core.camera = Camera1TextureComponent(context, config, holder!!)
                    }
                }
            }
            if (useTTS) {
                core.textToSpeech = TextToSpeechComponent(context, robotId!!)
            }
            //Set the log level
            core.logLevel = logLevel


            return core
        }
    }

    class InitializationException : Exception()

    companion object {
        fun initDependencies(context: Context, done: () -> Unit) {
            //Load FFMpeg
            TelemetryManager.init(context)
            val ffmpeg = FFmpeg.getInstance(context.applicationContext)
            try {
                ffmpeg.loadBinary(object : LoadBinaryResponseHandler() {
                    override fun onFinish() {
                        super.onFinish()
                        Log.d("FFMPEG", "onFinish")
                        done() //run next action
                    }

                    override fun onSuccess() {
                        super.onSuccess()
                        Log.d("FFMPEG", "onSuccess")
                    }

                    override fun onFailure() {
                        super.onFailure()
                        Log.d("FFMPEG", "onFailure")
                    }

                    override fun onStart() {
                        super.onStart()
                        Log.d("FFMPEG", "onStart")
                    }
                    //TODO maybe catch some error to display to the user
                })
            } catch (e: FFmpegNotSupportedException) {
                e.printStackTrace()
                done() //run next action
            }
        }

        private const val START = 1
        private const val STOP = 2
        private const val QUEUE_UPDATE_TO_SERVER = 3
        private const val TAG = "RobotCore"
        private val running = AtomicBoolean(false)
        /**
         * Core handler
         */
        internal var handler: Handler? = null
        var owner: String? = null
    }
}
