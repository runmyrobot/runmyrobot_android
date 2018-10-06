package tv.letsrobot.android.api.components.api19

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import tv.letsrobot.android.api.components.CameraBaseComponent
import java.io.IOException


/**
 * Class that contains only the camera components for streaming to letsrobot.tv
 *
 * To make this functional, pass in cameraId and a valid SurfaceHolder to a Core.Builder instance
 *
 * This will grab the camera password automatically from config file
 *
 * Does not support USB webcams
 */
class CameraComponent
/**
 * Init camera object.
 * @param context Needed to access the camera
 * @param cameraId camera id for robot
 */
constructor(context: Context, cameraId: String, val holder: SurfaceHolder) : CameraBaseComponent(context, cameraId), FFmpegExecuteResponseHandler, android.hardware.Camera.PreviewCallback, SurfaceHolder.Callback {
    private lateinit var r: Rect

    init {
        holder.addCallback(this)
    }

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
        push(b, ImageFormat.NV21, r)
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

    override fun disableInternal(){
        // Setting this to false will prevent the preview from executing code, which will starve FFmpeg
        // And sever the stream
        streaming.set(false)
    }

    private var camera : android.hardware.Camera? = null

    private var surface = false

    override fun surfaceCreated(holder: SurfaceHolder) {
        surface = true
        camera = Camera.open()
        camera?.setDisplayOrientation(90)
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
}
