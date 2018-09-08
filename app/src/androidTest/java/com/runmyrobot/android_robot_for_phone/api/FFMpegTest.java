package com.runmyrobot.android_robot_for_phone.api;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test if FFmpeg is in working condition
 */
@RunWith(AndroidJUnit4.class)
public class FFMpegTest {

    @Test
    public void testInit() throws FFmpegNotSupportedException {
        FFmpeg ffmpeg = FFmpeg.getInstance(InstrumentationRegistry.getTargetContext());
        final CountDownLatch latch = new CountDownLatch(1);
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {}

                @Override
                public void onFailure() {
                    Assert.fail();
                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onFinish() {
                    latch.countDown();
                }
            });
        try {
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
