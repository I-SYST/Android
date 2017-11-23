package com.i_syst.sensortagdemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.Manifest;
import android.content.Context;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends Activity {

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private BluetoothLeScanner mLEScanner;
    //private DeviceListAdapter mAdapter;
    private TextView mTempLabel;
    private TextView mHumiLabel;
    private TextView mPressLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    // Example of a call to a native method
    //TextView tv = (TextView) findViewById(R.id.sample_text);
    //tv.setText(stringFromJNI());
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        //mTextView = (TextView) findViewById(R.id.text_view);
        mTempLabel = (TextView) findViewById(R.id.tempLabel);
        mHumiLabel = (TextView) findViewById(R.id.humiLabel);
        mPressLabel = (TextView) findViewById(R.id.pressLabel);

        mBluetoothAdapter = bluetoothManager.getAdapter();
        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mLEScanner.startScan(mScanCallback);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice device = result.getDevice();
            ScanRecord scanRecord = result.getScanRecord();
            byte[] scanData = scanRecord.getBytes();
            String name = scanRecord.getDeviceName();
            long deviceID = 0;
            byte[] manuf = scanRecord.getManufacturerSpecificData(0x0177);

            if (manuf == null)
                return;

            if (manuf[0] == 1) {

                double press = (double)(ByteBuffer.wrap(manuf, 1, 4).order(ByteOrder.LITTLE_ENDIAN).getInt()) / 1000.0;
                double temp = (double)(ByteBuffer.wrap(manuf, 5, 2).order(ByteOrder.LITTLE_ENDIAN).getShort()) / 100.0;
                double humi = (double)(ByteBuffer.wrap(manuf, 7, 2).order(ByteOrder.LITTLE_ENDIAN).getShort()) / 100.0;

                String s = String.format("%.2f", press);
                mPressLabel.setText(s);
                mPressLabel.getRootView().postInvalidate();

                s = String.format("%.2f", temp);
                mTempLabel.setText(s);
                mTempLabel.getRootView().postInvalidate();

                s = String.format("%.0f", humi);
                mHumiLabel.setText(s);
                mHumiLabel.getRootView().postInvalidate();
            }

        }
    };
}
