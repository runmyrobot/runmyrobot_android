package tv.letsrobot.controller.android.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.android.synthetic.main.activity_manual_setup.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import tv.letsrobot.android.api.Core
import tv.letsrobot.android.api.enums.CameraDirection
import tv.letsrobot.android.api.enums.CommunicationType
import tv.letsrobot.android.api.enums.ProtocolType
import tv.letsrobot.android.api.utils.RobotConfig
import tv.letsrobot.controller.android.R
import tv.letsrobot.controller.android.robot.RobotSettingsObject
import tv.letsrobot.controller.android.utils.setPositionGivenText
import tv.letsrobot.controller.android.utils.setupSpinnerWithSetting
import tv.letsrobot.controller.android.utils.string
import tv.letsrobot.controller.android.utils.toIntOrZero
import java.io.FileNotFoundException


class ManualSetupActivity : AppCompatActivity() {

    private val legacyOnly = Build.VERSION.SDK_INT < 21 //phones under 21 cannot use the new camera api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_setup)
        applyButton.setOnClickListener {
            saveSettings()
            launchActivity()
        }

        exportQRButton.setOnClickListener {
            exportSettings()
        }

        cameraEnableToggle.setOnCheckedChangeListener { _, isChecked ->
            checkCameraState(isChecked)
        }

        legacyCameraEnableToggle.setOnCheckedChangeListener { _, _ ->
            checkCameraState(cameraEnableToggle.isChecked)
        }
        refreshSettings()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu).also {
            val inflater: MenuInflater = menuInflater
            inflater.inflate(R.menu.setup_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var consumed = true
        item?.let {
            when(it.itemId){
                R.id.cameraMenuItem -> getImageFromCamera(true)
                R.id.photosMenuItem -> getImageFromPhotos()
                else -> consumed = false
            }
            consumed = false
        } ?: run { consumed = false }
        return consumed
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
           when(requestCode){
               PHOTOS_REQUEST_CODE -> {
                   val targetUri = data?.data
                   val bitmap : Bitmap? = targetUri?.let {
                       try {
                           /*^let*/BitmapFactory.decodeStream(contentResolver.openInputStream(targetUri))
                       } catch (e: FileNotFoundException) {
                           e.printStackTrace()
                           /*^let*/null
                       }
                   }
                   bitmap?.let {
                       parseQRCodeAndUpdate(bitmap)
                   }
               }
               CAMERA_REQUEST_CODE -> {
                   val photo = (data?.extras?.get("data") as? Bitmap)!!
                   parseQRCodeAndUpdate(photo)
               }
           }
        }
    }

    private fun parseQRCodeAndUpdate(bitmap: Bitmap){
        GlobalScope.launch {
            val resultCoroutine = getQRResultFromBitmap(bitmap)
            val result = resultCoroutine.await()
            result?.let {
                RobotSettingsObject.fromString(it.text)?.let {settings ->
                    runOnUiThread {
                        refreshSettings(settings)
                    }
                }
            } ?: throwQRError()
        }
    }

    private fun throwQRError(){
        Snackbar.make(applyButton,
                "Error occurred while reading QR Code. Try Again",
                Snackbar.LENGTH_LONG).show()
    }

    private fun refreshSettings(settingsTxt : RobotSettingsObject? = null){
        val settings : RobotSettingsObject = settingsTxt?.let{
            it
        } ?: RobotSettingsObject.load(this) //load saved settings if settingsTxt is null
        robotIDEditText.setText(settings.robotId)
        cameraIDEditText.setText(settings.cameraId)
        cameraPassEditText.setText(settings.cameraPassword)
        cameraEnableToggle.isChecked = settings.cameraEnabled
        micEnableButton.isChecked = settings.enableMic
        ttsEnableButton.isChecked = settings.enableTTS
        errorReportButton.isEnabled = false //Not using right now.
        errorReportButton.isChecked = false //Not using right now.
        screenOverlaySettingsButton.isChecked = settings.screenTimeout
        bitrateEditText.setText(settings.cameraBitrate.toString())
        legacyCameraEnableToggle.isEnabled = !legacyOnly
        legacyCameraEnableToggle.isChecked = settings.cameraLegacy
        bitrateEditText.isEnabled = true
        checkCameraState(cameraEnableToggle.isChecked)
        updateSpinners(settings)
    }

    private fun updateSpinners(settings: RobotSettingsObject) {
        resolutionSpinner.setPositionGivenText(settings.cameraResolution)
        protocolChooser.setupSpinnerWithSetting(settings.robotProtocol)
        communicationChooser.setupSpinnerWithSetting(settings.robotCommunication)
        orientationChooser.setupSpinnerWithSetting(settings.cameraOrientation)
    }

    private fun getQRResultFromBitmap(bitmap : Bitmap) = GlobalScope.async{
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        bitmap.recycle()
        val source = RGBLuminanceSource(width, height, pixels)
        val bBitmap = BinaryBitmap(HybridBinarizer(source))
        val reader = MultiFormatReader()
        try {
            reader.decode(bBitmap)
        } catch (e: NotFoundException) {
            Log.e("QRCode", "decode exception", e)
            null
        }
    }


    private fun getImageFromPhotos() {
        val intent = Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PHOTOS_REQUEST_CODE)
    }

    private var queueCameraLaunch = false

    private fun getImageFromCamera(shouldShowPermission: Boolean) {
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            queueCameraLaunch = if(shouldShowPermission) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_REQUEST_CODE)
                true
            } else{
                false
            }
        }
        else{
            //permission successful
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == CAMERA_PERMISSION_REQUEST_CODE && queueCameraLaunch){
            //run getImageFromCamera again. Will not ask again if permission denied
            getImageFromCamera(false)
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

    private fun getSettingsFromUI(): RobotSettingsObject {
        return RobotSettingsObject(
                robotIDEditText.string(),
                ProtocolType.valueOf(protocolChooser.selectedItem.toString()),
                CommunicationType.valueOf(communicationChooser.selectedItem.toString()),
                cameraIDEditText.string(),
                cameraPassEditText.string(),
                CameraDirection.values()[orientationChooser.selectedItemPosition],
                bitrateEditText.toIntOrZero(),
                resolutionSpinner.selectedItem.toString(),
                cameraEnableToggle.isChecked,
                legacyCameraEnableToggle.isChecked,
                micEnableButton.isChecked,
                ttsEnableButton.isChecked,
                screenOverlaySettingsButton.isChecked)
    }

    private fun checkCameraState(cameraChecked : Boolean){
        cameraPassEditText.isEnabled = cameraChecked
        cameraIDEditText.isEnabled = cameraChecked
        bitrateEditText.isEnabled = cameraChecked
        val legacyToggled = legacyCameraEnableToggle.isChecked
        resolutionSpinner.isEnabled = !legacyToggled
        if(legacyToggled){
            resolutionSpinner.setSelection(0)
        }
    }



    companion object {
        private const val CAMERA_REQUEST_CODE = 1
        private const val PHOTOS_REQUEST_CODE = 2
        private const val CAMERA_PERMISSION_REQUEST_CODE = 23
    }
}
