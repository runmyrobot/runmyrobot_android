package tv.letsrobot.controller.android.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_manual_setup.*
import tv.letsrobot.android.api.Core
import tv.letsrobot.android.api.enums.CameraDirection
import tv.letsrobot.android.api.enums.CommunicationType
import tv.letsrobot.android.api.enums.ProtocolType
import tv.letsrobot.android.api.utils.RobotConfig
import tv.letsrobot.controller.android.R
import tv.letsrobot.controller.android.robot.RobotSettingsObject

class ManualSetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_setup)
        robotIDEditText.setText(RobotConfig.RobotId.getValue(this, "") as String)
        cameraIDEditText.setText(RobotConfig.CameraId.getValue(this, "") as String)
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
        resolutionEditText.setText(RobotConfig.VideoResolution.getValue(this, "640x480") as String)
        val legacyOnly = Build.VERSION.SDK_INT < 21 //phones under 21 cannot use the new camera api
        legacyCameraEnableToggle.isEnabled = !legacyOnly
        legacyCameraEnableToggle.isChecked = RobotConfig.UseLegacyCamera.getValue(this, legacyOnly) as Boolean
        bitrateEditText.isEnabled = true
        resolutionEditText.isEnabled = false
        checkState(cameraEnableToggle.isChecked)

        setupSpinnerWithSetting(protocolChooser, RobotConfig.Protocol, ProtocolType::class.java)
        setupSpinnerWithSetting(communicationChooser, RobotConfig.Communication, CommunicationType::class.java)
        setupSpinnerWithSetting(orientationChooser, RobotConfig.Orientation, CameraDirection::class.java)

        applyButton.setOnClickListener {
            saveSettings()
            launchActivity()
        }

        exportQRButton.setOnClickListener {
            exportSettings()
        }
    }

    private fun exportSettings() {
        val settings = getSettingsFromUI()
        startActivity(QRActivity.getLaunchIntent(this, settings))
    }

    private fun launchActivity() {
        if(resetRobotComponentsCheckbox.isChecked){
            Core.resetCommunicationConfig(this)
        }
        finish()
        startActivity(Intent(this, SplashActivity::class.java))
        RobotConfig.Configured.saveValue(this, true)
    }

    private fun saveSettings() {
        val settings = getSettingsFromUI()
        RobotSettingsObject.save(this, settings)
    }

    fun EditText.string() : String{
        return text.toString()
    }

    fun EditText.toIntOrZero() : Int{
        return string().toIntOrNull()?.let { it } ?: 0
    }

    private fun getSettingsFromUI(): RobotSettingsObject {
        return RobotSettingsObject(
                robotIDEditText.string(),
                ProtocolType.valueOf(protocolChooser.selectedItem.toString()),
                CommunicationType.valueOf(communicationChooser.selectedItem.toString()),
                cameraIDEditText.string(),
                cameraPassEditText.string(),
                CameraDirection.values()[orientationChooser.selectedItemPosition],
                bitrateEditText.toIntOrZero(),
                resolutionEditText.string(),
                cameraEnableToggle.isChecked,
                legacyCameraEnableToggle.isChecked,
                micEnableButton.isChecked,
                ttsEnableButton.isChecked,
                errorReportButton.isChecked)
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
