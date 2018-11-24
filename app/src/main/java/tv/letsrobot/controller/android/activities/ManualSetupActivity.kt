package tv.letsrobot.controller.android.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.letsrobot.controller.android.R
import kotlinx.android.synthetic.main.activity_manual_setup.*
import tv.letsrobot.android.api.enums.CameraDirection
import tv.letsrobot.android.api.enums.CommunicationType
import tv.letsrobot.android.api.enums.ProtocolType
import tv.letsrobot.android.api.utils.RobotConfig

class ManualSetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_setup)
        RobotConfig.RobotId.getValue(this)?.let { robotIDEditText.setText(it as String) }
        RobotConfig.CameraId.getValue(this)?.let { cameraIDEditText.setText(it as String) }
        cameraPassEditText.setText(RobotConfig.CameraPass.getValue(this, "hello") as String)
        cameraEnableToggle.isChecked = RobotConfig.CameraEnabled.getValue(this) as Boolean
        micEnableButton.isChecked = RobotConfig.MicEnabled.getValue(this) as Boolean
        ttsEnableButton.isChecked = RobotConfig.TTSEnabled.getValue(this) as Boolean
        errorReportButton.isChecked = RobotConfig.ErrorReporting.getValue(this) as Boolean
        cameraEnableToggle.setOnCheckedChangeListener { _, isChecked ->
            checkState(isChecked)
        }
        screenOverlaySettingsButton.isChecked = RobotConfig.SleepMode.getValue(this) as Boolean
        bitrateEditText.setText(RobotConfig.VideoBitrate.getValue(this, "512") as String)
        resolutionEditText.setText(RobotConfig.VideoBitrate.getValue(this, "640x480") as String)
        legacyCameraEnableToggle.isEnabled = Build.VERSION.SDK_INT >= 21
        legacyCameraEnableToggle.isChecked =
                RobotConfig.UseLegacyCamera.getValue(this, Build.VERSION.SDK_INT < 21) as Boolean
        bitrateEditText.isEnabled = true
        resolutionEditText.isEnabled = false
        checkState(cameraEnableToggle.isChecked)

        setupSpinnerWithSetting(protocolChooser, RobotConfig.Protocol, ProtocolType::class.java)
        setupSpinnerWithSetting(communicationChooser, RobotConfig.Communication, CommunicationType::class.java)
        setupSpinnerWithSetting(orientationChooser, RobotConfig.Orientation, CameraDirection::class.java)

        applyButton.setOnClickListener {
            saveButtonStates()
            launchActivity()
        }
    }

    private fun launchActivity() {
        finish()
        startActivity(Intent(this, SplashActivity::class.java))
        RobotConfig.Configured.saveValue(this, true)
    }

    private fun saveButtonStates() {
        robotIDEditText.text.takeIf { !it.isBlank() }?.let {
            RobotConfig.RobotId.saveValue(this, it.toString())
        }
        cameraIDEditText.text.takeIf { !it.isBlank() }?.let {
            RobotConfig.CameraId.saveValue(this, it.toString())
        }
        cameraPassEditText.text.takeIf { !it.isBlank() }?.let {
            RobotConfig.CameraPass.saveValue(this, it.toString())
        }
        bitrateEditText.text.takeIf { !it.isBlank() }?.let {
            RobotConfig.VideoBitrate.saveValue(this, it.toString())
        }
        resolutionEditText.text.takeIf { !it.isBlank() }?.let {
            //TODO Add pref for this
        }

        if(legacyCameraEnableToggle.isEnabled){
            RobotConfig.UseLegacyCamera.saveValue(this, legacyCameraEnableToggle.isChecked)
        }
        RobotConfig.CameraEnabled.saveValue(this, cameraEnableToggle.isChecked)
        RobotConfig.MicEnabled.saveValue(this, micEnableButton.isChecked)
        RobotConfig.TTSEnabled.saveValue(this, ttsEnableButton.isChecked)
        RobotConfig.ErrorReporting.saveValue(this, errorReportButton.isChecked)
        RobotConfig.Communication.saveValue(this, CommunicationType.valueOf(communicationChooser.selectedItem.toString()))
        RobotConfig.Protocol.saveValue(this, ProtocolType.valueOf(protocolChooser.selectedItem.toString()))
        RobotConfig.Orientation.saveValue(this, CameraDirection.values()[orientationChooser.selectedItemPosition])
        RobotConfig.SleepMode.saveValue(this, screenOverlaySettingsButton.isChecked)
    }

    /**
     * Sets up a spinner using enum values from RobotConfig
     */
    private fun <T : Enum<T>> setupSpinnerWithSetting(spinner : Spinner, value : RobotConfig, enumClass : Class<T>){
        spinner.adapter = createEnumArrayAdapter(enumClass.enumConstants)
        val enum = RobotConfig.fetchEnum(this, value, enumClass.enumConstants[0])
        spinner.setSelection(enum.ordinal)
    }

    private fun <T : Enum<T>> createEnumArrayAdapter(list : Array<T>) : ArrayAdapter<Any>{
        val arrList = ArrayList<String>()
        list.forEach {
            arrList.add(it.name)
        }
        // Creating adapter for spinner
        return ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
    }

    fun checkState(cameraChecked : Boolean){
        cameraPassEditText.isEnabled = cameraChecked
        cameraIDEditText.isEnabled = cameraChecked
        bitrateEditText.isEnabled = cameraChecked
        //resolutionEditText.isEnabled = cameraChecked
    }
}
