package com.example.susmita.smartlock;

import android.Manifest;
import android.bluetooth.BluetoothClass;
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
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
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

public class DevicecontrolActivity extends AppCompatActivity {


    TextView dname;
    CircleImageView unlock,lock;
    MyTask myTask1;
    MyTask2 myTask2;
    byte[] res;
    private String mDeviceName;
    private String mDeviceAddress;
    ProgressBar p_bar;

    private final static String TAG = DevicecontrolActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public Bluetoothservice mBluetoothLeService;
    public boolean mConnected = false;
    public BluetoothGattCharacteristic characteristicTX;
    public BluetoothGattCharacteristic characteristicRX;
    public final static UUID HM_RX_TX =
            UUID.fromString(SampleGattAttributes.HM_RX_TX);
    String str;
    String uuid;
    String ph_uuid;

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
            }catch (Throwable e){
                e.printStackTrace();
                Log.d("sus",e.toString());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Toast.makeText(mBluetoothLeService, "onservivce disconnected", Toast.LENGTH_SHORT).show();
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
                    MainActivity.isconnected = mConnected;
                    Toast.makeText(DevicecontrolActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                } else if (Bluetoothservice.ACTION_GATT_DISCONNECTED.equals(action)) {
                    mConnected = false;
                    MainActivity.isconnected = mConnected;
                    Toast.makeText(DevicecontrolActivity.this, "DisConnected", Toast.LENGTH_SHORT).show();
                } else if (Bluetoothservice.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    Log.i("Swapnil", "reached");
                    // Show all the supported services and characteristics on the user interface.
                    displayGattServices(mBluetoothLeService.getSupportedGattServices());
                    if (characteristicTX == null) {
                        Log.i("Sw", "Its not nulllll");
                    }
                } else if (Bluetoothservice.ACTION_DATA_AVAILABLE.equals(action)) {
                }
            }catch (Throwable e){
                e.printStackTrace();
                Log.d("sus",e.toString());
            }
        }
    };


    BluetoothDevice mDevice;
    String writeMessage;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devicecontrol);

        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);

        p_bar = findViewById(R.id.pbar);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

//        Intent in = getIntent();
//        Bundle b = in.getExtras();
//        if (b!= null){
//            str = b.getString("OTP");
//        }

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        try {

//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            ph_uuid = telephonyManager.getDeviceId();

            Intent gattServiceIntent = new Intent(DevicecontrolActivity.this, Bluetoothservice.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }catch (Throwable e){
            e.printStackTrace();
            Log.d("sus",e.toString());
        }
        
        if(isNetworkAvailable()==true) {
            lock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //if(isNetworkAvailable()==true)
                    try {
                        if (!MainActivity.isuser) {
                            myTask1 = new MyTask();
                            myTask1.execute("http://111.93.153.242/Thingworx/Things/Redeem_Bigbasket/Services/deliverymessage");
                        }else {
                            makeChange();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        Log.d("sus", e.toString());
                    }
                }
            });
        }else{
            Toast.makeText(this, "NO INTERNET....TRY AGAIN", Toast.LENGTH_SHORT).show();
        }
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
                connection.setRequestProperty("appKey", "65a73f5e-3877-4881-818b-d46b89be7783");
                connection.setRequestProperty("Content-Type", "application/json");
                outputStream = connection.getOutputStream();
                writer = new OutputStreamWriter(outputStream);
                JSONObject object = new JSONObject();
                object.accumulate("name", uuid);
                object.accumulate("value", ph_uuid);

                Log.d("UUID : ", ""+uuid);
                Log.d("IMEI : ", ""+ph_uuid);
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
                Toast.makeText(mBluetoothLeService, data, Toast.LENGTH_SHORT).show();
                myTask2 = new MyTask2();
                myTask2.execute("http://111.93.153.242/Thingworx/Things/Redeem_Bigbasket/Properties/Result");
                p_bar.setVisibility(View.VISIBLE);
            }catch (Throwable e){
                e.printStackTrace();
                Log.d("sus",e.toString());
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
//            Toast.makeText(DevicecontrolActivity.this, "start", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept", "application/json");
                Log.d("check", "success");

                connection.setRequestProperty("appKey", "65a73f5e-3877-4881-818b-d46b89be7783");
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
                    str = k.getString("Result");
                  }
                p_bar.setVisibility(View.VISIBLE);
                Log.d(" str:",""+str);

                if (str.equalsIgnoreCase("Access Denied")){
                    Toast.makeText(mBluetoothLeService, "Access Denied", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    makeChange();
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
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            Log.d("sus", e.toString());
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mGattUpdateReceiver);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            Log.d("sus", e.toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            Log.d("sus", e.toString());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            Log.i("Sw","Services not availabele");
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
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.d("check", e.toString());
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                Log.d("check", e.toString());
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void makeChange() {

        
        final byte[] tx = str.getBytes();
        if(mConnected) {
            try {
                Log.i("Sw","mConnected");
                characteristicTX.setValue(tx);
                mBluetoothLeService.writeCharacteristic(characteristicTX);
                mBluetoothLeService.setCharacteristicNotification(characteristicRX,true);
                characteristicRX.getValue();
                p_bar.setVisibility(View.INVISIBLE);
                mBluetoothLeService.readCharacteristic(characteristicRX);
                lock.setVisibility(View.INVISIBLE);
                unlock.setVisibility(View.VISIBLE);

//                Toast.makeText(this, str, Toast.LENGTH_LONG).show();
                Log.d("sus", Arrays.toString(tx));
            }
            catch (Throwable e)
            {
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
                if(info.isConnected()==true);
              return true;
            }
        }
        return false;

    }


}



