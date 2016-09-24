// port for audio and robot id must be set by hand (hardcoded) in this file


// code to make android compass:
// http://www.codingforandroid.com/2011/01/using-orientation-sensors-simple.html

package com.runmyrobot.android_robot_for_phone;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import android.content.Context;

import android.net.wifi.WifiManager;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


class RobotLocationListener implements LocationListener {

    public Socket toWebServerSocket;
    public AppCompatActivity applicationActivity;
    public String robotID;

    public RobotLocationListener(Socket socket, AppCompatActivity activity, String robotIDParameter) {
        toWebServerSocket = socket;
        applicationActivity = activity;
        robotID = robotIDParameter;
    }


    @Override
    public void onLocationChanged(final Location location) {

        Button mButton=(Button)applicationActivity.findViewById(R.id.button1);
        if (mButton != null) mButton.setText(location.toString());

        Log.i("RobotListener", "location changed " + location.toString());
        Log.i("toWebServerSocket", toWebServerSocket.toString());
        JSONObject locationMessage = new JSONObject();
        try {
            locationMessage.put("robot_id", robotID);
            locationMessage.put("latitude", location.getLatitude());
            locationMessage.put("longitude", location.getLongitude());
            if (location.hasBearing())
                locationMessage.put("bearing", location.getBearing());
            else
                locationMessage.put("bearing", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        toWebServerSocket.emit("current_location", locationMessage);
        Log.i("RobotListener", "finished emitting message" + locationMessage.toString());
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle b) {
        Log.i("RobotListener", "status changed");
    }
    @Override
    public void onProviderEnabled(String s) {
        Log.i("RobotListener", "provider enabled");
    }
    @Override
    public void onProviderDisabled(String s) {
        Log.i("RobotListener", "provider disabled");
    }
}



class CompassListener implements SensorEventListener {

    Float azimut;

    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    Socket toWebServerSocket;
    String robotID;


    public CompassListener(Socket socket, AppCompatActivity activity, String robotIDParameter) {

        toWebServerSocket = socket;
        robotID = robotIDParameter;

        mSensorManager = (SensorManager)activity.getSystemService(activity.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        int periodInMicroseconds = 100000;
        Log.i("RobotSensor", "registering accelerometer");
        mSensorManager.registerListener(this, accelerometer, periodInMicroseconds, periodInMicroseconds);
        Log.i("RobotSensor", "registering magnetometer");
        mSensorManager.registerListener(this, magnetometer, periodInMicroseconds, periodInMicroseconds);

    }

    float[] mGravity;
    float[] mGeomagnetic;
    float lastCompassBearingSent = 0;

    public void onSensorChanged(SensorEvent event) {
        //Log.i("RobotSensor", "accelerometer or magnetic field changed");
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        //Log.i("RobotSensor", "gravity: " + mGravity + "  geomagnetic: " + mGeomagnetic);
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                float offset = 90f;
                float compassBearing = (azimut*360/(2*3.14159f)) + offset;
                //Log.i("RobotSensor", "absolute value of difference: " + Math.abs(compassBearing - lastCompassBearingSent));
                if (Math.abs(compassBearing - lastCompassBearingSent) > 6) {
                    lastCompassBearingSent = compassBearing;
                    JSONObject message = new JSONObject();
                    try {
                        message.put("robot_id", robotID);
                        message.put("compass_bearing", compassBearing);
                        toWebServerSocket.emit("compass_bearing", message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.i("RobotSensor", "emitting compass bearing: " + message.toString());
                }

            }
        }
        //mCustomDrawableView.invalidate();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

}


public class MainActivity extends AppCompatActivity {

    Socket toWebServerSocketMemberVariable;
    LocationListener mLocationListener;
    CompassListener compassListener;
    //String robotID = "22027911"; // zip
    //String robotID = "88359766"; // skippy
    //String robotID = "3444925"; // timmy
    String robotID = "52225122"; // pippy
    AudioHandler audioHandler;
    TextToSpeech ttobj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        }
        );


        audioHandler = new AudioHandler(robotID);
        audioHandler.start();

        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        //wifiManager.setWifiEnabled(true);

        boolean wifiEnabled = wifiManager.isWifiEnabled();


        /*
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
        //PowerManager.WakeLock wakeLock = pm.newWakeLock((
        //        PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
        Log.i("RobotWake", "acquiring wake lock");
        wakeLock.acquire(600000); //todo: using number until find way to make it turn on forever, just leaving out the number does not seem to work
        */

        /* This code together with the one in onDestroy()
         * will make the screen be always on until this Activity gets destroyed. */
        /*
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
        */

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        Button mButton=(Button)findViewById(R.id.button1);
        mButton.setText("wifi_at_startup:" + wifiEnabled +
                        " robot_id:" + robotID +
                        " audio_port:" + AudioHandler.port);



        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        long LOCATION_REFRESH_TIME = 100; // milliseconds
        float LOCATION_REFRESH_DISTANCE = 0;//0.01F; // meters


        if (!Global.socketListeningInitialized) {
            try {
                // 10.0.2.2 is a special address, the computer you are developing on
                toWebServerSocketMemberVariable = IO.socket("http://runmyrobot.com:8022");
                chatSocket();
                mLocationListener = new RobotLocationListener(toWebServerSocketMemberVariable, this, robotID);


            } catch (java.net.URISyntaxException name) {
                // print error message here
                Log.e("RobotSocket", "uri syntax exception");
                mButton.setText("uri syntax exception");
            }


            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, mLocationListener);

            Log.i("RobotLocation", "requested location updates");

            //todo: these may not do anything
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

            Log.i("RobotSensor", "creating compass listener");
            compassListener = new CompassListener(toWebServerSocketMemberVariable, this, robotID);
            Global.socketListeningInitialized = true;
        } else {
            Log.i("Robot", "already initialized");
        }

    }


