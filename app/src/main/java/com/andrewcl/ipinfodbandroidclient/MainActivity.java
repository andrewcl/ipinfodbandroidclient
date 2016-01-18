package com.andrewcl.ipinfodbandroidclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String INTENT_PARAMATER_KEY_IP_ADDRESS = "ip_addresses";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //NOTE: provides readonly copy of values to launched activity
                ArrayList<String> dummyData = new ArrayList<>();
                dummyData.add("98.26.47.74");
                dummyData.add("173.236.244.234");
                dummyData.add("68.180.194.242");

                Intent launchMapsIntent = new Intent(MainActivity.this, MapsActivity.class);

                Bundle bundle = new Bundle();
                bundle.putStringArrayList(INTENT_PARAMATER_KEY_IP_ADDRESS, dummyData);
                launchMapsIntent.putExtras(bundle);

                MainActivity.this.startActivity(launchMapsIntent);
            }
        });


        List<String> testStringArray = stringArrayFromRange("127.0.0.1", "127.0.0.6");
        System.out.println("YOYOYOYOY: " + Integer.toString(testStringArray.size()));
    }

    private List<String> stringArrayFromRange(String startIPAddressRange, String stopIPAddressRange) {
        int startInteger = convertIPAddressStringToInt(startIPAddressRange);
        int stopInteger = convertIPAddressStringToInt(stopIPAddressRange);

        //Switches order in case user has reversed ip address range fields
        if (startInteger < stopInteger){
            int newStopInt = startInteger;
            startInteger = stopInteger;
            stopInteger = newStopInt;
        }

        List<String> ipAddressesArray = new ArrayList<>();
        while (startInteger <= stopInteger) {
            InetAddress inetAddress = ipIntToInetAddress(startInteger);
            ipAddressesArray.add(inetAddress.toString());
            startInteger += 1;
        }
        return ipAddressesArray;
    }

    public InetAddress ipIntToInetAddress(int ipAsInt) {
        byte[] ipAsByteArray = intToByteArray(ipAsInt);
        try {
            InetAddress inetAddress = InetAddress.getByAddress(ipAsByteArray);
            return inetAddress;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] intToByteArray(int a) {
        byte[] ret = new byte[4];
        ret[3] = (byte) (a & 0xFF);
        ret[2] = (byte) ((a >> 8) & 0xFF);
        ret[1] = (byte) ((a >> 16) & 0xFF);
        ret[0] = (byte) ((a >> 24) & 0xFF);
        return ret;
    }

    public static int convertIPAddressStringToInt(String ipAddressString) {
        String[] ipAddressInArray = ipAddressString.split("\\.");
        int ipAddress = 0;
        for (int i = 0; i < ipAddressInArray.length; i++) {
            int ipComponentAsInt = Integer.parseInt(ipAddressInArray[i]);
            ipAddress += ipComponentAsInt * Math.pow(256, 3 - i);
        }
        return ipAddress;
    }
}
