package tv.letsrobot.android.api.components.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.TextureView
import tv.letsrobot.android.api.models.CameraSettings

/**
 * Created by Brendon on 10/6/2018.
 */
abstract class TextureViewCameraBaseComponent(context: Context, settings: CameraSettings, val textureView: TextureView) : CameraBaseComponent(context, settings), TextureView.SurfaceTextureListener {
    protected var surfaceAvailable = false
    protected abstract fun setupCamera()
    protected abstract fun releaseCamera()

    open fun init(){
        textureView.surfaceTextureListener = this
        if(textureView.isAvailable){
            Log.v("CameraAPI", "isAvailable")
            surfaceAvailable = true
            setupCamera()
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        releaseCamera()
        surfaceAvailable = false
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        surfaceAvailable = true
        setupCamera()
    }
}