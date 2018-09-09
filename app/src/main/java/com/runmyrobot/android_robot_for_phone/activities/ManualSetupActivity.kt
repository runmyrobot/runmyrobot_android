package com.runmyrobot.android_robot_for_phone.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.runmyrobot.android_robot_for_phone.R
import com.runmyrobot.android_robot_for_phone.utils.StoreUtil
import kotlinx.android.synthetic.main.activity_manual_setup.*

class ManualSetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_setup)
        StoreUtil.getRobotId(this)?.let { robotIDEditText.setText(it) }
        StoreUtil.getCameraId(this)?.let { cameraIDEditText.setText(it) }
        cameraPassEditText.setText(StoreUtil.getCameraPass(this))
        cameraEnableToggle.isChecked = StoreUtil.getCameraEnabled(this)
        micEnableButton.isChecked = StoreUtil.getMicEnabled(this)
        ttsEnableButton.isChecked = StoreUtil.getTTSEnabled(this)
        errorReportButton.isChecked = StoreUtil.getErrorReportingEnabled(this)
        cameraEnableToggle.setOnCheckedChangeListener { _, isChecked ->
            checkState(isChecked)
        }
        bitrateEditText.setText(StoreUtil.getBitrate(this))
        resolutionEditText.setText(StoreUtil.getResolution(this))
        bitrateEditText.isEnabled = false
        resolutionEditText.isEnabled = false
        checkState(cameraEnableToggle.isChecked)
        applyButton.setOnClickListener {
            saveButtonStates()
            launchActivity()
        }
        setupBluetoothButton.setOnClickListener{
            startActivity(Intent(this, ChooseBluetoothActivity::class.java))
        }
    }

    private fun launchActivity() {
        startActivity(Intent(this, MainRobotActivity::class.java))
        StoreUtil.setConfigured(this, true)
    }

    private fun saveButtonStates() {
        robotIDEditText.text.takeIf { !it.isBlank() }?.let {
            StoreUtil.setRobotId(this, it.toString())
        }
        cameraIDEditText.text.takeIf { !it.isBlank() }?.let {
            StoreUtil.setCameraId(this, it.toString())
        }
        cameraPassEditText.text.takeIf { !it.isBlank() }?.let {
            StoreUtil.setCameraPass(this, it.toString())
        }
        bitrateEditText.text.takeIf { !it.isBlank() }?.let {
            //TODO Add pref for this
        }
        resolutionEditText.text.takeIf { !it.isBlank() }?.let {
            //TODO Add pref for this
        }
        StoreUtil.setCameraEnabled(this, cameraEnableToggle.isChecked)
        StoreUtil.setMicEnabled(this, micEnableButton.isChecked)
        StoreUtil.setTTSEnabled(this, ttsEnableButton.isChecked)
        StoreUtil.setErrorReportingEnabled(this, errorReportButton.isChecked)
    }

    fun checkState(cameraChecked : Boolean){
        cameraPassEditText.isEnabled = cameraChecked
        cameraIDEditText.isEnabled = cameraChecked
        //bitrateEditText.isEnabled = cameraChecked //TODO implement these, then enable this
        //resolutionEditText.isEnabled = cameraChecked
    }
}
