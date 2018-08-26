package com.runmyrobot.android_robot_for_phone.api;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by Brendon on 8/25/2018.
 */
public class RobotControllerComponent implements Emitter.Listener {
    public AtomicBoolean running = new AtomicBoolean(false);
    private String robotId;
    private Socket mSocket;
    private Context context;

    RobotControllerComponent(Context applicationContext, String robotId) {
        java.util.logging.Logger.getLogger(IO.class.getName()).setLevel(Level.FINEST);
        this.robotId = robotId;
        this.context = applicationContext;
        try {
            mSocket = IO.socket("http://runmyrobot.com:8022");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    TextToSpeech ttobj;

    public void enable() {
        if(running.getAndSet(true)){
            return;
        }

        ttobj = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
        ttobj.setLanguage(Locale.US);
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                mSocket.emit("identify_robot_id", robotId);
                Log.d("Robot", "Connection!");
                //mSocket.disconnect();
            }

        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.d("Robot", "Err");
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {}

        }).on("command_to_robot", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if(args != null && args[0] instanceof JSONObject){
                    JSONObject object = (JSONObject) args[0];
                    Log.d("Log", object.toString());
                    //{"command":"F","robot_id":"58853258","user":{"username":"recondelta090","anonymous":false},"key_position":"down"}
                    //{"command":"stop","robot_id":"58853258","user":{"username":"recondelta090","anonymous":false},"key_position":"up"}
                }

            }
        }).on("chat_message_with_name", new Emitter.Listener() {
            //TODO relocate to TextToSpeechComponent.java
            @Override
            public void call(Object... args) {
                Log.d("Log", "chat_message_with_name");
                if(args != null && args[0] instanceof JSONObject) {
                    JSONObject object = (JSONObject) args[0];
                    Log.d("Log", object.toString());
                    try {
                        String[] split = object.getString("message").split("]");
                        ttobj.speak(split[split.length-1], TextToSpeech.QUEUE_FLUSH, null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        /**
         * controlSocketIO.on('command_to_robot', onHandleCommand)
         controlSocketIO.on('disconnect', onHandleControlDisconnect)

         appServerSocketIO.on('exclusive_control', onHandleExclusiveControl)
         appServerSocketIO.on('connect', onHandleAppServerConnect)
         appServerSocketIO.on('reconnect', onHandleAppServerReconnect)
         appServerSocketIO.on('disconnect', onHandleAppServerDisconnect)

         if commandArgs.tts_delay_enabled:
         userSocket.on('message_removed', onHandleChatMessageRemoved)

         if commandArgs.enable_chat_server_connection:
         chatSocket.on('chat_message_with_name', onHandleChatMessage)
         chatSocket.on('connect', onHandleChatConnect)
         chatSocket.on('reconnect', onHandleChatReconnect)
         chatSocket.on('disconnect', onHandleChatDisconnect)
         */
        mSocket.connect();
        //TODO
    }

    public void disable(){
        if(!running.getAndSet(false)){
            return;
        }
        mSocket.disconnect();
        //TODO
    }

    @Override
    public void call(Object... args) {
        Log.d("Controller", Arrays.toString(args));
    }

    public boolean getConnected() {
        return mSocket != null && mSocket.connected();
    }
}
