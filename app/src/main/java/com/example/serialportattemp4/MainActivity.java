package com.example.serialportattemp4;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";
    private static final String ACTION_USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String TAG = "MAIN";

    private UsbManager usbManager;
    private UsbSerialPort port;
    private UsbSerialDriver driver;
    private UsbDeviceConnection con;
    private ArrayList<UsbSerialDriver> drivers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            MyHttpServer server = new MyHttpServer();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_GRANT_USB.equals(intent.getAction())) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        tryOpenDevice();
                    }
                }
            }
        };

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        drivers = (ArrayList<UsbSerialDriver>) UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
        if (drivers == null || drivers.isEmpty()) {
            Log.d(TAG, "Drivers is null or empty");
            return;
        }
        driver = drivers.get(0);
        tryOpenDevice();

        Button sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(view -> {
            if (port == null || !port.isOpen()) {
                Log.d(TAG, "Port is null or is not opened");
            }
            try {
                port.write("00000000000".getBytes(), 1000);
            } catch (IOException ioe) {
                Log.d(TAG, ioe.getMessage());
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
            drivers = (ArrayList<UsbSerialDriver>) UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
            if (drivers == null || drivers.isEmpty()) {
                Log.d(TAG, "Drivers is null or empty");
                return;
            }
            driver = drivers.get(0);
            tryOpenDevice();
        }
        super.onNewIntent(intent);
    }

    private void tryOpenDevice() {
        con = usbManager.openDevice(driver.getDevice());
        if (con == null) {
            if (!usbManager.hasPermission(driver.getDevice())) {
                PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_GRANT_USB), 0);
                usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            }
            return;
        }
        port = driver.getPorts().get(0);
        try {
            port.open(con);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException ioe) {
            Log.d(TAG, ioe.getMessage());
        }
        Toast.makeText(this, "Port opened", Toast.LENGTH_SHORT).show();
    }
}