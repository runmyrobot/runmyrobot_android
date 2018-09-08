/*
 * Copyright (c) 2010 Jacek Fedorynski
 * Modified 2014-2016 Brendon Telman
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This file is derived from:
 * 
 * http://developer.android.com/resources/samples/BluetoothChat/src/com/example/android/BluetoothChat/DeviceListActivity.html
 * 
 * Copyright (c) 2009 The Android Open Source Project
 */

package com.runmyrobot.android_robot_for_phone.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.runmyrobot.android_robot_for_phone.R;
import com.runmyrobot.android_robot_for_phone.utils.StoreUtil;

import java.util.Set;

public class ChooseBluetoothActivity extends Activity {

    public static final String EXTRA_DEVICE_NAME = "device_name";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public String tag = "ChooseBluetoothActivity";
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private BluetoothAdapter mBtAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(tag, "On Create");
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        Log.i(tag, "Setting View to R.layout.device_list");
        setContentView(R.layout.device_list);
        Log.i(tag, "Set View Done");
        setResult(Activity.RESULT_CANCELED);
        Log.i(tag, "Initializing Scan Button");
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });
        
        mPairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        
        Log.i(tag, "Setting up paired_devices ListView code");
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        
        Log.i(tag, "Setting up new_devices ListView code");
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        
        Log.i(tag, "Setting up IntentFilter ACTION_FOUND code");
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        Log.i(tag, "Setting up IntentFilter ACTION_DISCOVERY_FINISHED code");
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        
        Log.i(tag, "Setting up BluetoothAdapter");
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        
        Log.i(tag, "Getting Paired Devices");
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        
        boolean empty = true;
        
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if ((device.getBluetoothClass() != null) && (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.TOY_ROBOT)) {
                	Log.i(tag, "Found Paired Device " + device.getName() + " with address of " + device.getAddress());
                	mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    empty = false;
                }
            }
        }
        if (!empty) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            findViewById(R.id.no_devices).setVisibility(View.GONE);
        }
        Log.i(tag, "Done with onCreate");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        
        this.unregisterReceiver(mReceiver);
        Log.i(tag, "On Destroy");
    }

    private void doDiscovery() {
        setProgressBarIndeterminateVisibility(true);
        setTitle("Scanning...");
        Log.i(tag, "Discovery");
        //findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        
        mBtAdapter.startDiscovery();
        
        mNewDevicesArrayAdapter.clear();
        findViewById(R.id.title_new_devices).setVisibility(View.GONE);
        if (mPairedDevicesArrayAdapter.getCount() == 0) {
            findViewById(R.id.no_devices).setVisibility(View.VISIBLE);
        }
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            mBtAdapter.cancelDiscovery();
            Log.i(tag, "On Click");
            String info = ((TextView) v).getText().toString();
            String[] deviceInfo = info.split("\n");
            //String address = info.substring(info.length() - 17);
            String address = deviceInfo[1];
            String name = deviceInfo[0];
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            intent.putExtra(EXTRA_DEVICE_NAME, name);
            setResult(Activity.RESULT_OK, intent);
            //save it
            StoreUtil.INSTANCE.SetBluetoothDevice(getApplicationContext(), name, address);
            finish();
        }
    };
    
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.i(tag, "On Receive");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if ((device.getBondState() == BluetoothDevice.BOND_BONDED)/* && (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.TOY_ROBOT)*/) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
                    findViewById(R.id.no_devices).setVisibility(View.GONE);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i(tag, "Scan Complete");
                setProgressBarIndeterminateVisibility(false);
                setTitle("Select device");
                findViewById(R.id.button_scan).setVisibility(View.VISIBLE);
            }
        }
    };
}
