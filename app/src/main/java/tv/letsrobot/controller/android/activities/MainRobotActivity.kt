package tv.letsrobot.controller.android.activities

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
import kotlinx.android.synthetic.main.activity_main_robot.*
import tv.letsrobot.android.api.Core
import tv.letsrobot.android.api.components.AudioComponent
import tv.letsrobot.android.api.components.CommunicationComponent
import tv.letsrobot.android.api.components.RobotControllerComponent
import tv.letsrobot.android.api.components.TextToSpeechComponent
import tv.letsrobot.android.api.components.camera.CameraBaseComponent
import tv.letsrobot.android.api.interfaces.Component
import tv.letsrobot.android.api.interfaces.ILetsRobotControl
import tv.letsrobot.android.api.models.CameraSettings
import tv.letsrobot.android.api.services.LetsRobotControlApi
import tv.letsrobot.android.api.utils.PhoneBatteryMeter
import tv.letsrobot.controller.android.R
import tv.letsrobot.controller.android.RobotApplication
import tv.letsrobot.controller.android.robot.CustomComponentExample
import tv.letsrobot.controller.android.robot.RobotSettingsObject

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
    lateinit var handler : Handler
    lateinit var settings : RobotSettingsObject

    private var api: ILetsRobotControl? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PhoneBatteryMeter.getReceiver(applicationContext) //Setup phone battery monitor TODO integrate with component
        handler = Handler(Looper.getMainLooper())
        settings = RobotSettingsObject.load(this)
        //Full screen with no title
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main_robot) //Set the layout to use for activity

        //Setup a custom component
        val component = CustomComponentExample(applicationContext, "customString")
        components.add(component) //add to custom components list
        api = LetsRobotControlApi.getNewInstance(this)
        api?.connectToService()
        recordButtonMain.setOnClickListener{ //Hook up power button to start the connection
            if (recording) {
                recording = false
                api?.disable()
                Log.v(LOGTAG, "Recording Stopped")
            } else {
                recording = true
                if(settings.screenTimeout){
                    handler.postDelayed(this, 10000)
                }
                api?.enable()

                Log.v(LOGTAG, "Recording Started")
            }
        }
        settingsButtonMain.setOnClickListener {
            api?.disable()
            finish() //Stop activity
            startActivity(Intent(this, ManualSetupActivity::class.java))
        }

        //Black overlay to try to conserve power on AMOLED displays
        fakeSleepView.setOnTouchListener { view, motionEvent ->
            if(settings.screenTimeout) {
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
        cloudStatusIcon.setComponentInterface(Core::class.java.simpleName)
        cameraStatusIcon.setComponentInterface(CameraBaseComponent.EVENTNAME)
        robotStatusIcon.setComponentInterface(RobotControllerComponent::class.java.simpleName)
        micStatusIcon.setComponentInterface(AudioComponent::class.java.simpleName)
        ttsStatusIcon.setComponentInterface(TextToSpeechComponent::class.java.simpleName)
        robotMotorStatusIcon.setComponentInterface(CommunicationComponent::class.java.simpleName)
        //To add more, add another icon to the layout file somewhere and pass in your component
    }

    override fun onPause() {
        super.onPause()
        //api?.onPause() TODO API Pause? Would only apply to legacy devices
    }

    override fun onResume() {
        super.onResume()
        //Call onResume to re-enable it if needed. If null, create it
        //core?.onResume() ?: createCore() TODO API Resume? Would only apply to legacy devices
    }

    override fun onDestroy() {
        super.onDestroy()
        api?.disconnectFromService()
        PhoneBatteryMeter.destroyReceiver(applicationContext)
    }

    /**
     * Create the robot Core object. This will handle enabling all components on its own thread.
     * Core.Builder is the only way to create the Core to make sure settings do not change wile the robot is running
     */
    private fun createCore() {
        val builder = Core.Builder(applicationContext) //Initialize the Core Builder
        //Attach the SurfaceView textureView to render the camera to
        builder.holder = cameraSurfaceView
        builder.robotId = settings.robotId //Pass in our Robot ID

        (settings.cameraId).takeIf {
            settings.cameraEnabled
        }?.let{ cameraId ->
            val arrRes = settings.cameraResolution.split('x')
            val cameraSettings = CameraSettings(cameraId = cameraId,
                    pass = settings.cameraPassword,
                    width = arrRes[0].toInt(),
                    height = arrRes[1].toInt(),
                    bitrate = settings.cameraBitrate,
                    useLegacyApi = settings.cameraLegacy,
                    orientation = settings.cameraOrientation
            )
            builder.cameraSettings = cameraSettings
        }
        builder.useTTS = settings.enableTTS
        builder.useMic = settings.enableMic
        builder.protocol = settings.robotProtocol
        builder.communication = settings.robotCommunication
        builder.externalComponents = components //pass in arrayList of custom components
        try {
//            core = builder.build() //Retrieve the built Core instance //TODO pass settings to service
        } catch (e: Core.InitializationException) {
            RobotApplication.Instance.reportError(e) // Reports an initialization error to application
            e.printStackTrace()
        }
    }

    companion object {
        const val LOGTAG = "MainRobot"
    }
}