    private SensorManager mSensorManager;

    // rotation handler
    private final SensorEventListener mSensorListener = new SensorEventListener() {

        /*
        public int sensorIndex = 0;
        public float lastValue = -10;
        public float threshold = -9;
        */

        public int sensorIndex = 0;
        public float lastValue = 0;
        public float threshold = 2.5f;
        public float lowerThreshold = -0.9f;
        public float lastReportedValue = -100;
        public float minimumChange = 0.7f;


        public void onSensorChanged(SensorEvent se) {
            //Log.i("RobotRotation", "rotation sensor changed " + se.values[0] + " " + se.values[1] + " " + se.values[2]);
            //if ((Math.abs(lastReportedValue - se.values[sensorIndex]) > 0.2) &&(se.values[sensorIndex] > threshold) && (lastValue <= threshold)) {
            if ((Math.abs(lastReportedValue - se.values[sensorIndex]) > minimumChange) &&(se.values[sensorIndex] > threshold)) {
                JSONObject message = new JSONObject();
                try {
                    message.put("robot_id", robotID);
                    message.put("status", "tipped_up");
                    message.put("se0", se.values[0]);
                    message.put("se1", se.values[1]);
                    message.put("se2", se.values[2]);
                    //Log.i("RobotSensor", "robot has tipped up too much: " + se.values[0] + " " + se.values[1] + " " + se.values[2]);
                    Log.i("RobotRotation", message.toString());
                    toWebServerSocketMemberVariable.emit("rotation", message);
                    lastReportedValue = se.values[sensorIndex];
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //if ((Math.abs(lastReportedValue - se.values[sensorIndex]) > minimumChange) &&(se.values[sensorIndex] < lowerThreshold) && (lastValue >= lowerThreshold)) {
            if ((Math.abs(lastReportedValue - se.values[sensorIndex]) > minimumChange) &&(se.values[sensorIndex] < lowerThreshold)) {
                JSONObject message = new JSONObject();
                try {
                    message.put("robot_id", robotID);
                    message.put("status", "tipped_down");
                    message.put("se0", se.values[0]);
                    message.put("se1", se.values[1]);
                    message.put("se2", se.values[2]);
                    //Log.i("RobotSensor", "robot has tipped down too much: " + se.values[0] + " " + se.values[1] + " " + se.values[2]);
                    Log.i("RobotSensor", message.toString());
                    toWebServerSocketMemberVariable.emit("rotation", message);
                    lastReportedValue = se.values[sensorIndex];
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //if ((se.values[sensorIndex] < threshold) && (lastValue >= threshold)) {
            //if ((Math.abs(lastReportedValue - se.values[sensorIndex]) > 0.2) && (se.values[sensorIndex] > lowerThreshold) && (se.values[sensorIndex] < threshold)) {
            if ((Math.abs(lastReportedValue - se.values[sensorIndex]) > minimumChange) && (se.values[sensorIndex] > lowerThreshold)) {
                JSONObject message = new JSONObject();
                try {
                    Log.i("RobotSensor", "*****************************************");
                    message.put("robot_id", robotID);
                    message.put("status", "not_tipped_up"); //todo: call this something like "ok"
                    message.put("se0", se.values[0]);
                    message.put("se1", se.values[1]);
                    message.put("se2", se.values[2]);
                    //Log.i("RobotSensor", "robot is not tipped up anymore: " + se.values[0] + " " + se.values[1] + " " + se.values[2]);
                    Log.i("RobotSensor", message.toString());
                    toWebServerSocketMemberVariable.emit("rotation", message);
                    lastReportedValue = se.values[sensorIndex];
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            lastValue = se.values[sensorIndex];
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };



    @Override
    protected void onResume() {
        super.onResume();
        //mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        //mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        //mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }


    void chatSocket() {

        Log.i("RobotSocket", "test socket io method");

        //final Button mButton=(Button)findViewById(R.id.button1);

        toWebServerSocketMemberVariable.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.e("RobotSocket", "connection error: " + args.length + " " + args[0].toString());
                //mButton.setText("connection error: " + args.length + " " + args[0].toString());
            }

        }).on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.i("RobotSocket", "connected to the webserver");
                //socket.disconnect();
            }

        }).on("event", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.i("RobotSocket", "on event");
            }

        }).on("chat_message_with_name", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject)args[0];
                try {
                    //Log.i("RobotSocket", "chat message with name " + obj + " " + obj.get("name") + " " + args[0].getClass().getName() + " " + this.toString());
                    // uses all but the first word (which is the name of the robot)
                    String message = ((String)obj.get("message")).split(" ", 2)[1];
                    Log.i("RobotSocket", "chat message: " + message);

                    //ttobj.setLanguage(Locale.UK);
                    ttobj.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
                    runOnUiThread(new ChatRunnable(message) {
                        @Override
                        public void run() {
                            TextView textView = (TextView)findViewById(R.id.textView);
                            textView.setText(this.message);
                        }
                    });
                } catch (JSONException e) {
                    Log.e("RobotSocket", "JSON Exception: " + e.toString());
                }
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.i("RobotSocket", "on disconnect");
            }

        });

        toWebServerSocketMemberVariable.connect();
        Log.i("RobotSocket", "called socket connect4");

    }

}


