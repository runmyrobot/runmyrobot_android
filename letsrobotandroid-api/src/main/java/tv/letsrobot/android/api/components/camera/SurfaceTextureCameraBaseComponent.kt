package tv.letsrobot.android.api.components.camera

import android.content.Context
import tv.letsrobot.android.api.models.CameraSettings
import tv.letsrobot.android.api.utils.EglCore
import tv.letsrobot.android.api.utils.SurfaceTextureUtils
import javax.microedition.khronos.egl.EGLSurface

/**
 * Creates an off screen surface texture for rendering the camera preview
 *
 * Compatible down to API16
 */
abstract class SurfaceTextureCameraBaseComponent(context: Context, settings: CameraSettings) : CameraBaseComponent(context, settings){
    protected abstract fun setupCamera()
    protected abstract fun releaseCamera()
    protected var eglCore: EglCore? = null
    protected var eglSurface: EGLSurface? = null
    protected var mStManager : SurfaceTextureUtils.SurfaceTextureManager
    init {
        eglCore = EglCore()
        eglSurface = eglCore?.createOffscreenSurface(640, 480)
        eglCore?.makeCurrent(eglSurface)
        mStManager = SurfaceTextureUtils.SurfaceTextureManager()
    }

    override fun enableInternal() {
        super.enableInternal()
        setupCamera()
    }

    override fun disableInternal() {
        super.disableInternal()
        releaseCamera()
    }

    open fun init(){

    }
}