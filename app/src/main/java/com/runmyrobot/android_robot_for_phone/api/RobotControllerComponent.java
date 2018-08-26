package com.runmyrobot.android_robot_for_phone.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.runmyrobot.android_robot_for_phone.control.ControllerMessageManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Handles robot control Socket IO messages and broadcasts them through ControllerMessageManager
 *
 * Also grabs chat messages for TTS and sends it to ControllerMessageManager
 * Created by Brendon on 8/25/2018.
 */
public class RobotControllerComponent implements Emitter.Listener {
    public static final String ROBOT_DISCONNECTED = "robot_disconnect";
    public static final String ROBOT_CONNECTED = "robot_connect";
    public AtomicBoolean running = new AtomicBoolean(false);
    private String robotId;
    private Socket mSocket;
    private Context context;
    Handler handler;

    RobotControllerComponent(Context applicationContext, String robotId) {
        java.util.logging.Logger.getLogger(IO.class.getName()).setLevel(Level.FINEST);
        this.robotId = robotId;
        this.context = applicationContext;
        String host = null;
        String port = null;
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(new Request.Builder().url(String.format("https://letsrobot.tv/get_control_host_port/%s", robotId)).build());
        try {
            Response response = call.execute();
            if (response.body() != null) {
                JSONObject object = new JSONObject(response.body().string());
                Log.d("ROBOT", object.toString());
                host = object.getString("host");
                port = object.getString("port");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            String url = String.format("http://%s:%s", host, port);
            mSocket = IO.socket(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        try {
            Looper.prepare(); //Known to throw if already initialized. No way other than this to do it
        } catch (Exception ignored) {}
        handler = new Handler(Looper.myLooper());
    }

    TextToSpeech ttobj;

    public void enable() {
        if(running.getAndSet(true)){
            return;
        }

        ttobj = new TextToSpeech(context, new TextToSpeech.OnInitListener() { //TODO update to newer method
            @Override
            public void onInit(int status) {
            }
        });
        ttobj.setLanguage(Locale.US);
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                mSocket.emit("identify_robot_id", robotId);
                ControllerMessageManager.Companion.invoke(ROBOT_CONNECTED, null);
            }

        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.d("Robot", "Err");
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                ControllerMessageManager.Companion.invoke(ROBOT_DISCONNECTED, null);
                ControllerMessageManager.Companion.invoke("stop", null);
            }
        }).on("command_to_robot", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if(args != null && args[0] instanceof JSONObject){
                    JSONObject object = (JSONObject) args[0];
                    Log.d("Log", object.toString());
                    resetTimer(); //TODO validate that this actually works
                    try {
                        //broadcast what message was sent ex. F, stop, etc
                        ControllerMessageManager.Companion.invoke(object.getString("command"), null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //{"command":"F","robot_id":"55555555","user":{"username":"user","anonymous":false},"key_position":"down"}
                    //{"command":"stop","robot_id":"55555555","user":{"username":"user","anonymous":false},"key_position":"up"}
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
                    ControllerMessageManager.Companion.invoke("chat", object);
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

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ControllerMessageManager.Companion.invoke("messageTimeout", null);
        }
    };

    private void resetTimer() {
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 200);
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
