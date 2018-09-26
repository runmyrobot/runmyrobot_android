package com.runmyrobot.android_robot_for_phone.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.runmyrobot.android_robot_for_phone.R
import com.runmyrobot.android_robot_for_phone.RobotApplication
import com.runmyrobot.android_robot_for_phone.api.*
import com.runmyrobot.android_robot_for_phone.control.communicationInterfaces.CommunicationComponent
import com.runmyrobot.android_robot_for_phone.utils.StoreUtil
import kotlinx.android.synthetic.main.activity_main_robot.*

/**
 * Main activity for the robot. It has a simple camera UI and a button to connect and disconnect.
 * For camera functionality, this activity needs to have a
 * SurfaceView to pass to the camera component via the Builder
 */
class MainRobotActivity : Activity(), Runnable {
    override fun run() {
        if (recording){
            fakeSleepView.visibility = View.VISIBLE
        }
    }

    private var recording = false
    var core: Core? = null
    lateinit var handler : Handler
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(Looper.getMainLooper())
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main_robot)
        recordButtonMain.setOnClickListener{
            if (recording) {
                recording = false
                core?.disable() //Disable core if we hit the button to disable recording
                Log.v(LOGTAG, "Recording Stopped")
            } else {
                recording = true
                if(StoreUtil.getScreenSleepOverlayEnabled(this)){
                    handler.postDelayed(this, 5000)
                }
                core?.enable() //enable core if we hit the button to enable recording

                Log.v(LOGTAG, "Recording Started")
            }
        }
        settingsButtonMain.setOnClickListener {
            finish()
            startActivity(Intent(this, ManualSetupActivity::class.java))
            core?.disable()
            core = null
        }
        fakeSleepView.setOnTouchListener { view, motionEvent ->
            if(StoreUtil.getScreenSleepOverlayEnabled(this)) {
                fakeSleepView.visibility = View.INVISIBLE
                handler.removeCallbacks(this)
                handler.postDelayed(this, 5000)
            }
            return@setOnTouchListener false
        }
        initIndicators()
    }

    private fun initIndicators() {
        cloudStatusIcon.setComponentInterface(Core::class.java.name)
        cameraStatusIcon.setComponentInterface(CameraComponent::class.java.name)
        robotStatusIcon.setComponentInterface(RobotControllerComponent::class.java.name)
        micStatusIcon.setComponentInterface(AudioComponent::class.java.name)
        ttsStatusIcon.setComponentInterface(TextToSpeechComponent::class.java.name)
        robotMotorStatusIcon.setComponentInterface(CommunicationComponent::class.java.name)
    }

    override fun onPause() {
        super.onPause()
        core?.onPause()
    }

    override fun onResume() {
        super.onResume()
        //Call onResume to re-enable it if needed. If null, create it
        core?.onResume() ?: createCore()
    }

    private fun createCore() {
        val builder = Core.Builder(applicationContext) //Initialize the Core Builder
        //Attach the SurfaceView holder to render the camera to
        builder.holder = cameraSurfaceView.holder
        builder.robotId = StoreUtil.getRobotId(this) //Pass in our Robot ID
        if(StoreUtil.getCameraEnabled(this)){
            builder.cameraId = StoreUtil.getCameraId(this) //Pass in our Camera ID
        }
        builder.useTTS = StoreUtil.getTTSEnabled(this)
        builder.useMic = StoreUtil.getMicEnabled(this)
        builder.protocol = StoreUtil.getProtocolType(this)
        builder.communication = StoreUtil.getCommunicationType(this)
        try {
            core = builder.build() //Retrieve the built Core instance
        } catch (e: Core.InitializationException) {
            RobotApplication.Instance.reportError(e)
            e.printStackTrace()
        }
    }

    companion object {
        const val LOGTAG = "MainRobot"
    }
}