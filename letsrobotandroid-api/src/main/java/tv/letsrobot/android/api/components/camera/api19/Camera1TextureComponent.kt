package tv.letsrobot.android.api.components.camera.api19

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.Camera
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.os.Build
import android.util.Log
import android.view.TextureView
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import tv.letsrobot.android.api.components.camera.TextureViewCameraBaseComponent
import tv.letsrobot.android.api.models.CameraSettings
import tv.letsrobot.android.api.utils.GLTools
import tv.letsrobot.android.api.utils.SurfaceTextureUtils
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

    private var r: Rect? = null
    private var camera : android.hardware.Camera? = null
    private var _widthV1 = 0
    private var _heightV1 = 0
    private var eglDisplay : EGLDisplay? = null
    private var eglConfig: EGLConfig? = null
    private var eglContext: EGLContext? = null
    private var eglSurface: EGLSurface? = null
    private var mStManager : SurfaceTextureUtils.SurfaceTextureManager? = null

    init {
        Log.v("CameraAPI", "init")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            eglDisplay = GLTools.newDisplay()
            eglConfig = GLTools.newConfig(eglDisplay!!, true)
            eglContext = GLTools.newContext(eglDisplay!!, eglConfig!!)
            eglSurface = GLTools.newSurface(eglDisplay!!, eglConfig!!, 640, 480)
            GLTools.makeCurrent(eglDisplay!!, eglSurface!!, eglContext!!)
        }
        mStManager = SurfaceTextureUtils.SurfaceTextureManager()
        init()
    }

    override fun onPreviewFrame(b: ByteArray?, camera: android.hardware.Camera?) {
        if (_widthV1 == 0 || _heightV1 == 0) {
            camera?.parameters?.let {
                val size = it.previewSize
                _widthV1 = size.width
                _heightV1 = size.height
                r = Rect(0, 0, _widthV1, _heightV1)
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
                    p.setPreviewSize(640, 480)
                    it.parameters = p
                    it.setPreviewTexture(mStManager?.surfaceTexture!!)
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
