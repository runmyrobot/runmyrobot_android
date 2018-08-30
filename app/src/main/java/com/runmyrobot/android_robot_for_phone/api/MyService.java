package com.runmyrobot.android_robot_for_phone.api;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.runmyrobot.android_robot_for_phone.BuildConfig;
import com.runmyrobot.android_robot_for_phone.myrobot.RobotComponentList;

public class MyService extends Service implements Runnable{
    private Core core;
    private static Handler handler;
    private Thread thread;

    public synchronized static Handler getHandler() {
        return handler;
    }

    public MyService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        thread = new Thread(this);
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        core.disable();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        core.disable();
    }

    @Override
    public void run() {
        RobotComponentList.INSTANCE.init(getApplicationContext());
        Core.Builder builder = new Core.Builder(getApplicationContext());
        builder.robotId = BuildConfig.ROBOT_ID;
        try {
            core = builder.build();
        } catch (Core.InitializationException e) {
            e.printStackTrace();
        }
        core.enable();
    }
}
