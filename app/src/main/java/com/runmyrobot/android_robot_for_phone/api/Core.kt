package com.runmyrobot.android_robot_for_phone.api

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.SurfaceHolder
import com.runmyrobot.android_robot_for_phone.control.EventManager
import com.runmyrobot.android_robot_for_phone.control.EventManager.Companion.TIMEOUT
import com.runmyrobot.android_robot_for_phone.control.communicationInterfaces.CommunicationComponent
import com.runmyrobot.android_robot_for_phone.control.communicationInterfaces.CommunicationType
import com.runmyrobot.android_robot_for_phone.control.deviceProtocols.ProtocolType
import com.runmyrobot.android_robot_for_phone.myrobot.RobotComponentList
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
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
    var camera: CameraComponent? = null
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

    init {
        EventManager.invoke(javaClass.name, ComponentStatus.DISABLED)
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
        if(running.getAndSet(true)) return //already enabled
        EventManager.invoke(javaClass.name, ComponentStatus.CONNECTING)
        log(LogLevel.INFO, "starting core...")
        val client = OkHttpClient()
        val call = client.newCall(Request.Builder().url(String.format("https://letsrobot.tv/get_robot_owner/%s", robotId)).build())
        try {
            val response = call.execute()
            if (response.body() != null) {
                val `object` = JSONObject(response.body()!!.string())
                owner = `object`.getString("owner")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
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
            EventManager.invoke(javaClass.name, ComponentStatus.ERROR)
        }
        appServerSocket?.on(Socket.EVENT_CONNECT){
            Log.d(TAG, "appServerSocket is connected!")
            EventManager.invoke(javaClass.name, ComponentStatus.STABLE)
            appServerSocket?.emit("identify_robot_id", robotId)
        }
        appServerSocket?.on(Socket.EVENT_DISCONNECT){
            EventManager.invoke(javaClass.name, ComponentStatus.DISABLED)
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
        EventManager.invoke(javaClass.name, ComponentStatus.DISABLED)
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
        if (!shouldRun.get()) {
            return
        }
        handler?.sendEmptyMessage(STOP)
    }

    fun onResume() {
        if (!shouldRun.get()) {
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
         * Id for the camera to use to send video to the website
         */
        var cameraId: String? = null

        /**
         * Should chat messages be piped through the android speaker?
         */
        var useTTS = false

        var useMic = false
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
            //Get list of external components, such as LED code, or more customized motor control
            core.externalComponents = RobotComponentList.components
            robotId?.let{
                core.robotController = RobotControllerComponent(context, it)
                //Setup our protocol, if it exists
                val protocolClass = protocol?.getInstantiatedClass(context)
                protocolClass?.let {
                    //Add it to the component list
                    core.externalComponents?.add(it)
                }
                //Setup our communication, if it exists
                val communicationClass = communication?.getInstantiatedClass
                communicationClass?.let {
                    //Add it to the component list
                    core.externalComponents?.add(CommunicationComponent(context, it))
                }
            }
            cameraId?.let{
                if(useMic) {
                    core.audio = AudioComponent(context, cameraId!!)
                }
                holder?.let {
                    core.camera = CameraComponent(context, cameraId!!, holder!!)
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
