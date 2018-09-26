package com.runmyrobot.android_robot_for_phone.api;

import com.runmyrobot.android_robot_for_phone.BuildConfig;
import com.runmyrobot.android_robot_for_phone.myrobot.RobotComponentList;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

/**
 * Helper test to test core functionality without an activity. Cannot test camera using this
 */
@RunWith(AndroidJUnit4.class)
public class CoreAndroidTest {
    Core core = null;
    @Test
    public void Init() throws Core.InitializationException {
        RobotComponentList.INSTANCE.init(InstrumentationRegistry.getTargetContext());
        Core.Builder builder = new Core.Builder(InstrumentationRegistry.getTargetContext());
        builder.setRobotId(BuildConfig.ROBOT_ID);
        builder.setCameraId(BuildConfig.CAMERA_ID);
        builder.setUseTTS(true);
        core = builder.build();
        Assert.assertTrue(core.enable());
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(1000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(core.disable());
    }
}
