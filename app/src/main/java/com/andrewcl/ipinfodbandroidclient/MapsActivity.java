package com.andrewcl.ipinfodbandroidclient;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private final int MAP_PADDING = 20;

    private static final String IP_AGNOSTIC_API_URL = "http://ipinfodb.andrewcl.com/api/GET/";
    private static final String STATUS_CODE_VALID = "OK";
    private static final String RESPONSE_KEY_STATUS = "statusCode";
    private static final String RESPONSE_KEY_LATITUDE = "latitude";
    private static final String RESPONSE_KEY_LONGITUDE = "longitude";

    private GoogleMap mMap;

    private Map<LatLng, Marker> drawnMarkersMap = new HashMap<>();
    private List<LatLng> queuedCoordinatesArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker to TransLoc headquarters and move the camera
        LatLng transloc = new LatLng(35.875208, -78.840620);
        mMap.addMarker(new MarkerOptions().position(transloc).title("Marker at TransLoc"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(transloc));

        CameraPosition cameraPosition = new CameraPosition.Builder().target(transloc).zoom(12).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        new AccessIPAddressMetadata().execute("98.26.47.74");
    }

    private void showAllMarkers() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker mark : this.drawnMarkersMap.values()) {
            builder.include(mark.getPosition());
        }

        LatLngBounds latLngBounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, MAP_PADDING);
        mMap.moveCamera(cameraUpdate); //TODO: determine if animate better visually
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
                queuedCoordinatesArray.add(coordinates);

                //NOTE: testing code only. Should only be used for
                mMap.addMarker(new MarkerOptions().position(coordinates).title("New Marker"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(coordinates).zoom(12).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }
}