/*
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
*/



class Global {
    public static boolean started = false;
    //public static boolean speechInitialized = false;
    public static boolean socketListeningInitialized = false;
}




class AudioHandler {


    //public static int port = 51005; // dev
    public static int port = 50005; // prod


    //private String destinationInternetAddress = "192.168.1.3"; // windows machine
    private String destinationInternetAddress = "runmyrobot.com"; // dev server

    private Button startButton,stopButton;

    public byte[] buffer;
    public static DatagramSocket socket;

    AudioRecord recorder;

    private int sampleRate = 16000 ; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //private int audioFormat = AudioFormat.ENCODING_PCM_8BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    //int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 10;
    //int minBufSize = 2048*64;
    private boolean status = true;

    private String robotID;


    AudioHandler(String robotIDParameter) {
        robotID = robotIDParameter;
    }

    public void start() {

        Log.i("Audio", "minBufSize: " + minBufSize);

        if (!Global.started) {
            status = true;
            startStreaming();
            Global.started = true;
        }

    }


    public static short[] toShorts(byte[] bytes, ByteOrder byteOrder) {
        short[] out = new short[bytes.length / 2]; // will drop last byte if odd number
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(byteOrder);
        for (int i = 0; i < out.length; i++) {
            out[i] = bb.getShort();
        }
        return out;
    }


