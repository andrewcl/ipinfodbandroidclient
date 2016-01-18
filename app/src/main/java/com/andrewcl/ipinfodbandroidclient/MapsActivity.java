package com.andrewcl.ipinfodbandroidclient;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private final int MAP_PADDING = 60;

    private static final String IP_AGNOSTIC_API_URL = "http://ipinfodb.andrewcl.com/api/GET";
    private static final String STATUS_CODE_VALID = "OK";
    private static final String RESPONSE_KEY_STATUS = "statusCode";
    private static final String RESPONSE_KEY_LATITUDE = "latitude";
    private static final String RESPONSE_KEY_LONGITUDE = "longitude";

    private GoogleMap mMap;
    private Boolean mMapHasLoaded = false;

    private Map<LatLng, Marker> mDrawnMarkersMap = new HashMap<>();
    private Queue<LatLng> mCoordinatesQueue = new LinkedList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent passedIntent = getIntent();
        Bundle passedBundle = passedIntent.getExtras();

        if (passedBundle != null && passedBundle.getStringArrayList(MainActivity.INTENT_PARAMATER_KEY_IP_ADDRESS) != null) {
            ArrayList<String> ipAddresArrayList = passedBundle.getStringArrayList(MainActivity.INTENT_PARAMATER_KEY_IP_ADDRESS);

            //not ideal, but syntax for converting b/w ArrayList <-> List is fuzzy at best
            List<String> ipAddressList = new ArrayList<>();
            for (String ipAddressString : ipAddresArrayList) {
                ipAddressList.add(ipAddressString);
            }

            queueIPAddressForDownload(ipAddressList);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                //Process backlog queue of coordinates that have not yet been added to mMap
                mMapHasLoaded = true;
                processCoordinateBacklog();
            }
        });

        LatLng loadingCoordinates = new LatLng(0, 0);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loadingCoordinates));
    }

    public void queueIPAddressForDownload(List<String> ipAddressArray) {
        for (String ipAddress : ipAddressArray) {
            new AccessIPAddressMetadata().execute(ipAddress);
        }
    }

    //MARK - Marker Helpers
    private void showAllMarkers() {
        //Safety check in case layout has not occurred for mMap
        if (!mMapHasLoaded) {
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker mark : this.mDrawnMarkersMap.values()) {
            builder.include(mark.getPosition());
        }

        //TODO: dynamically determine marker size. Use to set padding
        LatLngBounds latLngBounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, MAP_PADDING);
        mMap.animateCamera(cameraUpdate);
    }

    //NOTE: prevents overdraw by comparing new coordinate against HashMap of already drawn coordinates
    private Boolean addMarkerToMap(LatLng coordinates) {
        if (!mMapHasLoaded) {
            return false;
        }

        if (!mDrawnMarkersMap.containsKey(coordinates)) {
            Marker marker = mMap.addMarker(new MarkerOptions().position(coordinates).title("Marker at TransLoc"));
            mDrawnMarkersMap.put(coordinates, marker);
            return true;
        }
        return false;
    }

    /*
     * Adds single marker and refreshes if marker has not been added to map
     */
    private void addMarkerToMapAndRefresh(LatLng coordinates) {
        if (!mMapHasLoaded) {
            return;
        }

        Boolean successfulMarkerAdd = addMarkerToMap(coordinates);
        if (successfulMarkerAdd) {
            showAllMarkers();
        }
    }

    private void clearMarkersFromMap() {
        mDrawnMarkersMap = new HashMap<>();
        mCoordinatesQueue.clear();
        mMap.clear();
    }

    /*
     * iterate through backlog of LatLng. Process and add in batch before triggering map refresh
     */
    private void processCoordinateBacklog()
    {
        if (mCoordinatesQueue.isEmpty() || !mMapHasLoaded) {
            return;
        }

        Boolean triggerRefresh = false;
        while (!mCoordinatesQueue.isEmpty()) {
            LatLng coordinate = mCoordinatesQueue.poll();
            Boolean successfulMarkerAdd = addMarkerToMap(coordinate);
            triggerRefresh = successfulMarkerAdd ? true : triggerRefresh;
        }

        if (triggerRefresh) {
            showAllMarkers();
        }
    }

    //MARK - AsyncTask for API call
    private class AccessIPAddressMetadata extends AsyncTask<String, Void, Boolean> {
        private HttpURLConnection urlConnection;
        private LatLng coordinates;

        protected Boolean doInBackground(String... ipAddressString) {
            StringBuilder stringBuilder = new StringBuilder();
            Boolean downloadSuccess = false;

            try {
                StringBuilder urlBuilder = new StringBuilder();
                urlBuilder.append(IP_AGNOSTIC_API_URL);
                urlBuilder.append(ipAddressString[0]); //NOTE: String... sends array, access first element

                URL url = new URL(urlBuilder.toString());
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String bufferString;
                while ((bufferString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(bufferString);
                }

                JSONObject responseObject = new JSONObject(stringBuilder.toString());
                if (responseObject.has(RESPONSE_KEY_STATUS) && responseObject.get(RESPONSE_KEY_STATUS).equals(STATUS_CODE_VALID)) {
                    Boolean hasValidLatitude = responseObject.has(RESPONSE_KEY_LATITUDE) && !responseObject.get(RESPONSE_KEY_LATITUDE).equals("");
                    Boolean hasValidLongitude = responseObject.has(RESPONSE_KEY_LONGITUDE) && !responseObject.get(RESPONSE_KEY_LONGITUDE).equals("");

                    if (hasValidLatitude && hasValidLongitude) {
                        Double latitude = responseObject.getDouble(RESPONSE_KEY_LATITUDE);
                        Double longitude = responseObject.getDouble(RESPONSE_KEY_LONGITUDE);
                        coordinates = new LatLng(latitude, longitude);
                        downloadSuccess = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

            return downloadSuccess;
        }

        protected void onPostExecute(Boolean downloadSuccess) {
            if (coordinates != null) {
                mCoordinatesQueue.add(coordinates);
                addMarkerToMapAndRefresh(coordinates);
            }
        }
    }
}
