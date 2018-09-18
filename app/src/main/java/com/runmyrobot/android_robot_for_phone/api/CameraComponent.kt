package com.runmyrobot.android_robot_for_phone.api

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.google.common.util.concurrent.RateLimiter
import com.runmyrobot.android_robot_for_phone.RobotApplication
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Class that contains only the camera components for streaming to letsrobot.tv
 *
 * To make this functional, pass in cameraId and a valid SurfaceHolder to a Core.Builder instance
 *
 * This will grab the camera password automatically from config file
 */
class CameraComponent
/**
 * Init camera object.
 * @param context Needed to access the camera
 * @param cameraId camera id for robot
 */
constructor(context: Context, val cameraId: String, val holder: SurfaceHolder) : Component(context), FFmpegExecuteResponseHandler, android.hardware.Camera.PreviewCallback, SurfaceHolder.Callback {
    internal var ffmpegRunning = AtomicBoolean(false)
    var ffmpeg : FFmpeg
    init {
        holder.addCallback(this)
        ffmpeg = FFmpeg.getInstance(context)
    }
    var UUID = java.util.UUID.randomUUID().toString()
    var process : Process? = null
    var port: String? = null
    var host: String? = null
    var streaming = AtomicBoolean(false)
    var previewRunning = false
    override fun enable() {
        super.enable()
        try {
            val client = OkHttpClient.Builder()
                    .build()
            var call = client.newCall(Request.Builder().url(String.format("https://letsrobot.tv/get_video_port/%s", cameraId)).build())
            var response = call.execute()
            if (response.body() != null) {
                val `object` = JSONObject(response.body()!!.string())
                Log.d("ROBOT", `object`.toString())
                port = `object`.getString("mpeg_stream_port")
            }
            call = client.newCall(Request.Builder().url(String.format("https://letsrobot.tv/get_websocket_relay_host/%s", cameraId)).build())
            response = call.execute()
            if (response.body() != null) {
                val `object` = JSONObject(response.body()!!.string())
                Log.d("ROBOT", `object`.toString())
                host = `object`.getString("host")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        if(host == null || port == null){
            throw Exception("Unable to form URL")
        }
        streaming.set(true)
    }

    fun bootFFMPEG(){
        if(!streaming.get()){
            ffmpegRunning.set(false)
            return
        }

        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            val xres = "640"
            val yres = "480"

            val rotation_option = "" //leave blank
            val kbps = "20"
            val video_host = host
            val video_port = port
            val stream_key = RobotApplication.Instance.getCameraPass()
            //TODO hook up with bitrate and resolution prefs
            val command = "-f image2pipe -codec:v mjpeg -i - -f mpegts -framerate 30 -codec:v mpeg1video -b:v 10k -bf 0 -muxdelay 0.001 -tune zerolatency -preset ultrafast -pix_fmt yuv420p -vf transpose=1 http://$video_host:$video_port/$stream_key/$xres/$yres/"
            ffmpeg.execute(UUID, null, command.split(" ").toTypedArray(), this)
        } catch (e: FFmpegCommandAlreadyRunningException) {
            e.printStackTrace()
            // Handle if FFmpeg is already running
        }
    }

    var width = 0
    var height = 0
    var limiter = RateLimiter.create(30.0)

    private lateinit var r: Rect

    override fun onPreviewFrame(b: ByteArray?, camera: android.hardware.Camera?) {
        if(!streaming.get()) return
        if(!limiter.tryAcquire()) return
        if (width == 0 || height == 0) {
            camera?.parameters?.let {
                val size = it.previewSize
                width = size.width
                height = size.height
                r = Rect(0, 0, width, height)
            }
        }
        if (!ffmpegRunning.getAndSet(true)) {
            bootFFMPEG()
        }
        process?.let {
            val im = YuvImage(b, ImageFormat.NV21, width, height, null)
            try {
                im.compressToJpeg(r, 100, it.outputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupCam(){
        if (!cameraActive.get() && surface) {
            camera?.let {
                if (previewRunning) {
                    it.stopPreview()
                }

                try {
                    val p = it.parameters
                    val previewSizes = p.supportedPreviewSizes

                    // You need to choose the most appropriate previewSize for your app
                    val previewSize = previewSizes.get(0) // .... select one of previewSizes here
                    //p.setPreviewSize(previewSize.width, previewSize.height);
                    p.setPreviewSize(640, 480)
                    it.parameters = p

                    it.setPreviewDisplay(holder)
                    it.setPreviewCallback(this)
                    Log.v(LOGTAG, "startPreview")
                    it.startPreview()
                    previewRunning = true
                    cameraActive.set(true)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun disable() {
        super.disable()
        // Setting this to false will prevent the preview from executing code, which will starve FFmpeg
        // And sever the stream
        streaming.set(false)
    }

    override fun onStart() {
        ffmpegRunning.set(true)
        @Suppress("ConstantConditionIf")
        if(shouldLog)
            Log.d(LOGTAG, "onStart")
    }

    override fun onProgress(message: String?) {
        @Suppress("ConstantConditionIf")
        if(shouldLog)
            Log.d(LOGTAG, "onProgress : $message")
    }

    override fun onFailure(message: String?) {
        Log.e(LOGTAG, "progress : $message")
    }

    override fun onSuccess(message: String?) {
        @Suppress("ConstantConditionIf")
        if(shouldLog)
            Log.d(LOGTAG, "onSuccess : $message")
    }

    override fun onFinish() {
        @Suppress("ConstantConditionIf")
        if(shouldLog)
            Log.d(LOGTAG, "onFinish")
        ffmpegRunning.set(false)
    }

    override fun onProcess(p0: Process?) {
        @Suppress("ConstantConditionIf")
        if(shouldLog)
            Log.d(LOGTAG, "onProcess")
        this.process = p0
    }

    private var camera : android.hardware.Camera? = null

    private var surface = false

    override fun surfaceCreated(holder: SurfaceHolder) {
        surface = true
        camera = Camera.open()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        setupCam()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.v("CameraAPI", "surfaceDestroyed")
        cameraActive.set(false)
        surface = false
        previewRunning = false
        camera?.stopPreview()
        camera?.setPreviewCallback (null)
        camera?.release()
        camera = null
    }

    companion object {
        private const val LOGTAG = "CameraComponent"
        private const val shouldLog = false
        private val cameraActive = AtomicBoolean(false)
    }
}
