package com.runmyrobot.android_robot_for_phone.api;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.runmyrobot.android_robot_for_phone.myrobot.RobotComponentList;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * letsrobot.tv core android api.
 *
 * This is configured with a builder to make sure no settings are changed while Core is running.
 * Core consists of multiple components
 * - Camera (only enabled if camera id is given)
 * - Robot Controller (Only enabled if robot id is given)
 * - Text to Speech (Defaults to disabled)
 *
 * Builder can allow for configuration of these individual components.
 * Disabling individual components is possible.
 *
 * Created by Brendon on 8/25/2018.
 */
public class Core {
    @SuppressWarnings("FieldCanBeLocal")
    private static String TAG = "RobotCore";
    private static AtomicBoolean running = new AtomicBoolean(false);
    private LogLevel logLevel = LogLevel.NONE;

    @Nullable
    private CameraComponent camera = null;
    @Nullable
    private RobotControllerComponent robotController = null;
    @Nullable
    private TextToSpeechComponent textToSpeech = null;
    private static Looper looper;
    private ArrayList<Component> externalComponents = null;

    public static Looper getLooper() {
        return looper;
    }

    /**
     * The Class's log level
     */
    public enum LogLevel{
        TRACE,
        INFO,
        DEBUG,
        WARNING,
        ERROR,
        NONE
    }

    /**
     * Log internal class messages, and allow log level to be adjusted specifically for this class
     * @param logLevel Log level to use
     * @param message message
     */
    private void log(LogLevel logLevel, String message){
        if(logLevel.ordinal() < this.logLevel.ordinal()){
            switch (logLevel){
                case TRACE:
                    Log.d(TAG, message, new Exception());
                    break;
                case INFO:
                    Log.i(TAG, message);
                    break;
                case DEBUG:
                    Log.d(TAG, message);
                    break;
                case WARNING:
                    Log.w(TAG, message);
                    break;
                case ERROR:
                    Log.e(TAG, message, new Exception());
                    break;
                case NONE:
                    break;
            }
        }
    }

    /**
     * Intentional private initializer. Use Builder to get an instance of Core
     */
    private Core(){

    }

    /**
     * Enable the LetsRobot android core. This will start from scratch.
     * Nothing except settings have been initialized before this call.
     * @return true if successful, false if already started
     */
    public boolean enable(){
        if(running.getAndSet(true)){
           return false;
        }
        log(LogLevel.INFO, "starting core...");
        if(robotController != null){
            robotController.enable();
        }
        if(camera != null){
            camera.enable();
        }
        if(textToSpeech != null){
            textToSpeech.enable();
        }
        for (Component component : externalComponents) {
            component.enable();
        }
        log(LogLevel.INFO, "core is started!");
        return true;
    }

    /**
     * Disables the api and resets it to before it was initialized.
     * This should be called in OnDestroy() when the app gets killed.
     * @return true if disable successful, or false if already in disabled state
     */
    public boolean disable(){
        if(!running.getAndSet(false)){
            return false;
        }
        log(LogLevel.INFO, "shutting down core...");
        if(robotController != null){
            robotController.disable();
        }
        if(camera != null){
            camera.disable();
        }
        if(textToSpeech != null){
            textToSpeech.disable();
        }
        for (Component component : externalComponents) {
            component.disable();
        }
        log(LogLevel.INFO, "core is shut down!");
        return true;
    }

    /**
     * Builder for the Core api.
     * Call build() after changing settings to have the actual Core class returned.
     */
    public static class Builder{
        Context context;

        public LogLevel logLevel = LogLevel.NONE;
        /**
         * Id that should be used to receive chat messages from server
         */
        public String robotId = null;

        /**
         * Id for the camera to use to send video to the website
         */
        public String cameraId = null;

        /**
         * Should chat messages be piped through the android speaker?
         */
        public boolean useTTS = false;

        /**
         * Instantiate for a Builder instance. After settings have been confirmed,
         * please call build() to receive an instance to Core
         *
         * Will throw NullPointerException if context is null
         * @param context Application context
         */
        public Builder(Context context){
            this.context = context.getApplicationContext();
        }

        /**
         * Build a configured instance of Core.
         * @return Core
         * @throws InitializationException
         */
        public Core build() throws InitializationException {
            //TODO define preconditions that will throw errors
            if(robotId == null && cameraId == null){
                throw new InitializationException();
            }
            Core core = new Core();
            if(robotId != null){
                 core.robotController = new RobotControllerComponent(context, robotId);
                 //TODO init robot controller
            }
            if(cameraId != null){
                core.camera = new CameraComponent(context, cameraId);
                //TODO init camera
            }
            if(useTTS){
                core.textToSpeech = new TextToSpeechComponent(context);
                //TODO init text to speech
            }
            //Set the log level
            core.logLevel = logLevel;

            //Get list of external components, such as LED code, or motor control
            core.externalComponents = RobotComponentList.INSTANCE.getComponents();
            return core;
        }
    }

    public static class InitializationException extends Exception{}
}
