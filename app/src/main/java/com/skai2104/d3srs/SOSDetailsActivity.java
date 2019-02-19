package com.skai2104.d3srs;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;

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
    private static final String TAG = "SOS Details Activity";

    private Toolbar mToolbar;
    private TextView mLocationTV, mDateTimeTV;
    private MapView mMapView;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;

    private double mLatitude = 0.0, mLongitude = 0.0;
    private String mDataFrom;
    private String mCurrentUserId, mDocId;

    private GeoApiContext mGeoApiContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sosdetails);

        mLocationTV = findViewById(R.id.locationTV);
        mDateTimeTV = findViewById(R.id.dateTimeTV);
        mMapView = findViewById(R.id.mapView);

        mDataFrom = getIntent().getStringExtra("from_user");
        String latitudeStr = getIntent().getStringExtra("latitude");
        String longitudeStr = getIntent().getStringExtra("longitude");
        String dateTime = getIntent().getStringExtra("datetime");
        mDocId = getIntent().getStringExtra("docId");

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(mDataFrom);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getUid();

        if (latitudeStr != null)
            mLatitude = Double.valueOf(latitudeStr);

        if (longitudeStr != null)
            mLongitude = Double.valueOf(longitudeStr);

        // Get the full address from the latitude and longitude
        Geocoder geocoder = new Geocoder(SOSDetailsActivity.this, Locale.getDefault());
        List<Address> addressList;
        String address = "";
        try {
            addressList = geocoder.getFromLocation(mLatitude, mLongitude, 1);
            if (!addressList.isEmpty()) {
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

        mLocationTV.setText(address);
        mDateTimeTV.setText(dateTime);
        mLocationTV.setMovementMethod(new ScrollingMovementMethod());

        initGoogleMap(savedInstanceState);

        findViewById(R.id.directionBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLocation();
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

        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_api_key))
                    .build();
        }
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

        map.addMarker(new MarkerOptions().position(location).title("The location of " + mDataFrom));
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sos_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dismissBtn:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Dismiss SOS Alert")
                        .setMessage("Are you sure you want to dismiss this SOS alert?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dismissSOS();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void dismissSOS() {
        mFirestore.collection("Users").document(mCurrentUserId)
                .collection("SOSNotification").document(mDocId)
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SOSDetailsActivity.this, "SOS alert dismissed", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    public void navigateToLocation() {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + mLatitude + "," + mLongitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage() );
            Toast.makeText(this, "Couldn't open map", Toast.LENGTH_SHORT).show();
        }
    }
}
