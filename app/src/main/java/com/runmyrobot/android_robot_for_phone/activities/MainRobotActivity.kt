package com.runmyrobot.android_robot_for_phone.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.runmyrobot.android_robot_for_phone.R
import com.runmyrobot.android_robot_for_phone.RobotApplication
import com.runmyrobot.android_robot_for_phone.robot.CustomComponentExample
import kotlinx.android.synthetic.main.activity_main_robot.*
import tv.letsrobot.android.api.Core
import tv.letsrobot.android.api.components.*
import tv.letsrobot.android.api.interfaces.Component
import tv.letsrobot.android.api.utils.PhoneBatteryMeter
import tv.letsrobot.android.api.utils.StoreUtil

/**
 * Main activity for the robot. It has a simple camera UI and a button to connect and disconnect.
 * For camera functionality, this activity needs to have a
 * SurfaceView to pass to the camera component via the Builder
 */
class MainRobotActivity : Activity(), Runnable {
    val components = ArrayList<Component>() //arraylist of custom components

    override fun run() {
        if (recording){
            fakeSleepView.visibility = View.VISIBLE
            fakeSleepView.setBackgroundColor(resources.getColor(R.color.black))
        }
    }

    private var recording = false
    var core: Core? = null
    lateinit var handler : Handler
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PhoneBatteryMeter.getReceiver(applicationContext) //Setup phone battery monitor TODO integrate with component
        handler = Handler(Looper.getMainLooper())

        //Full screen with no title
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main_robot) //Set the layout to use for activity

        //Setup a custom component
        val component = CustomComponentExample(applicationContext, "customString")
        components.add(component) //add to custom components list

        recordButtonMain.setOnClickListener{ //Hook up power button to start the connection
            if (recording) {
                recording = false
                core?.disable() //Disable core if we hit the button to disable recording
                Log.v(LOGTAG, "Recording Stopped")
            } else {
                recording = true
                if(StoreUtil.getScreenSleepOverlayEnabled(this)){
                    handler.postDelayed(this, 10000)
                }
                core?.enable() //enable core if we hit the button to enable recording

                Log.v(LOGTAG, "Recording Started")
            }
        }
        settingsButtonMain.setOnClickListener {
            finish() //Stop activity
            startActivity(Intent(this, ManualSetupActivity::class.java))
            core?.disable()
            core = null
        }

        //Black overlay to try to conserve power on AMOLED displays
        fakeSleepView.setOnTouchListener { view, motionEvent ->
            if(StoreUtil.getScreenSleepOverlayEnabled(this)) {
                fakeSleepView.setBackgroundColor(Color.TRANSPARENT)
                handler.removeCallbacks(this)
                handler.postDelayed(this, 10000) //10 second delay
                //TODO disable touch if black screen is up
            }
            return@setOnTouchListener false
        }
        initIndicators()
    }

    private fun initIndicators() { //Indicators for Core Services
        cloudStatusIcon.setComponentInterface(Core::class.java.name)
        cameraStatusIcon.setComponentInterface(CameraComponent::class.java.name)
        robotStatusIcon.setComponentInterface(RobotControllerComponent::class.java.name)
        micStatusIcon.setComponentInterface(AudioComponent::class.java.name)
        ttsStatusIcon.setComponentInterface(TextToSpeechComponent::class.java.name)
        robotMotorStatusIcon.setComponentInterface(CommunicationComponent::class.java.name)
        //To add more, add another icon to the layout file somewhere and pass in your component
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

    override fun onDestroy() {
        super.onDestroy()
        PhoneBatteryMeter.destroyReceiver(applicationContext)
    }

    /**
     * Create the robot Core object. This will handle enabling all components on its own thread.
     * Core.Builder is the only way to create the Core to make sure settings do not change wile the robot is running
     */
    private fun createCore() {
        val builder = Core.Builder(applicationContext) //Initialize the Core Builder
        //Attach the SurfaceView holder to render the camera to
        builder.holder = cameraSurfaceView.holder
        builder.robotId = StoreUtil.getRobotId(this) //Pass in our Robot ID
        if(StoreUtil.getCameraEnabled(this)){
            builder.cameraId = StoreUtil.getCameraId(this) //Pass in our Camera ID
        }
        //TODO set camera pass
        builder.useTTS = StoreUtil.getTTSEnabled(this)
        builder.useMic = StoreUtil.getMicEnabled(this)
        builder.protocol = StoreUtil.getProtocolType(this)
        builder.communication = StoreUtil.getCommunicationType(this)
        builder.externalComponents = components //pass in arrayList of custom components
        try {
            core = builder.build() //Retrieve the built Core instance
        } catch (e: Core.InitializationException) {
            RobotApplication.Instance.reportError(e) // Reports an initialization error to application
            e.printStackTrace()
        }
    }

    companion object {
        const val LOGTAG = "MainRobot"
    }
}