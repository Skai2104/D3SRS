package com.skai2104.d3srs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.GeoApiContext;

import javax.annotation.Nullable;

public class ViewLiveLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private Toolbar mToolbar;
    private TextView mLatitudeTV, mLongitudeTV, mWaitingTV;
    private MapView mMapView;
    private LinearLayout mLiveLocationLayout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mFirebaseUser;
    private ListenerRegistration mRegistration;

    private String mCurrentUserId, mName, mUserId, mStatus;
    private Double mLatitude = 0.0, mLongitude = 0.0;
    private boolean mIsFound = false;

    private GoogleMap mMap;

    private GeoApiContext mGeoApiContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_live_location);

        mLatitudeTV = findViewById(R.id.latitudeTV);
        mLongitudeTV = findViewById(R.id.longitudeTV);
        mMapView = findViewById(R.id.mapView);
        mLiveLocationLayout = findViewById(R.id.liveLocationLayout);
        mWaitingTV = findViewById(R.id.waitingTV);

        mLiveLocationLayout.setVisibility(View.GONE);
        mWaitingTV.setVisibility(View.VISIBLE);

        mName = getIntent().getStringExtra("name");
        mUserId = getIntent().getStringExtra("userId");
        mWaitingTV.setText("Waiting for " + mName + " to approve your request...");

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Live Location of " + mName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mCurrentUserId = mAuth.getUid();

        initGoogleMap(savedInstanceState);

        getLocationUpdate();

        findViewById(R.id.stopBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRegistration.remove();
                finish();
            }
        });
    }

    // "oNLHdC4DHF9jA5DmOx8R"
    public void getLocationUpdate() {
        Query query = mFirestore.collection("LiveLocations");
        mRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.MODIFIED) {
                            String userId = doc.getDocument().getString("userId");
                            if (userId != null) {
                                if (userId.equals(mUserId)) {
                                    mIsFound = true;

                                    mLatitude = doc.getDocument().getDouble("latitude");
                                    mLongitude = doc.getDocument().getDouble("longitude");
                                    mStatus = doc.getDocument().getString("status");

                                    break;

                                } else {
                                    mIsFound = false;
                                }
                            }
                        }
                    }
                    if (mIsFound) {
                        if (mStatus != null) {
                            switch (mStatus) {
                                case "sharing":
                                    mLiveLocationLayout.setVisibility(View.VISIBLE);
                                    mWaitingTV.setVisibility(View.GONE);

                                    mLatitudeTV.setText(String.valueOf(mLatitude));
                                    mLongitudeTV.setText(String.valueOf(mLongitude));

                                    refreshMap(mMap);
                                    markStartingLocationOnMap(mMap, new LatLng(mLatitude, mLongitude));

                                    break;

                                case "rejected":
                                    mWaitingTV.setText(mName + " has rejected your live location request.");

                                    mLiveLocationLayout.setVisibility(View.GONE);
                                    mWaitingTV.setVisibility(View.VISIBLE);

                                    break;

                                case "stop":
                                    mWaitingTV.setText(mName + " has ended the live location sharing session with you.");

                                    mLiveLocationLayout.setVisibility(View.GONE);
                                    mWaitingTV.setVisibility(View.VISIBLE);

                                    break;
                            }
                        }
                    }
                }
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
        mMap = map;
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

    private void refreshMap(GoogleMap mapInstance) {
        mapInstance.clear();
    }

    private void markStartingLocationOnMap(GoogleMap map, LatLng location) {
        float zoomLevel = map.getMaxZoomLevel() - 2.0f;

        map.addMarker(new MarkerOptions().position(location).title("The location of him/her"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
