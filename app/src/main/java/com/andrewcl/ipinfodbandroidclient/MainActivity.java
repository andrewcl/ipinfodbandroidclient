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
import android.widget.ToggleButton;

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

    public static final String INTENT_PARAMETER_KEY_ADDRESS_START = "address_start";
    public static final String INTENT_PARAMETER_KEY_ADDRESS_STOP = "address_stop";
    public static final String INTENT_PARAMETER_KEY_FINE_SEARCH = "fine_grain_search";

    private EditText mStartEditText;
    private EditText mStopEditText;
    private ToggleButton mToggleButton;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStartEditText = (EditText) findViewById(R.id.edit_text_range_start);
        mStopEditText = (EditText) findViewById(R.id.edit_text_range_stop);

        mToggleButton = (ToggleButton) findViewById(R.id.toggle_button_fine_grain);

        mButton = (Button) findViewById(R.id.button_launch_maps_activity);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Validate EditText text is valid ip address
                Boolean startTextValid = Patterns.IP_ADDRESS.matcher(mStartEditText.getText()).find() && mStartEditText.getText().length() > 0;
                Boolean stopTextValid = Patterns.IP_ADDRESS.matcher(mStopEditText.getText()).find() && mStopEditText.getText().length() > 0;

                if (startTextValid && stopTextValid) {
                    String startIPAddressString = mStartEditText.getText().toString();
                    String stopIPAddressString = mStopEditText.getText().toString();
                    Boolean enableFineSearch = mToggleButton.isChecked();

                    Bundle bundle = new Bundle();
                    bundle.putString(INTENT_PARAMETER_KEY_ADDRESS_START, startIPAddressString);
                    bundle.putString(INTENT_PARAMETER_KEY_ADDRESS_STOP, stopIPAddressString);
                    bundle.putBoolean(INTENT_PARAMETER_KEY_FINE_SEARCH, enableFineSearch);

                    Intent launchMapsIntent = new Intent(MainActivity.this, MapsActivity.class);
                    launchMapsIntent.putExtras(bundle);
                    MainActivity.this.startActivity(launchMapsIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "ip address must match pattern!!!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
