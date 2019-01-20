package tv.letsrobot.controller.android.tests

import org.junit.Assert
import org.junit.Test
import tv.letsrobot.android.api.enums.CameraDirection
import tv.letsrobot.android.api.robot.CommunicationType
import tv.letsrobot.android.api.robot.ProtocolType
import tv.letsrobot.controller.android.robot.RobotSettingsObject

/**
 * Created by Brendon on 11/28/2018.
 */
class RobotSettingsObjectTest{
    @Test
    fun testSerialize(){
        val data = RobotSettingsObject(
                "FOO",
                ProtocolType.SingleByte,
                CommunicationType.UsbSerial,
                "BAR",
                "LOOT",
                CameraDirection.DIR_0,
                1024,
                "640x480",
                true,
                false,
                true,
                true,
                true,
                1)
        val dataStr = data.toString()
        Assert.assertEquals("1;FOO;1;0;BAR;LOOT;0;1024;640x480;1;0;1;1;1", dataStr)
    }

    @Test
    fun testDeserialize(){
        val data = RobotSettingsObject.fromString("1;FOO;1;0;BAR;LOOT;0;1024;640x480;1;0;1;1;1")
        data?.let {
            Assert.assertEquals(1, it.version)
            Assert.assertEquals("FOO", it.robotId)
            Assert.assertEquals(ProtocolType.SingleByte, it.robotProtocol)
            Assert.assertEquals(CommunicationType.UsbSerial, it.robotCommunication)
            Assert.assertEquals("BAR", it.cameraId)
            Assert.assertEquals("LOOT", it.cameraPassword)
            Assert.assertEquals(CameraDirection.DIR_0, it.cameraOrientation)
            Assert.assertEquals(1024, it.cameraBitrate)
            Assert.assertEquals("640x480", it.cameraResolution)
            Assert.assertEquals(false, it.cameraLegacy)
            Assert.assertEquals(true, it.enableMic)
            Assert.assertEquals(true, it.enableTTS)
            Assert.assertEquals(true, it.screenTimeout)
        } ?: Assert.fail()
    }
}