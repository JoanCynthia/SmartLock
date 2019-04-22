package com.example.susmita.smartlock;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class GenerateOTP extends Activity {
    private static final String MATCH_QR = "MB17900257";
    //    Button oTP,btnScan;
    MyTask myTask1;
    MyTask2 myTask2;
    String str, str2;
    String ph_uuid;
    ProgressBar p_bar;
    //    TextView setOTP,resendOTP;
    CircleImageView unlock, lock, unlockSeat, lockSeat;
    String tempQrData;
    private static final int REQUEST_QRCODE = 8;


    private String mDeviceName;
    private String mDeviceAddress;


    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public Bluetoothservice mBluetoothLeService;
    public boolean mConnected = false;
    public BluetoothGattCharacteristic characteristicTX;
    public BluetoothGattCharacteristic characteristicRX;
    public final static UUID HM_RX_TX = UUID.fromString(SampleGattAttributes.HM_RX_TX);
    String uuid;


    public final String LIST_NAME = "NAME";
    public final String LIST_UUID = "UUID";


    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            try {
                mBluetoothLeService = ((Bluetoothservice.LocalBinder) service).getService();
                if (!mBluetoothLeService.initialize()) {
                    Log.v("check", "Unable to initialize Bluetooth");
                    return;
                }
                // Automatically connects to the device upon successful start-up initialization.
                try {
                    mBluetoothLeService.connect(mDeviceAddress);
                } catch (Throwable e) {
                    e.printStackTrace();
                    Log.d("check", e.toString());
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Log.d("sus", e.toString());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                final String action = intent.getAction();
                if (Bluetoothservice.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnected = true;
                    Toast.makeText(GenerateOTP.this, "Connected", Toast.LENGTH_SHORT).show();
                } else if (Bluetoothservice.ACTION_GATT_DISCONNECTED.equals(action)) {
                    mConnected = false;
                    Toast.makeText(GenerateOTP.this, "DisConnected", Toast.LENGTH_SHORT).show();
                } else if (Bluetoothservice.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    Log.i("Swapnil", "reached");
                    // Show all the supported services and characteristics on the user interface.
                    displayGattServices(mBluetoothLeService.getSupportedGattServices());
                    if (characteristicTX == null) {
                        Log.i("Sw", "Its not nullllllll");
                    }
                } else if (Bluetoothservice.ACTION_DATA_AVAILABLE.equals(action)) {
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Log.d("sus", e.toString());
            }
        }
    };
    BluetoothDevice mDevice;
    String writeMessage;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_QRCODE && resultCode == RESULT_OK && data != null) {
            String qrData = data.getStringExtra("qr_data");
            tempQrData = qrData;
//            Toast.makeText(mBluetoothLeService, qrData, Toast.LENGTH_SHORT).show();
            if (tempQrData.equals(MATCH_QR)) {
//                Toast.makeText(mBluetoothLeService, "Accepted", Toast.LENGTH_SHORT).show();
                str = "Unlock";
            } else {
                Toast.makeText(mBluetoothLeService, "QR not matched", Toast.LENGTH_SHORT).show();
                return;
            }


//            myTask1 = new MyTask();
//            myTask1.execute("http://61.12.38.210/Thingworx/Things/BIGBASKET1/Services/OTP");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_otp);

//        btnScan = findViewById(R.id.btn_barcode);
//        oTP = findViewById(R.id.otp);
        p_bar = findViewById(R.id.pbar);
//        setOTP = findViewById(R.id.setotp);
//        resendOTP = findViewById(R.id.resendotp);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);

        lockSeat = findViewById(R.id.lockBackSeat);
        unlockSeat = findViewById(R.id.unlockBackSeat);

        final Intent intent = getIntent();

        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

//        btnScan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivityForResult(new Intent(GenerateOTP.this,BarcodeScan.class),REQUEST_QRCODE);
//            }
//        });
//        oTP.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                myTask1 = new MyTask();
//                myTask1.execute("http://61.12.38.210/Thingworx/Things/BIGBASKET1/Services/OTP");
//            }
//        });
//        resendOTP.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                myTask1 = new MyTask();
//                myTask1.execute("http://61.12.38.210/Thingworx/Things/BIGBASKET1/Services/OTP");
//            }
//        });

        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(GenerateOTP.this, "entered", Toast.LENGTH_SHORT).show();
                if (str != null) {
                    makeChange("Unlock");
                }
//                else
//                    Toast.makeText(mBluetoothLeService, "No OTP", Toast.LENGTH_SHORT).show();
            }
        });
        unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(GenerateOTP.this, "entered", Toast.LENGTH_SHORT).show();
