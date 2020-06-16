package com.ddukddak.sangsa;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.LogRecord;

public class BluetoothService {


    public static final int STATE_NONE=1;
    public static final int STATE_LISTEN=2;
    public static final int STATE_CONNECTING =3;
    public static final int STATE_CONNECTED =4;
    public static final int STATE_FAIL =7;

    public int mState;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    public static final int REQUEST_ENABLE_BT = 2;
    public static final int REQUEST_CONNECT_DEVICE = 1;

    public static final String TAG = "BluetoothService";

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private BluetoothAdapter btAdapter;
    private Activity mActivity;
    private Handler mHandler;

    public BluetoothService(Activity activity, Handler handler) {
        mActivity = activity;
        mHandler = handler;

        btAdapter = BluetoothAdapter.getDefaultAdapter();


    }


    /* (1) getDeviceState() : 가장먼저 기기의 블루투스 지원여부를 확인한다.*/

    public boolean getDeviceState() {
        Log.d(TAG, "Check the Bluetooth support");

        if (btAdapter == null) {
            Log.d(TAG, "Bluetooth is not available");
            return false;
        } else {
            Log.d(TAG, "Bluetooth is available");
            return true;
        }
    }

    /*(2) enableBluetooth() : bluetooth활성화 메소드 (getDeviceState가 true를 반환시 활성화를 요청)*/
    public void enableBluetooth() {
        Log.i(TAG, "Check the enable Bluetooth");

        if (btAdapter.isEnabled()) {
            //기기의 블루투스 상태가 On일 경우..
            Log.d(TAG, "Bluetooth Enable Now");

            //블루투스 장치 검색
            scanDevice();

        } else {
            //기기의 블루투스 상태가 off일 경우
            Log.d(TAG, "Bluetooth Enable Request");

            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(i, REQUEST_ENABLE_BT);
        }
    }

    public void scanDevice() {
        Log.d(TAG, "Scan Device");

        Intent serverIntent = new Intent(mActivity, DeviceListActivity.class);
        mActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }
    private synchronized void setState(int state){
        Log.d(TAG, "setState()"+ mState+ "->"+state);
        mState = state;
        mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();

    }
    public synchronized int getState(){
        return mState;
    }

    public synchronized void start(){
        Log.d(TAG, "start");

        if(mConnectedThread==null){

        }else{
            mConnectThread.cancel();
            mConnectThread = null;
        }
    }

    public void getDeviceinfo(Intent data){
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        Log.d(TAG, "Get Device Info \n" + "address : "+address);

        connect(device);
    }

    private synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " +device);

        if(mState ==STATE_CONNECTING){
            if(mConnectThread ==null){

            }else{
                mConnectThread.cancel();
                mConnectThread = null;
            }
            mConnectThread = new ConnectThread(device);

            mConnectThread.start();
            setState(STATE_CONNECTING);
        }
    }
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device){
        Log.d(TAG, "connected");

        if(mConnectThread ==null){

        }else{
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mConnectedThread == null){

        }else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }
    public synchronized  void stop(){
        Log.d(TAG, "stop");

        if(mConnectedThread !=null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mConnectedThread != null){
            mConnectedThread = null;
        }
        setState(STATE_NONE);

    }
    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            try{
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            }
            catch(IOException e){
                Log.e(TAG, "create( failed", e);
            }
            mmSocket = tmp;
        }
        public void run(){
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            btAdapter.cancelDiscovery();

            try{
                mmSocket.connect();
                Log.d(TAG, "Connect Success");
            }
            catch(IOException e){
                connectionFailed();
                Log.d(TAG, "Connect Fail");

                try{
                    mmSocket.close();
                }
                catch(IOException e2){
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                BluetoothService.this.start();
                return;
            }
            synchronized (BluetoothService.this){
                mConnectThread = null;
            }
            connected(mmSocket, mmDevice);
        }
        public void cancel(){
            try{
                mmSocket.close();
            }catch(IOException e){
                Log.e(TAG, "close() of connect socket failed ", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

// BluetoothSocket의 inputstream 과 outputstream을 얻는다.
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

// Keep listening to the InputStream while connected
            while (true) {
                try {
// InputStream으로부터 값을 받는 읽는 부분(값을 받는다)
                    bytes = mmInStream.read(buffer);

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
// 값을 쓰는 부분(값을 보낸다)
                mmOutStream.write(buffer);

            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    public void write(byte[] out) { // Create temporary object
        ConnectedThread r; // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        } // Perform the write unsynchronized r.write(out); }
    }

    private void connectionFailed() {
        setState(STATE_FAIL);
    }

    /* connectionLost() : 연결을 잃었을 때 */
    private void connectionLost() {
        setState(STATE_LISTEN);
    }


}
