package tv.letsrobot.android.api.components.camera.api19

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.Camera
import android.util.Log
import android.view.TextureView
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import tv.letsrobot.android.api.components.camera.TextureViewCameraBaseComponent
import tv.letsrobot.android.api.models.CameraSettings
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
class Camera1TextureComponent
/**
 * Init camera object.
 * @param context Needed to access the camera
 * @param cameraId camera id for robot
 */
constructor(context: Context, settings: CameraSettings, textureView: TextureView) : TextureViewCameraBaseComponent(context, settings, textureView), FFmpegExecuteResponseHandler, android.hardware.Camera.PreviewCallback{

    private lateinit var r: Rect
    private var camera : android.hardware.Camera? = null

    init {
        Log.v("CameraAPI", "init")
        init()
    }

    override fun onPreviewFrame(b: ByteArray?, camera: android.hardware.Camera?) {
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

    override fun releaseCamera() {
        cameraActive.set(false)
        surfaceAvailable = false
        previewRunning = false
        camera?.stopPreview()
        camera?.setPreviewCallback (null)
        camera?.release()
        camera = null
    }

    override fun setupCamera(){
        Log.v("CameraAPI", "setupCamera")
        camera ?: kotlin.run {
            camera = Camera.open()
            camera?.setDisplayOrientation(90)
        }
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
                    p.setPreviewSize(width, height)
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
}
