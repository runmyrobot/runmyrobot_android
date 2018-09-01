package com.runmyrobot.android_robot_for_phone.api;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.runmyrobot.android_robot_for_phone.myrobot.RobotComponentList;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Brendon on 8/26/2018.
 */
@RunWith(AndroidJUnit4.class)
public class CoreAndroidTest {
    Core core = null;
    @Test
    public void Init() throws Core.InitializationException {
        RobotComponentList.INSTANCE.init(InstrumentationRegistry.getTargetContext());
        Core.Builder builder = new Core.Builder(InstrumentationRegistry.getTargetContext());
        builder.setRobotId("58853258");
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
