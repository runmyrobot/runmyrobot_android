package com.runmyrobot.android_robot_for_phone;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import android.content.Context;


class RobotLocationListener implements LocationListener {

    public Socket toWebServerSocket;
    public AppCompatActivity applicationActivity;

    public RobotLocationListener(Socket socket, AppCompatActivity activity) {
        toWebServerSocket = socket;
        applicationActivity = activity;
    }

    @Override
    public void onLocationChanged(final Location location) {

        Button mButton=(Button)applicationActivity.findViewById(R.id.button1);
        if (mButton != null) mButton.setText(location.toString());

        Log.i("RobotListener", "location changed " + location.toString());
        Log.i("toWebServerSocket", toWebServerSocket.toString());
        JSONObject locationMessage = new JSONObject();
        try {
            locationMessage.put("robot_id", "22027911");
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
        Log.i("RobotListener", "finished emitting message");
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


public class MainActivity extends AppCompatActivity {

    Socket toWebserverSocketMemberVariable;
    LocationListener mLocationListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mButton=(Button)findViewById(R.id.button1);
        mButton.setText("number4");

        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        long LOCATION_REFRESH_TIME = 1000; // milliseconds
        float LOCATION_REFRESH_DISTANCE = 0.01F; // meters

        /*
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    LocationService.MY_PERMISSION_ACCESS_COURSE_LOCATION);
        }
        */

        /*
        final int REQUEST_LOCATION = 1;
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mButton.setText("have location permission");
        } else {
            mButton.setText("missing permission");
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Camera Permission is Needed.",
                        Toast.LENGTH_SHORT).show();
            }
            // request permissions
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        */


        try {
            // 10.0.2.2 is a special address, the computer you are developing on
            toWebserverSocketMemberVariable = IO.socket("http://runmyrobot.com:8022");
            testSocket();
            mLocationListener = new RobotLocationListener(toWebserverSocketMemberVariable, this);


        } catch (java.net.URISyntaxException name) {
            // print error message here
            Log.e("RobotSocket", "uri syntax exception");
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);

        Log.i("RobotLocation", "requested location updates");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);


    }


    private SensorManager mSensorManager;

    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public float lastValue = -10;
        public float threshold = -9;

        public void onSensorChanged(SensorEvent se) {
            //Log.i("RobotSensor", "sensor changed " + se.values[0] + " " + se.values[1] + " " + se.values[2]);
            if ((se.values[0] > threshold) && (lastValue <= threshold)) {
                Log.i("RobotSensor", "robot has tipped up too much: " + se.values[0] + " " + se.values[1] + " " + se.values[2]);
                toWebserverSocketMemberVariable.emit("rotation", "tipped_up");
            }
            if ((se.values[0] < threshold) && (lastValue >= threshold)) {
                Log.i("RobotSensor", "robot is not tipped up anymore: " + se.values[0] + " " + se.values[1] + " " + se.values[2]);
                toWebserverSocketMemberVariable.emit("rotation", "not_tipped_up");
            }

            lastValue = se.values[0];
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }


    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    */

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */


    void testSocket() {

        Log.i("RobotSocket", "test socket io method");

        toWebserverSocketMemberVariable.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.i("RobotSocket", "connection error: " + args.length + " " + args[0].toString());
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

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.i("RobotSocket", "on disconnect");
            }

        });

        toWebserverSocketMemberVariable.connect();
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