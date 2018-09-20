package com.runmyrobot.android_robot_for_phone.api;

import android.support.test.runner.AndroidJUnit4;

import com.runmyrobot.android_robot_for_phone.BuildConfig;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Specifically test just the robot control portion. No motors will move.
 *
 * This will connect to the LetsRobot api to ensure proper functionality and making sure that it
 * would send it to a motor
 *
 * To trigger events in the logs, please use the website controls on the robot page
 */
@RunWith(AndroidJUnit4.class)
public class RobotControllerComponentAndroidTests {
    @Test
    public void Init(){
        RobotControllerComponent controllerComponent = new RobotControllerComponent(BuildConfig.ROBOT_ID);
        controllerComponent.enable();
        Assert.assertTrue(controllerComponent.getRunning().get());
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(2, TimeUnit.MINUTES); //Wait for 2 Minutes
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(controllerComponent.getConnected()); //Make sure socket is actually connected
        controllerComponent.disable(); //Disable controller
        latch = new CountDownLatch(1);
        try {
            //wait a little bit to make sure it had time to disconnect
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertFalse(controllerComponent.getConnected()); //Make sure disable works
    }
}
