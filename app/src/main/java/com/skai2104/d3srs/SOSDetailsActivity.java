package com.skai2104.d3srs;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SOSDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private TextView mNameTV, mLocationTV, mDateTimeTV;
    private MapView mMapView;

    private double mLatitude = 0.0, mLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sosdetails);

        mNameTV = findViewById(R.id.nameTV);
        mLocationTV = findViewById(R.id.locationTV);
        mDateTimeTV = findViewById(R.id.dateTimeTV);
        mMapView = findViewById(R.id.mapView);

        String dataMessage = getIntent().getStringExtra("message");
        String dataFrom = getIntent().getStringExtra("from_user");
        String latitudeStr = getIntent().getStringExtra("latitude");
        String longitudeStr = getIntent().getStringExtra("longitude");
        String dateTime = getIntent().getStringExtra("datetime");

        if (latitudeStr != null)
            mLatitude = Double.valueOf(latitudeStr);

        if (longitudeStr != null)
            mLongitude = Double.valueOf(longitudeStr);

        // Todo: delete
        /*mLatitude = 5.362608;
        mLongitude = 100.446980;*/

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList = new ArrayList<>();
        String address = "";
        try {
            addressList = geocoder.getFromLocation(mLatitude, mLongitude, 1);
            if (addressList != null) {
                Address returnedAddress = addressList.get(0);
                StringBuilder returnedAddressStr = new StringBuilder();

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    returnedAddressStr.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                address = returnedAddressStr.toString();
            } else {
                Log.d("Current address error", "No address returned");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("GeocoderException", e.getMessage());
        }

        mNameTV.setText(dataFrom);
        mLocationTV.setText(address);
        mDateTimeTV.setText(dateTime);
        mLocationTV.setMovementMethod(new ScrollingMovementMethod());

        initGoogleMap(savedInstanceState);

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng location = new LatLng(mLatitude, mLongitude);
        float zoomLevel = 15.0f;

        map.addMarker(new MarkerOptions().position(location).title("Marker"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
