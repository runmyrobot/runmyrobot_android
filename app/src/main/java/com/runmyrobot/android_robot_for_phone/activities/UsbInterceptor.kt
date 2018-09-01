package com.runmyrobot.android_robot_for_phone.activities

import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity

/**
 * Used to intercept a USB device being plugged in and save it.
 *
 * The use of this class is what helps prevent the request dialog from popping up every time the app
 * is run.
 */
class UsbInterceptor : AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        intent?.let {
            if (intent?.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                val usbDevice = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE)

                // Create a new intent and put the usb device in as an extra
                val broadcastIntent = Intent(ACTION_USB_DEVICE_ATTACHED)
                broadcastIntent.putExtra(UsbManager.EXTRA_DEVICE, usbDevice)

                // Broadcast this event so we can receive it
                sendBroadcast(broadcastIntent)
            }
        }

        // Close the activity
        finish()
    }

    companion object {
        const val ACTION_USB_DEVICE_ATTACHED = "com.example.ACTION_USB_DEVICE_ATTACHED"
    }
}
