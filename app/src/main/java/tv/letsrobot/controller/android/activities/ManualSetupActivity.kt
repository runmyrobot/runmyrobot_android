package tv.letsrobot.controller.android.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
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
import java.io.FileNotFoundException
import java.util.*


class ManualSetupActivity : AppCompatActivity() {

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
            checkState(isChecked)
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
                R.id.cameraMenuItem -> getImageFromCamera()
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
                           /*return*/BitmapFactory.decodeStream(contentResolver.openInputStream(targetUri))
                       } catch (e: FileNotFoundException) {
                           // TODO Auto-generated catch block
                           e.printStackTrace()
                           /*return*/null
                       }
                   }
                   bitmap?.let {
                       GlobalScope.launch {
                           val resultCoroutine = getQRResultFromBitmap(bitmap)
                           val result = resultCoroutine.await()
                           runOnUiThread {
                               refreshSettings(result?.text)
                           }
                       }
                   }
               }
               CAMERA_REQUEST_CODE -> {
                   val photo = (data?.extras?.get("data") as? Bitmap)!!
                   print(photo.byteCount)
                   GlobalScope.launch {
                       val resultCoroutine = getQRResultFromBitmap(photo)
                       val result = resultCoroutine.await()
                       runOnUiThread {
                           refreshSettings(result?.text)
                       }
                   }

               }
           }
        }
    }

    private fun refreshSettings(settingsTxt : String? = null){
        val settings : RobotSettingsObject = settingsTxt?.let{
            RobotSettingsObject.fromString(settingsTxt) //attempt to load string
        } ?: RobotSettingsObject.load(this) //load saved settings
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

    private fun getImageFromCamera() {
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
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
                resolutionEditText.string(),
                cameraEnableToggle.isChecked,
                legacyCameraEnableToggle.isChecked,
                micEnableButton.isChecked,
                ttsEnableButton.isChecked,
                screenOverlaySettingsButton.isChecked)
    }

    fun checkState(cameraChecked : Boolean){
        cameraPassEditText.isEnabled = cameraChecked
        cameraIDEditText.isEnabled = cameraChecked
        bitrateEditText.isEnabled = cameraChecked
        //resolutionEditText.isEnabled = cameraChecked
    }

    //some utility functions

    fun EditText.string() : String{
        return text.toString()
    }

    fun EditText.toIntOrZero() : Int{
        return string().toIntOrNull()?.let { it } ?: 0
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

    companion object {
        private const val CAMERA_REQUEST_CODE = 1
        private const val PHOTOS_REQUEST_CODE = 2
    }
}
