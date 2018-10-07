package tv.letsrobot.android.api.components.api19

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Log
import android.view.TextureView
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
class Camera1Component
/**
 * Init camera object.
 * @param context Needed to access the camera
 * @param cameraId camera id for robot
 */
constructor(context: Context, cameraId: String, val textureView: TextureView) : CameraBaseComponent(context, cameraId), FFmpegExecuteResponseHandler, android.hardware.Camera.PreviewCallback, TextureView.SurfaceTextureListener {

    private lateinit var r: Rect
    private var camera : android.hardware.Camera? = null

    private var surfaceAvailable = false

    init {
        Log.v("CameraAPI", "init")
        textureView.surfaceTextureListener = this
        if(textureView.isAvailable){
            Log.v("CameraAPI", "isAvailable")
            surfaceAvailable = true
            camera = Camera.open()
            camera?.setDisplayOrientation(90)
        }
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
        Log.v("CameraAPI", "setupCam")
        if (!cameraActive.get() && surfaceAvailable) {
            Log.v("CameraAPI", "!cameraActive.get() && surfaceAvailable")
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
                    it.setPreviewTexture(textureView.surfaceTexture)
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

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        cameraActive.set(false)
        surfaceAvailable = false
        previewRunning = false
        camera?.stopPreview()
        camera?.setPreviewCallback (null)
        camera?.release()
        camera = null
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        surfaceAvailable = true
        camera = Camera.open()
        camera?.setDisplayOrientation(90)
        setupCam()
    }
}
