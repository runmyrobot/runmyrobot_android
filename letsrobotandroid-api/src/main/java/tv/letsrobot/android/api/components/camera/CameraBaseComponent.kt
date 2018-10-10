package tv.letsrobot.android.api.components.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.HandlerThread
import android.util.Log
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.google.common.util.concurrent.RateLimiter
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.interfaces.Component
import tv.letsrobot.android.api.utils.StoreUtil
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Abstracted class for different camera implementations
 */
abstract class CameraBaseComponent(context: Context, val cameraId: String) : Component(context), FFmpegExecuteResponseHandler {
    internal var ffmpegRunning = AtomicBoolean(false)
    protected var ffmpeg : FFmpeg = FFmpeg.getInstance(context)
    protected var UUID = java.util.UUID.randomUUID().toString()
    var process : Process? = null
    protected var port: String? = null
    protected var host: String? = null
    protected var streaming = AtomicBoolean(false)
    protected var previewRunning = false
    protected var width = 0
    protected var height = 0
    protected var limiter = RateLimiter.create(30.0)
    protected val cameraActive = AtomicBoolean(false)
    protected var successCounter: Int = 0

    private var handler = HandlerThread("CameraProcessing")

    init {
        handler.start()
    }

    override fun enableInternal() {
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
            status = ComponentStatus.ERROR
        }
        else
            streaming.set(true)
    }

    override fun disableInternal() {
        streaming.set(false)
    }

    fun push(b : Any?, format : Int, r : Rect?){
        if(!streaming.get()) return
        if(!limiter.tryAcquire()) return
        if (!ffmpegRunning.getAndSet(true)) {
            bootFFMPEG()
        }
        process?.let { _process ->
            (b as? ByteArray)?.let {
                when(format){
                    ImageFormat.JPEG -> {
                        _process.outputStream.write(b)
                    }
                    ImageFormat.NV21 -> {
                        val im = YuvImage(b, format, width, height, null)
                        try {
                            im.compressToJpeg(r, 100, _process.outputStream)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    else -> {
                    }
                }
            } ?: (b as? Bitmap)?.let {
                b.compress(Bitmap.CompressFormat.JPEG, 100, _process.outputStream)
            }
        }
    }

    fun byteArrayPush(b : ByteArray, format : Int, r : Rect){

    }

    fun bootFFMPEG(){
        if(!streaming.get()){
            ffmpegRunning.set(false)
            status = ComponentStatus.DISABLED
            return
        }
        successCounter = 0
        status = ComponentStatus.CONNECTING
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            val xres = "640"
            val yres = "480"

            val rotationOption = StoreUtil.getOrientation(context).ordinal //leave blank
            val builder = StringBuilder()
            for (i in 0..rotationOption){
                if(i == 0) builder.append("-vf transpose=1")
                else builder.append(",transpose=1")
            }
            print("\"$builder\"")
            val kbps = "20"
            val video_host = host
            val video_port = port
            val stream_key = StoreUtil.getCameraPass(context)
            //TODO hook up with bitrate and resolution prefs
            val command = "-f image2pipe -codec:v mjpeg -i - -f mpegts -framerate 30 -codec:v mpeg1video -b:v 10k -bf 0 -muxdelay 0.001 -tune zerolatency -preset ultrafast -pix_fmt yuv420p $builder http://$video_host:$video_port/$stream_key/$xres/$yres/"
            ffmpeg.execute(UUID, null, command.split(" ").toTypedArray(), this)
        } catch (e: FFmpegCommandAlreadyRunningException) {
            e.printStackTrace()
            // Handle if FFmpeg is already running
        }
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
        successCounter++
        status = when {
            successCounter > 5 -> ComponentStatus.STABLE
            successCounter > 2 -> ComponentStatus.INTERMITTENT
            else -> ComponentStatus.CONNECTING
        }
    }

    override fun onFailure(message: String?) {
        Log.e(LOGTAG, "progress : $message")
        status = ComponentStatus.ERROR
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
        process?.destroy()
        process = null
        status = ComponentStatus.DISABLED
    }

    override fun onProcess(p0: Process?) {
        @Suppress("ConstantConditionIf")
        if(shouldLog)
            Log.d(LOGTAG, "onProcess")
        this.process = p0
    }

    companion object {
        const val LOGTAG = "Camera1TextureComponent"
        protected const val shouldLog = true
    }
}