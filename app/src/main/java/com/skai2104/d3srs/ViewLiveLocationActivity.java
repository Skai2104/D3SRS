package com.skai2104.d3srs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

    private TextView mLatitudeTV, mLongitudeTV;
    private Button mStartBtn, mStopBtn;
    private MapView mMapView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mFirebaseUser;
    private ListenerRegistration mRegistration;

    private String mCurrentUserId, mDocId;
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
        mStartBtn = findViewById(R.id.startBtn);
        mStopBtn = findViewById(R.id.stopBtn);
        mMapView = findViewById(R.id.mapView);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mCurrentUserId = mAuth.getUid();

        initGoogleMap(savedInstanceState);

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartBtn.setEnabled(false);
                mStopBtn.setEnabled(true);
                getLocationUpdate();
            }
        });

        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartBtn.setEnabled(true);
                mStopBtn.setEnabled(false);
                mRegistration.remove();
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
                        mDocId = doc.getDocument().getId();

                        if (mDocId.equals("oNLHdC4DHF9jA5DmOx8R")) {
                            if (doc.getType() == DocumentChange.Type.MODIFIED) {
                                mLatitude = doc.getDocument().getDouble("latitude");
                                mLongitude = doc.getDocument().getDouble("longitude");

                                mLatitudeTV.setText(String.valueOf(mLatitude));
                                mLongitudeTV.setText(String.valueOf(mLongitude));

                                refreshMap(mMap);
                                markStartingLocationOnMap(mMap, new LatLng(mLatitude, mLongitude));
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
        float zoomLevel = 15.0f;

        map.addMarker(new MarkerOptions().position(location).title("The location of him/her"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));

    }
}
