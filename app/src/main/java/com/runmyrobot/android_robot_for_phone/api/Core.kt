package com.runmyrobot.android_robot_for_phone.api

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.SurfaceHolder
import com.runmyrobot.android_robot_for_phone.BuildConfig
import com.runmyrobot.android_robot_for_phone.control.ControllerMessageManager
import com.runmyrobot.android_robot_for_phone.myrobot.RobotComponentList
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.util.*
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
    private var camera: CameraComponent? = null
    private var audio: AudioComponent? = null
    private var robotController: RobotControllerComponent? = null
    private var textToSpeech: TextToSpeechComponent? = null
    private var externalComponents: ArrayList<Component>? = null
    private val onControllerTimeout = fun(_: Any?) {
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
                Core.LogLevel.TRACE -> Log.d(TAG, message, Exception())
                Core.LogLevel.INFO -> Log.i(TAG, message)
                Core.LogLevel.DEBUG -> Log.d(TAG, message)
                Core.LogLevel.WARNING -> Log.w(TAG, message)
                Core.LogLevel.ERROR -> Log.e(TAG, message, Exception())
                Core.LogLevel.NONE -> {
                }
            }
        }
    }

    private var handler: Handler? = null

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper){
            msg ->
            Log.d(TAG, "handleMessage")
            when (msg.what) {
                START -> enableInternal()
                STOP -> disableInternal()
                QUEUE_UPDATE_TO_SERVER -> handler?.postDelayed(onUpdateServer, 1000)
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
            ipInfo.put("ip", "169.254.25.110")
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
        log(LogLevel.INFO, "starting core...")
        Thread{
            camera?.enable()
        }.start()
        Thread{
            audio?.enable()
        }.start()

        robotController?.enable()
        textToSpeech?.enable()
        for (component in externalComponents!!) {
            component.enable()
        }
        appServerSocket = IO.socket("http://letsrobot.tv:8022")
        appServerSocket?.connect()
        appServerSocket?.on(Socket.EVENT_CONNECT_ERROR){
            Log.d(TAG, "appServerSocket EVENT_CONNECT_ERROR")
        }
        appServerSocket?.on(Socket.EVENT_CONNECT){
            Log.d(TAG, "appServerSocket is connected!")
            appServerSocket?.emit("identify_robot_id", robotId)
        }
        //Ugly way of doing timeouts. Should find a better way
        ControllerMessageManager.subscribe("messageTimeout", onControllerTimeout)
        handler?.postDelayed(onUpdateServer, 1000)
        log(LogLevel.INFO, "core is started!")
    }

    private fun disableInternal() {
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
        ControllerMessageManager.unsubscribe("messageTimeout", onControllerTimeout)
        log(LogLevel.INFO, "core is shut down!")
    }

    /**
     * Enable the LetsRobot android core. This will start from scratch.
     * Nothing except settings have been initialized before this call.
     * @return true if successful, false if already started
     */
    fun enable(): Boolean {
        if (running.getAndSet(true)) {
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
        if (!running.getAndSet(false)) {
            return false
        }
        handler?.sendEmptyMessage(STOP)
        return true
    }

    fun onPause() {
        if (!running.get()) {
            return
        }
        handler?.sendEmptyMessage(STOP)
    }

    fun onResume() {
        if (!running.get()) {
            return
        }
        handler?.sendEmptyMessage(START)
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
         * Id that should be used to receive chat messages from server
         */
        var robotId: String? = null

        /**
         * Id for the camera to use to send video to the website
         */
        var cameraId: String? = null

        /**
         * Should chat messages be piped through the android speaker?
         */
        var useTTS = false
        var holder: SurfaceHolder? = null

        /**
         * Build a configured instance of Core.
         * @return Core
         * @throws InitializationException
         */
        @Throws(Core.InitializationException::class)
        fun build(): Core {
            RobotComponentList.init(context)
            //TODO define preconditions that will throw errors

            //RobotId MUST be defined, cameraId can be ignored
            if (robotId == null && cameraId == null || robotId == null) {
                throw InitializationException()
            }
            val core = Core(robotId!!, cameraId)
            robotId?.let{
                core.robotController = RobotControllerComponent(it)
            }
            cameraId?.let{
                core.audio = AudioComponent(context, cameraId!!)
                holder?.let {
                    core.camera = CameraComponent(context, cameraId!!, holder!!)
                }
            }
            if (useTTS) {
                core.textToSpeech = TextToSpeechComponent(context, BuildConfig.ROBOT_ID)
            }
            //Set the log level
            core.logLevel = logLevel

            //Get list of external components, such as LED code, or motor control
            core.externalComponents = RobotComponentList.components
            return core
        }
    }

    class InitializationException : Exception()

    companion object {
        private const val START = 1
        private const val STOP = 2
        private const val QUEUE_UPDATE_TO_SERVER = 3
        private const val TAG = "RobotCore"
        private val running = AtomicBoolean(false)

    }
}
