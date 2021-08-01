/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Formatter;
import java.util.Locale;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity implements LocationListener {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mWindSpeedField;
    private TextView mGroundSpeedField;
    private float mWindSpeed = 0.0f;
    private float mGroundSpeed = 0.0f;
    private TextView mSampleCountField;
    private int mSampleCount = 0;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private boolean mPauseDataCollection = false;
    private StringBuffer mCsvData = new StringBuffer();

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                byte[] byteArray = intent.getByteArrayExtra("characteristic_16_BIT");
                byte[] windSpeedBytes = {0x00, 0x00};
                if (byteArray == null || byteArray.length != 12) {
                    return;
                }

                windSpeedBytes[0] = byteArray[1];
                windSpeedBytes[1] = byteArray[0];  // big to little endian
                ByteBuffer wrapped = ByteBuffer.wrap(windSpeedBytes);
                short rawWindSpeed = wrapped.getShort();
                float windSpeedMpS = (float)rawWindSpeed / 1000.0f;
                setWindSpeedMpH(windSpeedMpS * 2.237f);
            }
        }
    };

    private void clearUI() {
        mWindSpeedField.setText(R.string.no_data);
        mGroundSpeedField.setText(R.string.no_data);
        mSampleCountField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        String mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        ((TextView) findViewById(R.id.device_name)).setText(mDeviceName);
        mConnectionState = findViewById(R.id.connection_state);
        mWindSpeedField = findViewById(R.id.wind_speed_value);
        mGroundSpeedField = findViewById(R.id.ground_speed_value);
        mSampleCountField = findViewById(R.id.sample_count_value);

        if (getActionBar() != null) {
            getActionBar().setTitle(mDeviceName);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0f, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void setWindSpeedMpH(float windSpeedMpH) {
        if (mPauseDataCollection) {
            return;
        }
        mWindSpeed = windSpeedMpH;
        mWindSpeedField.setText(String.format("%4.1f", windSpeedMpH));
        mSampleCountField.setText(Integer.toString(++mSampleCount));
        addCsvDataPoint();
    }

    public void onClearClickBtn(View view) {
        DeviceControlActivity activity = (DeviceControlActivity)view.getContext();
        activity.clearCounts();
    }

    public void onPauseClickBtn(View view) {
        DeviceControlActivity activity = (DeviceControlActivity)view.getContext();
        activity.togglePause();
    }

    public void onEmailClickBtn(View view) {
        DeviceControlActivity activity = (DeviceControlActivity)view.getContext();
        activity.emailData();
    }

    private void clearCounts() {
        mSampleCount = 0;
        mSampleCountField.setText(R.string.no_data);
        mWindSpeedField.setText(R.string.no_data);
        mGroundSpeedField.setText(R.string.no_data);
        mCsvData = new StringBuffer();
    }

    private void togglePause() {
        mPauseDataCollection = !mPauseDataCollection;
    }

    public void setConnected(boolean connected) {
        if (connected) {
            mConnected = true;
            updateConnectionState(R.string.connected);
            invalidateOptionsMenu();
        } else {
            mConnected = false;
            updateConnectionState(R.string.disconnected);
            invalidateOptionsMenu();
            clearUI();
        }
    }

    private void emailData() {
        Log.i("Send email", "");

        String[] TO = {"john.zehetner@gmail.com"};
        String[] CC = {};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Automated Anemometer Calibration Data");
        emailIntent.putExtra(Intent.EXTRA_TEXT, mCsvData.toString());

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i("Finished sending email", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(DeviceControlActivity.this,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addCsvDataPoint() {
        String dataPoint = System.currentTimeMillis() + "," + mWindSpeed + "," + mGroundSpeed + "\n";
        mCsvData.append(dataPoint);
    }


    @Override
    public void onLocationChanged(Location location) {
        if (mPauseDataCollection) {
            return;
        }

        if(location != null)
        {
            mGroundSpeed = convertMpsToMph(location.getSpeed());

            Formatter fmt = new Formatter(new StringBuilder());
            fmt.format(Locale.US, "%4.1f", mGroundSpeed);
            String strCurrentSpeed = fmt.toString();

            mGroundSpeedField.setText(strCurrentSpeed);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    private float convertMpsToMph(float speedMps) {
        return speedMps * 2.237f;
    }
}
