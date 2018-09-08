package com.runmyrobot.android_robot_for_phone.api

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
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
constructor(val context: Context, val cameraId: String, val holder: SurfaceHolder) : FFmpegExecuteResponseHandler, android.hardware.Camera.PreviewCallback, SurfaceHolder.Callback {
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
    var recording = false
    var previewRunning = false
    fun enable() {
        try {
            try {
                holder.removeCallback(this)
            } catch (e: Exception) {
            }
            holder.addCallback(this)
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
        recording = true
    }

    fun bootFFMPEG(){
        if(!recording){
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
            val stream_key = RobotApplication.cameraPass
                        //"-f image2pipe -codec:v mjpeg -i - -f mpegts -framerate 30 -codec:v mpeg1video -b:v 10k -bf 0 -muxdelay 0.001 -tune zerolatency -preset ultrafast -pix_fmt yuv420p http://letsrobot.tv:11225/"+ RobotApplication.getCameraPass()+"/%s/%s/";
            val command = "-f image2pipe -codec:v mjpeg -i - -f mpegts -framerate 30 -codec:v mpeg1video -b:v 10k -bf 0 -muxdelay 0.001 -tune zerolatency -preset ultrafast -pix_fmt yuv420p -vf transpose=1 http://$video_host:$video_port/$stream_key/$xres/$yres/"
            //val command = "-f image2pipe -codec:v mjpeg -i - -f mpegts -framerate 25 -codec:v mpeg1video -b:v ${kbps}k -bf 0 -muxdelay 0.001 http://$video_host:${video_port}/${stream_key}/${xres}/${yres}/"
            ffmpeg.execute(UUID, null, command.split(" ").toTypedArray(), this)
        } catch (e: FFmpegCommandAlreadyRunningException) {
            e.printStackTrace()
            // Handle if FFmpeg is already running
        }
    }

    var width = 0
    var height = 0
    var limiter = RateLimiter.create(30.0)

    override fun onPreviewFrame(b: ByteArray?, camera: android.hardware.Camera?) {
        if(!limiter.tryAcquire()) return
        if (width == 0 || height == 0) {
            camera?.parameters?.let {
                val size = it.previewSize
                width = size.width
                height = size.height
            }
        }
        if (!ffmpegRunning.getAndSet(true)) {
            bootFFMPEG()
        }
        process?.let {
            val im = YuvImage(b, ImageFormat.NV21, width, height, null)
            val r = Rect(0, 0, width, height)
            try {
                im.compressToJpeg(r, 100, it.outputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun disable() {
        recording = false
        camera?.stopPreview()
        holder.removeCallback(this)
        camera?.release()
    }

    override fun onStart() {
        ffmpegRunning.set(true)
        Log.d(LOGTAG, "onStart")
    }

    override fun onProgress(message: String?) {
        Log.d(LOGTAG, "onProgress : $message")
    }

    override fun onFailure(message: String?) {
        Log.e(LOGTAG, "progress : $message")
    }

    override fun onSuccess(message: String?) {
        Log.d(LOGTAG, "onSuccess : $message")
    }

    override fun onFinish() {
        Log.d(LOGTAG, "onFinish")
        ffmpegRunning.set(false)
    }

    override fun onProcess(p0: Process?) {
        Log.d(LOGTAG, "onProcess")
        this.process = p0
    }

    private var camera : android.hardware.Camera? = null

    override fun surfaceCreated(holder: SurfaceHolder) {
        camera = android.hardware.Camera.open()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (!recording) {
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
                    /*
                    Log.v(LOGTAG,"Setting up preview callback buffer");
                    previewCallbackBuffer = new byte[(camcorderProfile.videoFrameWidth * camcorderProfile.videoFrameHeight *
                                                        ImageFormat.getBitsPerPixel(p.getPreviewFormat()) / 8)];
                    Log.v(LOGTAG,"setPreviewCallbackWithBuffer");
                    camera.addCallbackBuffer(previewCallbackBuffer);
                    camera.setPreviewCallbackWithBuffer(this);
                    */
                    Log.v(LOGTAG, "startPreview")
                    it.startPreview()
                    previewRunning = true
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.v("CameraAPI", "surfaceDestroyed")
        if (recording) {
            recording = false
        }
        previewRunning = false
        camera?.release()
    }

    companion object {
        private const val LOGTAG = "CameraComponent"
    }
}
