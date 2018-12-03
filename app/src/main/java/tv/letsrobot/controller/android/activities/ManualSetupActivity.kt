package tv.letsrobot.controller.android.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
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
        val settings = RobotSettingsObject.load(this)
        robotIDEditText.setText(settings.robotId)
        cameraIDEditText.setText(settings.cameraId)
        cameraPassEditText.setText(settings.cameraPassword)
        cameraEnableToggle.isChecked = settings.cameraEnabled
        micEnableButton.isChecked = settings.enableMic
        ttsEnableButton.isChecked = settings.enableTTS
        errorReportButton.isEnabled = false //Not using right now.
        errorReportButton.isChecked = false //Not using right now.
        cameraEnableToggle.setOnCheckedChangeListener { _, isChecked ->
            checkState(isChecked)
        }
        screenOverlaySettingsButton.isChecked = settings.screenTimeout
        bitrateEditText.setText(settings.cameraBitrate.toString())
        resolutionEditText.setText(settings.cameraResolution)
        val legacyOnly = Build.VERSION.SDK_INT < 21 //phones under 21 cannot use the new camera api
        legacyCameraEnableToggle.isEnabled = !legacyOnly
        legacyCameraEnableToggle.isChecked = settings.cameraLegacy
        bitrateEditText.isEnabled = true
        resolutionEditText.isEnabled = false
        checkState(cameraEnableToggle.isChecked)

        setupSpinnerWithSetting(protocolChooser, settings.robotProtocol)
        setupSpinnerWithSetting(communicationChooser, settings.robotCommunication)
        setupSpinnerWithSetting(orientationChooser, settings.cameraOrientation)

        applyButton.setOnClickListener {
            saveSettings()
            launchActivity()
        }

        exportQRButton.setOnClickListener {
            exportSettings()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu).also {
            val inflater: MenuInflater = menuInflater
            inflater.inflate(R.menu.setup_menu, menu)
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
                screenOverlaySettingsButton.isChecked)
    }

    /**
     * Sets up a spinner using enum values from RobotConfig
     */
    private fun <T : Enum<T>> setupSpinnerWithSetting(spinner : Spinner, value : T){
        spinner.adapter = createEnumArrayAdapter(value.declaringClass.enumConstants)
        spinner.setSelection(value.ordinal)
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