//                if (str != null && str.equals("unlock")) {
//                    str = "lock";
                    makeChange("lock");
//                } else
//                    Toast.makeText(mBluetoothLeService, "No UOTP", Toast.LENGTH_SHORT).show();
            }
        });
        lockSeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(GenerateOTP.this, "entered", Toast.LENGTH_SHORT).show();
                if (str != null) {
                    makeChange2("backsheetunlock");
                }
//                else
//                    Toast.makeText(mBluetoothLeService, "No OTP", Toast.LENGTH_SHORT).show();
            }
        });
        unlockSeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(GenerateOTP.this, "entered", Toast.LENGTH_SHORT).show();
//                if (str != null && str.equals("unlock")) {
                    str = "lock";
                    makeChange2("backsheetlock");
//                } else
//                    Toast.makeText(mBluetoothLeService, "No UOTP", Toast.LENGTH_SHORT).show();
            }
        });


        Intent gattServiceIntent = new Intent(GenerateOTP.this, Bluetoothservice.class);
        bindService(gattServiceIntent, mServiceConnection,BIND_AUTO_CREATE);

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        ph_uuid = telephonyManager.getDeviceId().trim();

        startActivityForResult(new Intent(GenerateOTP.this, BarcodeScan.class), REQUEST_QRCODE);

    }

    public class MyTask extends AsyncTask<String, Void, String> {
        URL myurl;
        HttpURLConnection connection;
        OutputStream outputStream;
        OutputStreamWriter writer;

        @Override
        protected String doInBackground(String... strings) {
            try {
                myurl = new URL(strings[0]);
                connection = (HttpURLConnection) myurl.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("appKey", "d0007179-396c-4dba-a288-bd951b206471");
                connection.setRequestProperty("Content-Type", "application/json");
                outputStream = connection.getOutputStream();
                writer = new OutputStreamWriter(outputStream);
                JSONObject object = new JSONObject();
                object.accumulate("value", ph_uuid);
                object.accumulate("qrData", tempQrData);
                Log.d("JSON", object.toString());

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(GenerateOTP.this, ph_uuid, Toast.LENGTH_LONG).show();
//                    }
//                });

                Log.d("IMEI : ", "" + ph_uuid + "\n");
                writer.write(object.toString());
                writer.flush();
                Log.d("check", "success");

                int response = connection.getResponseCode();

                return null;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d("check", e.toString());
                return e.toString();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("check", e.toString());
                return e.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("check", e.toString());
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            try {
//                Toast.makeText(GenerateOTP.this, data, Toast.LENGTH_SHORT).show();
                p_bar.setVisibility(View.VISIBLE);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myTask2 = new MyTask2();
                        myTask2.execute("http://61.12.38.210/Thingworx/Things/BIGBASKET1/Properties/BAG3");
                    }
                }, 10000);
            } catch (Throwable e) {
                e.printStackTrace();
                Log.d("sus", e.toString());
            }
        }
    }


    public class MyTask2 extends AsyncTask<String, Void, String> {
        URL url;
        HttpURLConnection connection;
        InputStream inputstream;
        InputStreamReader inputstreamreader;
        BufferedReader bufferreader;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            Toast.makeText(GenerateOTP.this, "start", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept", "application/json");
                Log.d("check", "success");

                connection.setRequestProperty("appKey", "d0007179-396c-4dba-a288-bd951b206471");
                Log.d("check", "success");

                connection.setRequestProperty("Content-Type", "application/json");
                Log.d("check", "success");

                inputstream = connection.getInputStream();

                Log.d("check", "success");
                inputstreamreader = new InputStreamReader(inputstream);
                bufferreader = new BufferedReader(inputstreamreader);
                StringBuilder sb = new StringBuilder();
                String str = bufferreader.readLine();

                while (str != null) {
                    sb.append(str);
                    str = bufferreader.readLine();
                }

                return sb.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d("check", e.toString());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("check", e.toString());

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("B39", "some other exception : " + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject obj = new JSONObject(s);
                JSONArray arr = obj.getJSONArray("rows");
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject k = arr.getJSONObject(i);
                    str = k.getString("BAG3");
//                    Toast.makeText(GenerateOTP.this, str, Toast.LENGTH_LONG).show();
//                    setOTP.setText(str);
                    p_bar.setVisibility(View.INVISIBLE);
//                    Toast.makeText(GenerateOTP.this, "OTP will be valid for next 5mins", Toast.LENGTH_SHORT).show();
                }
                Log.d(" str:", "" + str);

                if (str.equalsIgnoreCase("Access Denied")) {
//                    Toast.makeText(GenerateOTP.this, "Denied", Toast.LENGTH_SHORT).show();
                    p_bar.setVisibility(View.INVISIBLE);
                    return;
                } else {
//                    makeChange();
                    // MOVE TO NEXT PAGE
//                    Intent in = new Intent(GenerateOTP.this,DevicecontrolActivity.class);
//                    in.putExtra("OTP",str);
//                    startActivity(in);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onResume() {
        super.onResume();
        try {
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            if (mBluetoothLeService != null) {
                final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                Log.v("check", "Connect request result=" + result);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.d("sus", e.toString());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mGattUpdateReceiver);
        } catch (Throwable e) {
            e.printStackTrace();
            Log.d("sus", e.toString());
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            Toast.makeText(mBluetoothLeService, "onDestroy", Toast.LENGTH_SHORT).show();
            unbindService(mServiceConnection);

            mBluetoothLeService = null;
        } catch (Throwable e) {
            e.printStackTrace();
            Log.d("sus", e.toString());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            Log.i("Sw", "Services not availabele");
            return;
        }
        uuid = null;
        String unknownServiceString = "Unknown Service";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();


        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            try {
                HashMap<String, String> currentServiceData = new HashMap<String, String>();
                uuid = gattService.getUuid().toString();
                Log.i("Sw ", "UUID: " + uuid);
                currentServiceData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));

                // If the service exists for HM 10 Serial, say so.
                if (SampleGattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial") {
                } else {
                }
                currentServiceData.put(LIST_UUID, uuid);
                gattServiceData.add(currentServiceData);

                // get characteristic when UUID matches RX/TX UUID
                characteristicTX = gattService.getCharacteristic(Bluetoothservice.UUID_HM_RX_TX);
                characteristicRX = gattService.getCharacteristic(Bluetoothservice.UUID_HM_RX_TX);

                Log.i("Sw", characteristicTX == null ? "Itsnull" : "wsbhd");
                Log.i("Sw", "About to call make change");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("check", e.toString());
            } catch (Throwable e) {
                e.printStackTrace();
                Log.d("check", e.toString());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void makeChange(String msg) {
        final byte[] tx = msg.getBytes();
        Log.d("sus", mConnected + "");
//        Toast.makeText(mBluetoothLeService, msg, Toast.LENGTH_SHORT).show();
        if (mConnected) {
            try {
                Log.i("Sw", "mConnected");
                characteristicTX.setValue(tx);
                mBluetoothLeService.writeCharacteristic(characteristicTX);
                mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
                characteristicRX.getValue();
                p_bar.setVisibility(View.INVISIBLE);
                mBluetoothLeService.readCharacteristic(characteristicRX);
                if (msg.equals("Unlock")) {
                    lock.setVisibility(View.INVISIBLE);
                    unlock.setVisibility(View.VISIBLE);
                }else {
                    unlock.setVisibility(View.INVISIBLE);
                    lock.setVisibility(View.VISIBLE);
                }
//                Toast.makeText(this, str, Toast.LENGTH_LONG).show();
                Log.d("sus", Arrays.toString(tx));
            } catch (Throwable e) {
                e.printStackTrace();
                Log.d("check", e.toString());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void makeChange2( String msg) {
        final byte[] tx = msg.getBytes();
//        Toast.makeText(mBluetoothLeService, msg, Toast.LENGTH_SHORT).show();
        Log.d("sus", mConnected + "");
        if (mConnected) {
            try {
                Log.i("Sw", "mConnected");
                characteristicTX.setValue(tx);
                mBluetoothLeService.writeCharacteristic(characteristicTX);
                mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
                characteristicRX.getValue();
                p_bar.setVisibility(View.INVISIBLE);
                mBluetoothLeService.readCharacteristic(characteristicRX);
                if (msg.equals("backsheetunlock")) {
                    lockSeat.setVisibility(View.INVISIBLE);
                    unlockSeat.setVisibility(View.VISIBLE);
                }else {
                    unlockSeat.setVisibility(View.INVISIBLE);
                    lockSeat.setVisibility(View.VISIBLE);
                }
//                Toast.makeText(this, str, Toast.LENGTH_LONG).show();
                Log.d("sus", Arrays.toString(tx));
            } catch (Throwable e) {
                e.printStackTrace();
                Log.d("check", e.toString());
            }
        }
    }

    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Bluetoothservice.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Bluetoothservice.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(Bluetoothservice.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(Bluetoothservice.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null) {
                if (info.isConnected() == true) ;
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(mBluetoothLeService, "trying to disconnect", Toast.LENGTH_SHORT).show();
        mBluetoothLeService.disconnect();
        super.onBackPressed();
    }
}
