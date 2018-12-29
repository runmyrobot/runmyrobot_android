package tv.letsrobot.android.api

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.widget.Toast
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import tv.letsrobot.android.api.EventManager.Companion.TIMEOUT
import tv.letsrobot.android.api.components.AudioComponent
import tv.letsrobot.android.api.components.ControlSocketComponent
import tv.letsrobot.android.api.components.TextToSpeechComponent
import tv.letsrobot.android.api.components.camera.CameraBaseComponent
import tv.letsrobot.android.api.components.camera.api19.Camera1TextureComponent
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.enums.LogLevel
import tv.letsrobot.android.api.interfaces.Component
import tv.letsrobot.android.api.robot.CommunicationType
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
    var controlSocket: ControlSocketComponent? = null
    var textToSpeech: TextToSpeechComponent? = null
    private var externalComponents: ArrayList<Component>? = null
    private var onControllerTimeout = fun(_: Any?) {
        for (component in externalComponents!!) {
            component.timeout()
        }
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
        //get owner of this bot
        JsonObjectUtils.getJsonObjectFromUrl(
                String.format("https://letsrobot.tv/get_robot_owner/%s", robotId)
        )?.let {
            owner = it.getString("owner")
        }
        enableComponents()
        setupAppWebSocket()
        //manage our own timeout in case something happens to network or site
        EventManager.subscribe(TIMEOUT, onControllerTimeout)
        handler?.postDelayed(onUpdateServer, 1000)
        log(LogLevel.INFO, "core is started!")
    }

    private fun setupAppWebSocket() {
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
    }

    private fun enableComponents() {
        camera?.enable()
        audio?.enable()
        controlSocket?.enable()
        textToSpeech?.enable()
        for (component in externalComponents!!) {
            component.enable()
        }
    }

    private fun disableInternal() {
        if(!running.getAndSet(false)) return //already disabled
        log(LogLevel.INFO, "shutting down core...")
        disableComponents()
        appServerSocket?.disconnect()
        EventManager.unsubscribe(TIMEOUT, onControllerTimeout)
        EventManager.invoke(javaClass.simpleName, ComponentStatus.DISABLED)
        log(LogLevel.INFO, "core is shut down!")
    }

    private fun disableComponents(){
        if (controlSocket != null) {
            controlSocket!!.disable()
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
        if (shouldFollowLifecycle()) {
            handler?.sendEmptyMessage(STOP)
        }
        TelemetryManager.Instance?.invoke("onPause", null)
    }

    fun onResume() {
        if (shouldFollowLifecycle()) {
            handler?.sendEmptyMessage(START)
        }
        TelemetryManager.Instance?.invoke("onResume", null)
    }

    fun shouldFollowLifecycle() : Boolean{
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP || camera is Camera1TextureComponent){
            if (!shouldRun.get()) {
                return false
            }
            true
        }
        else{
            false
        }
    }

    class InitializationException : Exception()

    companion object {
        fun initDependencies(context: Context, done: () -> Unit) {
            TelemetryManager.init(context.applicationContext)
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
