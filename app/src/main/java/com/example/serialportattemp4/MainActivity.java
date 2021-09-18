package com.example.serialportattemp4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(view -> {
            UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            ArrayList<UsbSerialDriver> drivers = (ArrayList<UsbSerialDriver>) UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
            if (drivers == null || drivers.isEmpty()) {
                Toast.makeText(MainActivity.this, "drivers is null or empty", Toast.LENGTH_LONG).show();
                return;
            }
            UsbSerialDriver driver = drivers.get(0);
            UsbDeviceConnection con = usbManager.openDevice(driver.getDevice());
            if (con == null) {
                Toast.makeText(MainActivity.this, "Con is null", Toast.LENGTH_SHORT).show();
                if (usbManager.hasPermission(driver.getDevice())) {
                    Toast.makeText(this, "HAS PERMISSION", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "NO PERMISSION :( :(", Toast.LENGTH_SHORT).show();
                    PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(BuildConfig.APPLICATION_ID + ".GRANT_USB"), 0);
                    usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
                }
                return;
            }
            UsbSerialPort port = driver.getPorts().get(0);
            if (port == null) {
                Toast.makeText(MainActivity.this, "Port is null", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                port.open(con);
                port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                port.write("00000000000".getBytes(), 1000);
            } catch (IOException ioe) {
                Toast.makeText(MainActivity.this, "IOE", Toast.LENGTH_SHORT).show();
            }
        });
    }
}