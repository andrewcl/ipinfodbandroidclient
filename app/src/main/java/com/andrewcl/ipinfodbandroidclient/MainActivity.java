package com.andrewcl.ipinfodbandroidclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.lang.reflect.Array;
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
    }
}
