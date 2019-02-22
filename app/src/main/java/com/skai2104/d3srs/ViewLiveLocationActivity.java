package com.skai2104.d3srs;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.GeoApiContext;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class ViewLiveLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private Toolbar mToolbar;
    private TextView mLatitudeTV, mLongitudeTV, mWaitingTV;
    private MapView mMapView;
    private LinearLayout mLiveLocationLayout, mInfoLayout;
    private Button mCancelBtn, mOkayBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mFirebaseUser;
    private ListenerRegistration mRegistration;

    private String mCurrentUserId, mName, mUserId, mStatus, mLatitudeStr, mLongitudeStr;
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
        mCancelBtn = findViewById(R.id.cancelBtn);
        mOkayBtn = findViewById(R.id.okayBtn);
        mInfoLayout = findViewById(R.id.infoLayout);

        mLiveLocationLayout.setVisibility(View.GONE);
        mInfoLayout.setVisibility(View.VISIBLE);
        mCancelBtn.setVisibility(View.VISIBLE);
        mOkayBtn.setVisibility(View.GONE);

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

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder  = new AlertDialog.Builder(ViewLiveLocationActivity.this);
                builder.setTitle("Cancel Request")
                        .setMessage("Are you sure you want to cancel the live location request?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                stopSharing("cancelled");
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });

        mOkayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRegistration.remove();
                finish();
            }
        });

        findViewById(R.id.stopBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewLiveLocationActivity.this);
                builder.setTitle("End Sharing Session")
                        .setMessage("Are you sure you want to end the live location sharing session?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                stopSharing("requesterStop");
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        });
    }

    public void getLocationUpdate() {
        Query query = mFirestore.collection("Users");
        mRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        String docId = doc.getDocument().getId();

                        if (docId.equals(mUserId)) {
                            if (doc.getType() == DocumentChange.Type.MODIFIED) {
                                mIsFound = true;

                                mLatitudeStr = doc.getDocument().getString("latitude");
                                mLongitudeStr = doc.getDocument().getString("longitude");

                                if (mLatitudeStr != null) {
                                    if (!mLatitudeStr.isEmpty()) {
                                        mLatitude = Double.valueOf(mLatitudeStr);
                                    }
                                }

                                if (mLongitudeStr != null) {
                                    if (!mLongitudeStr.isEmpty()) {
                                        mLongitude = Double.valueOf(mLongitudeStr);
                                    }
                                }
                                mStatus = doc.getDocument().getString("sharing");

                                break;
                            }
                        } else {
                            mIsFound = false;
                        }
                    }
                    if (mIsFound) {
                        if (mStatus != null) {
                            switch (mStatus) {
                                case "sharing":
                                    mLiveLocationLayout.setVisibility(View.VISIBLE);
                                    mInfoLayout.setVisibility(View.GONE);

                                    mLatitudeTV.setText(String.valueOf(mLatitude));
                                    mLongitudeTV.setText(String.valueOf(mLongitude));

                                    refreshMap(mMap);
                                    markStartingLocationOnMap(mMap, new LatLng(mLatitude, mLongitude));

                                    break;

                                case "rejected":
                                    mWaitingTV.setText(mName + " has rejected your live location request.");

                                    mLiveLocationLayout.setVisibility(View.GONE);
                                    mInfoLayout.setVisibility(View.VISIBLE);
                                    mCancelBtn.setVisibility(View.GONE);
                                    mOkayBtn.setVisibility(View.VISIBLE);

                                    break;

                                case "stop":
                                    mWaitingTV.setText(mName + " has ended the live location sharing session with you.");

                                    mLiveLocationLayout.setVisibility(View.GONE);
                                    mInfoLayout.setVisibility(View.VISIBLE);
                                    mCancelBtn.setVisibility(View.GONE);
                                    mOkayBtn.setVisibility(View.VISIBLE);

                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void stopSharing(String event) {
        Map<String, Object> stopSharingMap = new HashMap<>();
        stopSharingMap.put("sharing", event);

        mFirestore.collection("Users").document(mUserId)
                .update(stopSharingMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mRegistration.remove();
                            finish();
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
