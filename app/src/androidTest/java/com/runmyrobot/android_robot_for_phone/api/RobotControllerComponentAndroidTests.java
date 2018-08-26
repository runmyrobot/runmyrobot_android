package com.runmyrobot.android_robot_for_phone.api;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Brendon on 8/26/2018.
 */
@RunWith(AndroidJUnit4.class)
public class RobotControllerComponentAndroidTests {
    @Test
    public void Init(){
        RobotControllerComponent controllerComponent = new RobotControllerComponent("58853258"); //recondelta090 test robot
        controllerComponent.enable();
        Assert.assertTrue(controllerComponent.running.get());
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(controllerComponent.getConnected());
        controllerComponent.disable();
        latch = new CountDownLatch(1);
        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertFalse(controllerComponent.getConnected());
    }
}
