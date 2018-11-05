package tv.letsrobot.android.api.components.camera

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.ImageFormat
import android.os.IBinder
import android.util.Log
import com.ford.openxc.webcam.webcam.WebcamService
import tv.letsrobot.android.api.models.CameraSettings

/**
 * Created by Brendon on 10/13/2018.
 */
class ExtCameraInterface(context: Context, settings: CameraSettings) : CameraBaseComponent(context, settings), Runnable{

    private var mWebcamService: WebcamService? = null
    private val mServiceSyncToken = java.lang.Object()

    override fun enableInternal() {
        super.enableInternal()
        startPreview(WebcamService.VIDEO) //TODO make user controllable
    }

    override fun disableInternal() {
        super.disableInternal()
        stopPreview()
    }

    fun startPreview(video: String) {
        //        stopPreview();
        val intent = Intent(context, WebcamService::class.java)
        intent.putExtra("video", video)
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        Thread(this).start()
    }

    fun stopPreview() {
        if (mWebcamService != null) {
            Log.w(TAG, "Unbinding from webcam manager")
            context.unbindService(mConnection)
            mWebcamService = null
        }
    }

    override fun run() {
        while (enabled.get()) {
            try {
                synchronized(mServiceSyncToken) {
                    if (mWebcamService == null) {
                        mServiceSyncToken.wait()
                    }
                    mWebcamService?.frame?.let {
                        push(it, ImageFormat.JPEG, null)
                    }
                }
            } catch (e: InterruptedException) {
                break
            }
        }
    }

    companion object {
        const val TAG = "ExtCameraInterface"
    }


    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            Log.w(TAG, "Bound to WebcamManager")
            synchronized(mServiceSyncToken) {
                mWebcamService = (service as WebcamService.WebcamBinder).service
                mServiceSyncToken.notify()
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Log.w(TAG, "WebcamManager disconnected unexpectedly")
            synchronized(mServiceSyncToken) {
                mWebcamService = null
                mServiceSyncToken.notify()
            }
        }
    }
}
