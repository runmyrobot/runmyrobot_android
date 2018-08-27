package com.runmyrobot.android_robot_for_phone.api;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import android.support.test.rule.GrantPermissionRule;
import android.support.v4.content.ContextCompat;

/**
 * Created by Brendon on 8/26/2018.
 */
@RunWith(AndroidJUnit4.class)
public class CameraComponentAndroidTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule .grant(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE);
    @Test
    public void Init(){
        if (ContextCompat.checkSelfPermission(InstrumentationRegistry.getTargetContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Assert.fail();
        }
        CameraComponent component = new CameraComponent(InstrumentationRegistry.getTargetContext(), "74815067");
        component.enable();
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
