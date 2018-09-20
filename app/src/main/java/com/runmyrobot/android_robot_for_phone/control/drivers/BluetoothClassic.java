package com.runmyrobot.android_robot_for_phone.control.drivers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * This class handles Bluetooth classic connections. This does not support BLE or Bluetooth Gatt
 */
public class BluetoothClassic{
	private String tag = "BluetoothClassic";
	public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ = 1;
	public static final int BLUETOOTH_ADAPTER_MESSAGE = 5;
	public static final int DESTROY = 6;
	public static final int CONNECT_MESSAGE = 2;
	public static final int DISCONNECT_MESSAGE = 3;
	public static final int SEND_MESSAGE = 4;
	public static final int CONNECTION_STABLE = 0,CONNECTION_LOST = 1,CONNECTION_NOT_POSSIBLE = 2,CONNECTION_NON_EXISTENT = 3, CONNECTING = 4;
	public int BTStatus = CONNECTION_NON_EXISTENT;
	public BluetoothAdapter btAdapter;
	private IntentFilter filter;

	public Handler serviceHandler;
	private BluetoothDevice selectedDevice;
	private ConnectThread connect;
	private ConnectedThread connectedThread;
	private Context gContext;
	byte[] readBuffer = {0x00};
	public BluetoothClassic(Context context){
		gContext = context;
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		BluetoothInit();
	}
	public int getStatus(){
		return BTStatus;
	}

	@SuppressLint("HandlerLeak")
	private void BluetoothInit(){
		serviceHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what){
				case SUCCESS_CONNECT:
					// DO something
					connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
					connectedThread.start();
					Log.i(tag, "connected");
					break;
				case MESSAGE_READ:
					//byte[] readBuf = (byte[])msg.obj;
					// TODO make reading code
					break;
				case CONNECT_MESSAGE:
					BTStatus = CONNECTING;
					try{
						connectedThread.cancel();
					}catch(Exception ignored){

					}
					selectedDevice = (BluetoothDevice)msg.obj;
					connect = new ConnectThread(selectedDevice);
					connect.start();
					break;
				case DISCONNECT_MESSAGE:
					if(connectedThread != null)
						connectedThread.cancel();
					break;
				case SEND_MESSAGE:
					byte[] message;
					try{
					message = (byte[]) msg.obj;
					write(message);
					}catch(Exception e){
						e.printStackTrace();
						//Log.e("BLUETOOTH", "CANNOT WRITE");
						BTStatus = CONNECTION_LOST;
					}
					break;
				case DESTROY:
					try {
						try {
							connect.cancel();
						} catch (Exception e) {
							e.printStackTrace();
						}
						connectedThread.cancel();
					} catch (Exception e) {
						e.printStackTrace();
					}
					Log.i(tag, "destroyed");
					break;
				}
			}
		};
	}

	public void connect(String address){
		Message message = Message.obtain();
		message.obj = btAdapter.getRemoteDevice(address);
		message.what = CONNECT_MESSAGE;
		serviceHandler.sendMessage(message);
	}

	/**
	 * Write a message. Please use the send command, which is thread safe
	 * @param out byte[]
	 * @throws IOException
	 */
    private void write(byte[] out) throws IOException {
		Log.i(tag, "In write void with " + Arrays.toString(out) + " as message");
        connectedThread.write(out);
    }

	public void ensurePoweredOn() {
		if(!btAdapter.isEnabled()) {
			btAdapter.enable();
		}
	}

	public class BluetoothConnect extends AsyncTask<BluetoothDevice, Void, Void> {
    	public String tag;
		private BluetoothSocket mmSocket;
	    private BluetoothDevice mmDevice;

		@Override
		protected Void doInBackground(BluetoothDevice... arg0) {
			BluetoothSocket tmp = null;
	        mmDevice = arg0[0];
	        Log.i(tag, "construct");
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
	        } catch (IOException e) {
	        	Log.i(tag, "get socket failed");

	        }
	        mmSocket = tmp;


	        // Do work to manage the connection (in a separate thread)


				///--Rest of Connection
				btAdapter.cancelDiscovery();
				Log.i(tag, "connect - run");
				try {
					// Connect the device through the socket. This will block
					// until it succeeds or throws an exception
					mmSocket.connect();
					BTStatus = CONNECTION_STABLE;
					Log.i(tag, "connect - succeeded");
				} catch (IOException connectException) {
					Log.i(tag, "connect failed");
					// Unable to connect; close the socket and get out
					BTStatus = CONNECTION_NOT_POSSIBLE;
					try {
						mmSocket.close();
					} catch (IOException closeException) {
					}
					this.cancel(true);
				}
				serviceHandler.obtainMessage(SUCCESS_CONNECT, mmSocket)
						.sendToTarget();
				//serviceHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
			return null;
		}

	}
	private class ConnectThread extends Thread {
	   	public String tag;
		private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;

	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	        Log.i(tag, "construct");
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
	        } catch (IOException e) {
	        	Log.i(tag, "get socket failed");

	        }
	        mmSocket = tmp;
	    }

	    @Override
		public void run() {
	        // Cancel discovery because it will slow down the connection
	        btAdapter.cancelDiscovery();
	        Log.i(tag, "connect - run");
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	            BTStatus = CONNECTION_STABLE;
	            Log.i(tag, "connect - succeeded");
	        } catch (IOException connectException) {	Log.i(tag, "connect failed");
	            // Unable to connect; close the socket and get out
	        BTStatus = CONNECTION_NOT_POSSIBLE;
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }

	        // Do work to manage the connection (in a separate thread)

	        serviceHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
	    }

	    @SuppressWarnings("unused")
		public void cancel() {
	        try {
	            mmSocket.close();
	            this.stop();
	        } catch (IOException e) { }
	    }
	}

	private void disconnect(){

	}

    //TODO turn into Timer
	class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;

	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }

	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	    @Override
		public void run() {
	    	int errCount = 0;
	        byte[] buffer;  // buffer store for the stream
	        int bytes; // bytes returned from read()
	        boolean readInput = true;
	        // Keep listening to the InputStream until an exception occurs
	        while (readInput && !Thread.currentThread().isInterrupted()) {
	            try {
	                // Read from the InputStream
	            	buffer = new byte[66];
	            	if(mmSocket != null && !this.isInterrupted())
	                bytes = mmInStream.read(buffer);
	                // Send the obtained bytes to the UI activity
	                //serviceHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
	                     //   .sendToTarget();
	                readBuffer = buffer;
	                errCount = 0;
	                BTStatus = CONNECTION_STABLE; //TODO
	            } catch (Exception e) {
	                errCount += 1;
	                if(errCount > 50){
	                	BTStatus = CONNECTION_LOST; //TODO
	                	break;
	                }
	            }
	        }
	    }

	    public void write(byte[] bytes) throws IOException {
	    	Log.i("CONNECTEDTHREAD", "In write void with " + bytes + " as message");
			if (mmOutStream == null)
				throw new IOException();
			mmOutStream.write(bytes);
	        Log.i(tag,"Successfull write");
	    }

	    public void sendMessage(byte[] message) throws IOException {
	        if (mmOutStream == null)
	            throw new IOException();

	        // send message length
	        int messageLength = message.length;
	        mmOutStream.write(messageLength);
	        mmOutStream.write(messageLength >> 8);
	        mmOutStream.write(message, 0, message.length);
	    }

	    public void cancel() {
	        try {
	            mmSocket.close();
	            //TODO STOP THREAD
	            this.interrupt();
	        } catch (IOException e) {
				//
			}
	    }
	}
}
