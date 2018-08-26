package com.runmyrobot.android_robot_for_phone.api;

import android.util.Log;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;

/**
 * Created by Brendon on 8/25/2018.
 */
public class RobotControllerComponent implements Emitter.Listener {
    public AtomicBoolean running = new AtomicBoolean(false);
    private String robotId;
    private Socket mSocket;

    RobotControllerComponent(String robotId) {
        java.util.logging.Logger.getLogger(IO.class.getName()).setLevel(Level.FINEST);
        this.robotId = robotId;
        try {
            mSocket = IO.socket("http://runmyrobot.com:8022");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }



    public void enable() {
        if(running.getAndSet(true)){
            return;
        }
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
                Log.d("Log", "robot command!");
            }
        }).on("chat_message_with_name", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("Log", "chat_message_with_name");
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
