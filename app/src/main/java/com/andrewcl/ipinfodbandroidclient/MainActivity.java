package com.andrewcl.ipinfodbandroidclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    public static final String INTENT_PARAMATER_KEY_IP_ADDRESS = "ip_addresses";

    private EditText mStartEditText;
    private EditText mStopEditText;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStartEditText = (EditText) findViewById(R.id.edit_text_range_start);
        mStopEditText = (EditText) findViewById(R.id.edit_text_range_stop);

        mButton = (Button) findViewById(R.id.button_launch_maps_activity);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ArrayList<String> dummyData = new ArrayList<>();
//                dummyData.add("98.26.47.74");
//                dummyData.add("173.236.244.234");
//                dummyData.add("68.180.194.242");

                //Validate EditText text is valid ip address
                Boolean startTextValid = Patterns.IP_ADDRESS.matcher(mStartEditText.getText()).find() && mStartEditText.getText().length() > 0;
                Boolean stopTextValid = Patterns.IP_ADDRESS.matcher(mStopEditText.getText()).find() && mStopEditText.getText().length() > 0;

                if (startTextValid && stopTextValid) {
                    String startIPAddressString = mStartEditText.getText().toString();
                    String stopIPAddressString = mStopEditText.getText().toString();
                    ArrayList<String> ipAddressArray = stringArrayFromRange(startIPAddressString, stopIPAddressString);

                    Intent launchMapsIntent = new Intent(MainActivity.this, MapsActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(INTENT_PARAMATER_KEY_IP_ADDRESS, ipAddressArray);
                    launchMapsIntent.putExtras(bundle);
                    MainActivity.this.startActivity(launchMapsIntent);
                } else {
//                    Toast warningToast = new Toast(MainActivity.this);
//                    warningToast.makeText(MainActivity.this, "ip address must match pattern!!!", Toast.LENGTH_LONG);
//                    warningToast.show();
                    Toast.makeText(getApplicationContext(), "ip address must match pattern!!!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private ArrayList<String> stringArrayFromRange(String startIPAddressRange, String stopIPAddressRange) {
        int startInteger = convertIPAddressStringToInt(startIPAddressRange);
        int stopInteger = convertIPAddressStringToInt(stopIPAddressRange);

        //Switches order in case user has reversed ip address range fields
        if (startInteger < stopInteger) {
            int newStopInt = startInteger;
            startInteger = stopInteger;
            stopInteger = newStopInt;
        }

        ArrayList<String> ipAddressesArray = new ArrayList<>();
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
