package com.skai2104.d3srs;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.GeoApiContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatusDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String TAG = "Status Details Activity";

    private TextView mStatusTV, mLocationTV, mDateTimeTV;
    private Toolbar mToolbar;
    private MapView mMapView;
    private ProgressBar mProgressBar;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    private double mLatitude = 0.0, mLongitude = 0.0;
    private String mName, mUserId, mRequesterName, mRequesterId;

    private GeoApiContext mGeoApiContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_details);

        mStatusTV = findViewById(R.id.statusTV);
        mLocationTV = findViewById(R.id.locationTV);
        mDateTimeTV = findViewById(R.id.dateTimeTV);
        mMapView = findViewById(R.id.mapView);
        mProgressBar = findViewById(R.id.progressBar);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mName = getIntent().getStringExtra("from_user");
        mUserId = getIntent().getStringExtra("from_user_id");
        String status = getIntent().getStringExtra("status");
        String latitudeStr = getIntent().getStringExtra("latitude");
        String longitudeStr = getIntent().getStringExtra("longitude");
        String datetime = getIntent().getStringExtra("datetime");

        getSupportActionBar().setTitle(mName);

        mProgressBar.setVisibility(View.GONE);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mRequesterId = mAuth.getCurrentUser().getUid();

        if (latitudeStr != null)
            if (!latitudeStr.isEmpty())
                mLatitude = Double.valueOf(latitudeStr);

        if (longitudeStr != null)
            if (!longitudeStr.isEmpty())
                mLongitude = Double.valueOf(longitudeStr);

        String color = "#B0C4DE";
        switch (status) {
            case "Unknown":
                color = "#B0C4DE";
                break;

            case "Safe":
                color = "#32CD32";
                break;

            case "Waiting for help":
                color = "#FF8C00";
                break;
        }

        Geocoder geocoder = new Geocoder(StatusDetailsActivity.this, Locale.getDefault());
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
                address = "Location not available.";
                Log.d("Current address error", "No address returned");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("GeocoderException", e.getMessage());
        }

        if (datetime == null) {
            datetime = "N/A";
        }

        mStatusTV.setText(status);
        mStatusTV.setBackgroundColor(Color.parseColor(color));
        mLocationTV.setText(address);
        mDateTimeTV.setText(datetime);
        mLocationTV.setMovementMethod(new ScrollingMovementMethod());

        initGoogleMap(savedInstanceState);

        findViewById(R.id.openInMapsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInGoogleMaps();
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

        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();

        mFirestore.collection("Users").document(mRequesterId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        mRequesterName = documentSnapshot.getString("name");
                    }
                });
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

        map.addMarker(new MarkerOptions().position(location).title("The location of " + mName));
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
        getMenuInflater().inflate(R.menu.status_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.requestLlBtn:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Request Live Location")
                        .setMessage("Request the live location from " + mName + "?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mProgressBar.setVisibility(View.VISIBLE);

                                sendLiveLocationRequest();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendLiveLocationRequest() {
        Map<String, Object> sendRequestMap = new HashMap<>();
        sendRequestMap.put("name", mRequesterName);
        sendRequestMap.put("userId", mRequesterId);

        mFirestore.collection("Users").document(mUserId)
                .collection("LiveLocationRequests")
                .add(sendRequestMap)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Intent i = new Intent(StatusDetailsActivity.this, ViewLiveLocationActivity.class);
                            i.putExtra("name", mName);
                            i.putExtra("userId", mUserId);
                            startActivity(i);
                        }
                    }
                });
    }

    public void openInGoogleMaps() {
        Uri gmmIntentUri = Uri.parse("geo:" + mLatitude + "," + mLongitude + "?q=" + mLatitude + "," + mLongitude + "(The location of " + mName + ")");
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
