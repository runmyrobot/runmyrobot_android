package com.runmyrobot.android_robot_for_phone.api;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.runmyrobot.android_robot_for_phone.BuildConfig;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test TTS on its own, and make sure it can connect to the API
 */
@RunWith(AndroidJUnit4.class)
public class TTSAndroidTest {
    @Test
    public void Init(){
        TextToSpeechComponent textToSpeechComponent = new TextToSpeechComponent(InstrumentationRegistry.getTargetContext(), BuildConfig.ROBOT_ID);
        textToSpeechComponent.enable();
        Assert.assertTrue(textToSpeechComponent.getRunning().get());
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(2, TimeUnit.MINUTES); //Wait for 2 Minutes
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(textToSpeechComponent.getConnected()); //Make sure socket is actually connected
        textToSpeechComponent.disable(); //Disable TTS
        latch = new CountDownLatch(1);
        try {
            //wait a little bit to make sure it had time to disconnect
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertFalse(textToSpeechComponent.getConnected()); //Make sure disable works
    }
}
