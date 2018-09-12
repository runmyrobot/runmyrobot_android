package com.runmyrobot.android_robot_for_phone.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import com.runmyrobot.android_robot_for_phone.R
import com.runmyrobot.android_robot_for_phone.control.CommunicationInterface
import com.runmyrobot.android_robot_for_phone.utils.StoreUtil

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        // Setup App before initializing anything, then go back to do permissions flow
        // and to do device setup
        if(!StoreUtil.getConfigured(this)){
            finish()
            startActivity(Intent(this, ManualSetupActivity::class.java))
            return
        }

        //Load FFMpeg
        val ffmpeg = FFmpeg.getInstance(applicationContext)
        try {
            ffmpeg.loadBinary(object : LoadBinaryResponseHandler() {
                override fun onFinish() {
                    super.onFinish()
                    Log.d("FFMPEG", "onFinish")
                    runOnUiThread{
                        next() //run next action
                    }
                }

                override fun onSuccess() {
                    super.onSuccess()
                    Log.d("FFMPEG", "onSuccess")
                }

                override fun onFailure() {
                    super.onFailure()
                    Log.d("FFMPEG", "onFailure")
                }

                override fun onStart() {
                    super.onStart()
                    Log.d("FFMPEG", "onStart")
                }
                //TODO maybe catch some error to display to the user
            })
        } catch (e: FFmpegNotSupportedException) {
            e.printStackTrace()
        }
    }

    //TODO replace with co-routine once that is stable and stops breaking Android Studio
    private fun next() {
        //Check permissions. break out if that returns false
        if(!checkPermissions()){
            return
        }
        //Setup device. break out if not setup, or if error occurred
        setupDevice()?.let {
            if(!it){
                //setup not complete
                return
            }
        } ?: run{
            //Something really bad happened here. Not sure how we continue
            setupError()
            return
        }
        //All checks are done. Lets startup the activity!
        finish()
        startActivity(Intent(this, MainRobotActivity::class.java))
    }

    /**
     * Show some setup error message. Allow the user to attempt setup again
     */
    private fun setupError() {
        Toast.makeText(this
                , "Something happened while trying to setup. Please try again"
                , Toast.LENGTH_LONG).show()
        StoreUtil.setConfigured(this, false)
        finish()
        startActivity(Intent(this, ManualSetupActivity::class.java))
    }

    private var pendingDeviceSetup: CommunicationInterface? = null

    private var pendingResultCode: Int = -1

    private fun setupDevice(): Boolean? {
        val commType = StoreUtil.getCommunicationType(this) // :CommunicationType?
        commType?.let {
            val clazz = it.getInstantiatedClass
            clazz?.let {
                return if(it.needsSetup(this)){
                    pendingResultCode = it.setupComponent(this)
                    pendingDeviceSetup = it
                    false
                } else{
                    true
                }
            }
        }
        return null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(checkPermissions()){
            next()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Check if result was due to a pending interface setup
        pendingDeviceSetup?.takeIf { pendingResultCode == requestCode}?.let {
            //relay info to interface
            it.receivedComponentSetupDetails(this, data)
            pendingDeviceSetup = null
            pendingResultCode = -1
            next()
        }
    }

    private val requestCode = 1002

    private fun checkPermissions() : Boolean{
        val list = ArrayList<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            list.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            list.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            list.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            list.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            list.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        return if(!list.isEmpty()){
            ActivityCompat.requestPermissions(this,
                    list.toArray(Array<String>(0) {""}),
                    requestCode)
            false
        }
        else{
            true
        }
    }
}
