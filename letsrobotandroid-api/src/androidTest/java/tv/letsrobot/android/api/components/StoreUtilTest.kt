package tv.letsrobot.android.api.components

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import tv.letsrobot.android.api.enums.CameraDirection
import tv.letsrobot.android.api.robot.CommunicationType
import tv.letsrobot.android.api.robot.ProtocolType
import tv.letsrobot.android.api.utils.RobotConfig

/**
 * Verify that all of the settings get saved correctly
 */
@RunWith(AndroidJUnit4::class)
class StoreUtilTest {
    @Test
    fun testSettings(){
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        //Test setting a boolean
        RobotConfig.CameraEnabled.saveValue(context, true)
        RobotConfig.SleepMode.saveValue(context, true)
        RobotConfig.MicEnabled.saveValue(context, true)
        RobotConfig.TTSEnabled.saveValue(context, true)
        RobotConfig.ErrorReporting.saveValue(context, true)
        RobotConfig.ErrorReporting.saveValue(context, true)
        RobotConfig.UseLegacyCamera.saveValue(context, true)
        //Test setting strings
        RobotConfig.RobotId.saveValue(context, "Robot")
        RobotConfig.CameraId.saveValue(context, "Camera")
        RobotConfig.CameraPass.saveValue(context, "Test")
        RobotConfig.VideoBitrate.saveValue(context, "Bitrate")
        RobotConfig.VideoResolution.saveValue(context, "Res")
        //Test setting custom enums
        RobotConfig.Communication.saveValue(context, CommunicationType.BluetoothClassic)
        RobotConfig.Protocol.saveValue(context, ProtocolType.ArduinoRaw)
        RobotConfig.Orientation.saveValue(context, CameraDirection.DIR_90)
        //Now test setting the wrong value
        try {
            RobotConfig.Orientation.saveValue(context, "MismatchValue")
            Assert.fail() //if we made it this far, then something bad happened
        }
        catch (e : Exception){
            //Good, we were supposed to catch an exception
        }

        ////
        //// Now Test retrieval
        ////

        //Retrieve those boolean values
        Assert.assertTrue(RobotConfig.CameraEnabled.getValue(context, false) as Boolean)
        Assert.assertTrue(RobotConfig.SleepMode.getValue(context, false) as Boolean)
        Assert.assertTrue(RobotConfig.MicEnabled.getValue(context, false) as Boolean)
        Assert.assertTrue(RobotConfig.TTSEnabled.getValue(context, false) as Boolean)
        Assert.assertTrue(RobotConfig.ErrorReporting.getValue(context, false) as Boolean)
        Assert.assertTrue(RobotConfig.ErrorReporting.getValue(context, false) as Boolean)
        Assert.assertTrue(RobotConfig.UseLegacyCamera.getValue(context, false) as Boolean)

        Assert.assertEquals("Robot",RobotConfig.RobotId.getValue(context, ""))
        Assert.assertEquals("Camera",RobotConfig.CameraId.getValue(context, ""))
        Assert.assertEquals("Test",RobotConfig.CameraPass.getValue(context, ""))
        Assert.assertEquals("Bitrate",RobotConfig.VideoBitrate.getValue(context, ""))
        Assert.assertEquals("Res",RobotConfig.VideoResolution.getValue(context, ""))

        Assert.assertEquals(CommunicationType.BluetoothClassic,RobotConfig.Communication.getValue(context, CommunicationType.UsbSerial))
        Assert.assertEquals(ProtocolType.ArduinoRaw,RobotConfig.Protocol.getValue(context, ProtocolType.SingleByte))
        Assert.assertEquals(CameraDirection.DIR_90,RobotConfig.Orientation.getValue(context, CameraDirection.DIR_0))
    }
}
