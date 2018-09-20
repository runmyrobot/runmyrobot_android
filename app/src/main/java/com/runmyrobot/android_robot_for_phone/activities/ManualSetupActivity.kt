package com.runmyrobot.android_robot_for_phone.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import com.runmyrobot.android_robot_for_phone.R
import com.runmyrobot.android_robot_for_phone.api.CameraDirection
import com.runmyrobot.android_robot_for_phone.control.communicationInterfaces.CommunicationType
import com.runmyrobot.android_robot_for_phone.control.deviceProtocols.ProtocolType
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

        //Configure protocol spinner
        val protocols = ArrayList<String>()
        ProtocolType.values().forEach {
            protocols.add(it.name)
        }
        // Creating adapter for spinner
        val protocolAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, protocols)
        protocolChooser.adapter = protocolAdapter

        StoreUtil.getProtocolType(this)?.let {
            protocolChooser.setSelection(it.ordinal)
        }

        //Configure communication spinner
        val communications = ArrayList<String>()
        CommunicationType.values().forEach {
            communications.add(it.name) //TODO maybe check for device support here before showing it?
        }
        // Creating adapter for spinner
        val commAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, communications)
        communicationChooser.adapter = commAdapter
        StoreUtil.getCommunicationType(this)?.let {
            communicationChooser.setSelection(it.ordinal)
        }

        //Configure communication spinner
        val orientationChooserList = ArrayList<String>()
        CameraDirection.values().forEach {
            orientationChooserList.add(it.toString())
        }
        // Creating adapter for spinner
        val orientationChooserAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, orientationChooserList)
        orientationChooser.adapter = orientationChooserAdapter
        StoreUtil.getOrientation(this).also {
            orientationChooser.setSelection(it.ordinal)
        }

        applyButton.setOnClickListener {
            saveButtonStates()
            launchActivity()
        }
    }

    private fun launchActivity() {
        finish()
        startActivity(Intent(this, SplashActivity::class.java))
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
        StoreUtil.setCommunicationType(this, CommunicationType.valueOf(communicationChooser.selectedItem.toString()))
        StoreUtil.setProtocolType(this, ProtocolType.valueOf(protocolChooser.selectedItem.toString()))
        StoreUtil.setOrientation(this, CameraDirection.values()[orientationChooser.selectedItemPosition])
    }

    fun checkState(cameraChecked : Boolean){
        cameraPassEditText.isEnabled = cameraChecked
        cameraIDEditText.isEnabled = cameraChecked
        //bitrateEditText.isEnabled = cameraChecked //TODO implement these, then enable this
        //resolutionEditText.isEnabled = cameraChecked
    }
}
