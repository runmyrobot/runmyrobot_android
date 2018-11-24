package tv.letsrobot.android.api.components

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import tv.letsrobot.android.api.enums.CameraDirection
import tv.letsrobot.android.api.enums.CommunicationType
import tv.letsrobot.android.api.enums.ProtocolType
import tv.letsrobot.android.api.utils.Settings

/**
 * Verify that all of the settings get saved correctly
 */
@RunWith(AndroidJUnit4::class)
class StoreUtilTest {
    @Test
    fun testSettings(){
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        //Test setting a boolean
        Settings.CameraEnabled.saveValue(context, true)
        Settings.SleepMode.saveValue(context, true)
        Settings.MicEnabled.saveValue(context, true)
        Settings.TTSEnabled.saveValue(context, true)
        Settings.ErrorReporting.saveValue(context, true)
        Settings.ErrorReporting.saveValue(context, true)
        Settings.UseLegacyCamera.saveValue(context, true)
        //Test setting strings
        Settings.RobotId.saveValue(context, "Robot")
        Settings.CameraId.saveValue(context, "Camera")
        Settings.CameraPass.saveValue(context, "Test")
        Settings.VideoBitrate.saveValue(context, "Bitrate")
        Settings.VideoResolution.saveValue(context, "Res")
        //Test setting custom enums
        Settings.Communication.saveValue(context, CommunicationType.BluetoothClassic)
        Settings.Protocol.saveValue(context, ProtocolType.ArduinoRaw)
        Settings.Orientation.saveValue(context, CameraDirection.DIR_90)
        //Now test setting the wrong value
        try {
            Settings.Orientation.saveValue(context, "MismatchValue")
            Assert.fail() //if we made it this far, then something bad happened
        }
        catch (e : Exception){
            //Good, we were supposed to catch an exception
        }

        ////
        //// Now Test retrieval
        ////

        //Retrieve those boolean values
        Assert.assertTrue(Settings.CameraEnabled.getValue(context, false) as Boolean)
        Assert.assertTrue(Settings.SleepMode.getValue(context, false) as Boolean)
        Assert.assertTrue(Settings.MicEnabled.getValue(context, false) as Boolean)
        Assert.assertTrue(Settings.TTSEnabled.getValue(context, false) as Boolean)
        Assert.assertTrue(Settings.ErrorReporting.getValue(context, false) as Boolean)
        Assert.assertTrue(Settings.ErrorReporting.getValue(context, false) as Boolean)
        Assert.assertTrue(Settings.UseLegacyCamera.getValue(context, false) as Boolean)

        Assert.assertEquals("Robot",Settings.RobotId.getValue(context, ""))
        Assert.assertEquals("Camera",Settings.CameraId.getValue(context, ""))
        Assert.assertEquals("Test",Settings.CameraPass.getValue(context, ""))
        Assert.assertEquals("Bitrate",Settings.VideoBitrate.getValue(context, ""))
        Assert.assertEquals("Res",Settings.VideoResolution.getValue(context, ""))

        Assert.assertEquals(CommunicationType.BluetoothClassic,Settings.Communication.getValue(context, CommunicationType.UsbSerial))
        Assert.assertEquals(ProtocolType.ArduinoRaw,Settings.Protocol.getValue(context, ProtocolType.SingleByte))
        Assert.assertEquals(CameraDirection.DIR_90,Settings.Orientation.getValue(context, CameraDirection.DIR_0))
    }
}
