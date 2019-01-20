package tv.letsrobot.controller.android.activities

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tv.letsrobot.android.api.enums.CameraDirection
import tv.letsrobot.android.api.robot.CommunicationType
import tv.letsrobot.android.api.robot.ProtocolType
import tv.letsrobot.controller.android.activities.QRActivity.Companion.KEY_DATA
import tv.letsrobot.controller.android.robot.RobotSettingsObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by Brendon on 11/28/2018.
 */
@RunWith(AndroidJUnit4::class)
class QRActivityTest {
    @get:Rule
    val serviceRule : ActivityTestRule<QRActivity>
    = ActivityTestRule(QRActivity::class.java, false, false)

    @Before
    @Throws(Exception::class)
    fun setUp() {
        val intent = Intent()
        val data = RobotSettingsObject(
                "FOO",
                ProtocolType.SingleByte,
                CommunicationType.UsbSerial,
                "BAR",
                "LOOT",
                CameraDirection.DIR_0,
                1024,
                "640x480",
                false,
                true,
                true,
                true,
                true,
                1)
        intent.putExtra(KEY_DATA, data.toString())
        serviceRule.launchActivity(intent)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {

    }

    @Test
    fun testLaunch(){
        CountDownLatch(1).await(20, TimeUnit.SECONDS)
    }
}