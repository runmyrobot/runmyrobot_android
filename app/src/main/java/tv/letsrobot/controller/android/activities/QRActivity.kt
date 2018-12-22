package tv.letsrobot.controller.android.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.android.synthetic.main.activity_qr.*
import kotlinx.android.synthetic.main.content_qr.*
import kotlinx.coroutines.*
import tv.letsrobot.controller.android.R
import tv.letsrobot.controller.android.robot.RobotSettingsObject

class QRActivity : AppCompatActivity(){

    private val qrCodeJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + qrCodeJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)
        setSupportActionBar(toolbar)

        intent.extras?.getString(KEY_DATA)?.let {
            uiScope.launch {
                val bitmap = getQRCode(it)
                qrImageView.setImageBitmap(bitmap.await())
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Get QR Code asynchronously. Returns a bitmap
     */
    private fun getQRCode(data : String) = GlobalScope.async {
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        const val KEY_DATA = "robotData"

        fun getLaunchIntent(context: Context, settingsObject: RobotSettingsObject) : Intent{
            val intent = Intent(context, QRActivity::class.java)
            intent.putExtra(KEY_DATA, settingsObject.toString())
            return intent
        }
    }
}
