package com.runmyrobot.android_robot_for_phone.api

import android.content.Context
import android.graphics.Camera
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Environment
import android.util.Log
import android.view.SurfaceHolder

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import com.runmyrobot.android_robot_for_phone.RobotApplication
import com.runmyrobot.android_robot_for_phone.activities.MJPEGFFMPEGTest.LOGTAG
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import okhttp3.CertificatePinner



/**
 * Class that contains only the camera components for streaming to letsrobot.tv
 *
 * Created by Brendon on 8/25/2018.
 */
class CameraComponent
/**
 * Init camera object.
 * @param context Needed to access the camera
 * @param cameraId camera id for robot
 */
constructor(val context: Context, val cameraId: String, val holder: SurfaceHolder) : FFmpegExecuteResponseHandler, android.hardware.Camera.PreviewCallback, SurfaceHolder.Callback {
    internal var ffmpegRunning = AtomicBoolean(false)

    init {
        holder.addCallback(this)
    }
    var process : Process? = null
    var port: String? = null
    var host: String? = null
    var recording = false
    var previewRunning = false
    lateinit var ffmpeg : FFmpeg
    fun enable() {
        ffmpeg = FFmpeg.getInstance(context)
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
        recording = true
    }

    fun bootFFMPEG(){
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            val xres = "640"
            val yres = "480"

            val rotation_option = "" //leave blank
            val kbps = "20"
            val video_host = host
            val video_port = port
            val stream_key = RobotApplication.getCameraPass()///dev/video${video_device_number}
//Do not hardcode "/sdcard/"; use `Environment.getExternalStorageDirectory().getPath()` instead
            val command = "-f image2pipe -codec:v mjpeg -i - -f mpegts -framerate 25 -codec:v mpeg1video -b:v ${kbps}k -bf 0 -muxdelay 0.001 -tune zerolatency -preset ultrafast -pix_fmt yuv420p http://$video_host:$video_port/$stream_key/$xres/$yres/"
            //val command = "-f image2pipe -codec:v mjpeg -i - -f mpegts -framerate 25 -codec:v mpeg1video -b:v ${kbps}k -bf 0 -muxdelay 0.001 http://$video_host:${video_port}/${stream_key}/${xres}/${yres}/"
            ffmpeg.execute(command.split(" ").toTypedArray(), this)
        } catch (e: FFmpegCommandAlreadyRunningException) {
            // Handle if FFmpeg is already running
        }
    }

    var width = 0
    var height = 0

    override fun onPreviewFrame(b: ByteArray?, camera: android.hardware.Camera?) {
        if (width == 0 || height == 0) {
            camera?.parameters?.let {
                val size = it.previewSize
                width = size.width
                height = size.height
            }
        }
        try {
            if (!ffmpegRunning.getAndSet(true)) {
                bootFFMPEG()
            }
            process?.let {
                val im = YuvImage(b, ImageFormat.NV21, width, height, null)
                val r = Rect(0, 0, width, height)
                im.compressToJpeg(r, 20, it.outputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun disable() {
        recording = false
    }

    override fun onStart() {
        ffmpegRunning.set(true)
    }

    override fun onProgress(message: String?) {

    }

    override fun onFailure(message: String?) {
        Log.e(LOGTAG, message)
    }

    override fun onSuccess(message: String?) {

    }

    override fun onFinish() {
        ffmpegRunning.set(false)
    }

    override fun onProcess(p0: Process?) {
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
}