    public static byte[] concatByteArrays(byte a[], byte b[]) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }


    public void startStreaming() {

        Thread streamThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    DatagramSocket socket = new DatagramSocket();
                    Log.d("Audio", "Socket Created");

                    byte[] buffer = new byte[minBufSize];

                    short[] buffer2 = new short[minBufSize];


                    Log.d("Audio","Buffer created of size " + minBufSize);
                    DatagramPacket packet;

                    final InetAddress destination = InetAddress.getByName(destinationInternetAddress);
                    Log.d("Audio", "Address retrieved: " + destination.toString());

                    Log.d("Audio", "Port: " + port);


                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*10);
                    //recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,10);
                    Log.d("Audio", "Recorder initialized");

                    recorder.startRecording();

                    int count = 0;

                    while(status == true) {

                        //Log.i("Audio", "------------------------------");

                        //reading data from MIC into buffer
                        minBufSize = recorder.read(buffer, 0, buffer.length);

                        //minBufSize = recorder.read(buffer2, 0, buffer.length);

                        //todo: this will definitely mess up the sound but i'm using it to debug right now
                        //buffer[0] = (byte)count;
                        //buffer[1] = 0;

                        // create a packet that has the robot id and the audio data
                        int robotIDChunkSize = 64;
                        byte[] robotIDChunk = new byte[robotIDChunkSize];
                        // copy robot id into the chunk
                        byte[] robotIDInBytes = robotID.getBytes("UTF-8");
                        for (int k=0; k<robotIDInBytes.length; k++)
                            robotIDChunk[k] = robotIDInBytes[k];
                        byte[] fullBuffer = concatByteArrays(robotIDChunk, buffer);

                        //putting full buffer in the packet
                        packet = new DatagramPacket(fullBuffer, fullBuffer.length, destination, port);

                        String message = "";
                        for (int index=0; index<50; index++) {
                            byte b2 = buffer[index];
                            //http://stackoverflow.com/questions/12310017/how-to-convert-a-byte-to-its-binary-string-representation
                            String s2 = String.format("%8s", Integer.toBinaryString(b2 & 0xFF));
                            message += s2;
                            message += " ";
                        }
                        //Log.i("Audio", "binary: " + message + "...");


                        short[] shorts = toShorts(buffer, ByteOrder.LITTLE_ENDIAN);
                        //short[] shorts = buffer2;
                        String message1 = "";
                        for (int index=0; index<50; index++) {
                            message1 += shorts[index];
                            message1 += " ";
                        }


                        short[] shortsBig = toShorts(buffer, ByteOrder.BIG_ENDIAN);
                        String messageBig = "";
                        for (int index=0; index<50; index++) {
                            messageBig += shortsBig[index];
                            messageBig += " ";
                        }

                        //Log.i("Audio", "little endian: " + message1 + "..."); // looks more correct
                        //Log.i("Audio", "big endian: " + messageBig + "...");
                        //Log.i("Audio", "length: " + buffer.length);

                        socket.send(packet);
                        //System.out.println("MinBufferSize: " +minBufSize);

                        count++;

                    }



                } catch(UnknownHostException e) {
                    Log.e("Audio", "UnknownHostException");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Audio", "IOException");
                }
            }

        });
        streamThread.start();
    }
}





