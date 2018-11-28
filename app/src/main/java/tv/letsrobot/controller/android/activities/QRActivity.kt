package tv.letsrobot.controller.android.activities

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.android.synthetic.main.activity_qr.*
import kotlinx.android.synthetic.main.content_qr.*
import tv.letsrobot.controller.android.R
import java.util.*


class QRActivity : AppCompatActivity(), Runnable{
    var handler : Handler? = null
    var handlerThread : HandlerThread? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)
        setSupportActionBar(toolbar)
        handlerThread = HandlerThread("QRCode").also {
            it.start()
            handler = Handler(it.looper)
        }
        setupFromQRFab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        handler?.post(this)
    }

    /**
     * Runnable on worker thread for QR code dynamic generation
     */
    override fun run() {
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(UUID.randomUUID().toString(), BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            runOnUiThread{
                qrImageView.setImageBitmap(bmp)
                handler?.post(this)
            }
        } catch (e: WriterException) {
            e.printStackTrace()
        }

    }
}
