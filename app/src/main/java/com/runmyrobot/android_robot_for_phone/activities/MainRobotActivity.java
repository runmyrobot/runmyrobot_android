package com.runmyrobot.android_robot_for_phone.activities;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.runmyrobot.android_robot_for_phone.BuildConfig;
import com.runmyrobot.android_robot_for_phone.R;
import com.runmyrobot.android_robot_for_phone.api.Core;

/**
 * Based off of this sample
 * https://github.com/vanevery/Android-MJPEG-Video-Capture-FFMPEG/blob/master/src/com/mobvcasting/mjpegffmpeg/MJPEGFFMPEGTest.java
 */
public class MainRobotActivity extends Activity implements OnClickListener{

    public static final String LOGTAG = "MJPEG_FFMPEG";
    boolean recording = false;
    Button recordButton;
    public Core core;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_mjpegffmpegtest);
        recordButton = (Button) this.findViewById(R.id.RecordButton);
        recordButton.setOnClickListener(this);
        SurfaceView cameraView = (SurfaceView) findViewById(R.id.CameraView);
        final Core.Builder builder = new Core.Builder(getApplicationContext());
        builder.holder = cameraView.getHolder();
        builder.robotId = BuildConfig.ROBOT_ID;
        builder.cameraId = BuildConfig.CAMERA_ID;
        try {
            core = builder.build();
        } catch (Core.InitializationException e) {
            e.printStackTrace();
        }
        cameraView.setClickable(true);
        cameraView.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (recording)
        {
            recording = false;
            if(core != null)
                core.disable();
            Log.v(LOGTAG, "Recording Stopped");
            // Convert to video
            //processVideo = new ProcessVideo();
            //processVideo.execute();
        }
        else
        {
            recording = true;
            if(core != null)
                core.enable();
            Log.v(LOGTAG, "Recording Started");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration conf)
    {
        super.onConfigurationChanged(conf);
    }
}