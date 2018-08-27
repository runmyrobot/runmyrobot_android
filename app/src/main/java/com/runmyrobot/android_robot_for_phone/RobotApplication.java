package com.runmyrobot.android_robot_for_phone;

import android.app.Application;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Brendon on 8/26/2018.
 */
public class RobotApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {

                }

                @Override
                public void onFailure() {

                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onFinish() {

                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public static String getCameraPass() {
        return BuildConfig.CAMERA_PASS;
    }
}
