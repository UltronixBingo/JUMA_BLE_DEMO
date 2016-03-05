package com.example.lb.juma_ble_demo;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.juma.sdk.JumaDevice;
import com.juma.sdk.JumaDeviceCallback;
import com.juma.sdk.ScanHelper;

import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private ScanHelper scanner;
    private JumaDevice myDevice;
    private HashMap<UUID, JumaDevice> deviceList =  new HashMap<UUID, JumaDevice>();
    public static final String ACTION_DEVICE_DISCOVERED = "com.example.temperaturegatheringdemo.ACTION_DEVICE_DISCOVERED";
    private Button bConnect;
    private TextView mDataSend;
    private TextView mDataDisplay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDataSend = (EditText) findViewById(R.id.SendText);
        mDataDisplay =(TextView)findViewById(R.id.textView);
        bConnect= (Button)findViewById(R.id.bConnect);
       scanDevice();
    }

    private void scanDevice(){
        scanner = new ScanHelper(getApplicationContext(), new ScanHelper.ScanCallback(){
            @Override
            public void onDiscover(JumaDevice device, int rssi) {
                if(!deviceList.containsKey(device.getUuid())){
                    deviceList.put(device.getUuid(), device);
                    Intent intent = new Intent(MainActivity.ACTION_DEVICE_DISCOVERED);
                    intent.putExtra("name", device.getName());
                    intent.putExtra("uuid", device.getUuid().toString());
                    intent.putExtra("rssi", rssi);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            }
            @Override
            public void onScanStateChange(int arg0) {
            }
        });
    }

    public void onClear(View v){//点击Clear响应函数
        mDataDisplay.setText("");
    }
    public void onScan(View v){//点击Scan响应函数
        deviceList.clear();
        scanner.startScan(null);
        final CustomDialog scanDialog = new CustomDialog(MainActivity.this);
        scanDialog.setScanCallback(new CustomDialog.Callback() {
            @Override
            public void onDevice(final UUID uuid, final String name) {
                scanner.stopScan();
                myDevice = deviceList.get(uuid);
                bConnect.setText("TO DISCONNECT");
                myDevice.connect(callback);
            }
            @Override
            public void onDismiss() {
                scanner.stopScan();
            }
        });
        scanDialog.setNegativeButton(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                scanDialog.dismiss();
            }
        });
        scanDialog.show();
    }
    public void onSend(View v){//点击发送响应函数
        if(myDevice != null && myDevice.isConnected()) {
            String sendString = mDataSend.getText().toString();
            byte[] srtbyte = sendString.getBytes();
            if(sendString.length() > 0)
                myDevice.send((byte)9, srtbyte);
        }
    }
    public void onConnect(View v) {//点击Connect响应函数
        if (bConnect.getText().equals("TO DISCONNECT")) {
            myDevice.disconnect();
        } else {
            if (myDevice != null) {
                myDevice.connect(callback);
            }
        }
    }
    private JumaDeviceCallback callback = new JumaDeviceCallback() {
        @Override
        public void onConnectionStateChange(int status, int newState) {
            super.onConnectionStateChange(status, newState);
            if (newState == JumaDevice.STATE_CONNECTED && status == JumaDevice.SUCCESS) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        bConnect.setText("TO DISCONNECT");
                        getSupportActionBar().setTitle(myDevice.getName() + " is connect");

                    }

                });
            } else if (newState == JumaDevice.STATE_DISCONNECTED) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        bConnect.setText("TO CONNECT");
                        getSupportActionBar().setTitle(myDevice.getName() + " is disconnect");
                    }

                });
            }
        }
        String s1;
       byte s2;
        @Override
        public void onReceive(byte type, byte[] message) {
            super.onReceive(type, message);
            s1=new String(message);
            s2=type;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("onReceive", s2+ " "+s1);
                    mDataDisplay.append(s1);
                }
            });
        }
    };
}
