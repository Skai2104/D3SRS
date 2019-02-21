package com.skai2104.d3srs;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.GeoApiContext;

import java.util.HashMap;
import java.util.Map;

public class ShareLiveLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private Toolbar mToolbar;
    private TextView mLatitudeTV, mLongitudeTV, mShareLocationTV;
    private LinearLayout mMainLayout, mApprovalLayout;
    private MapView mMapView;
    private RelativeLayout mProgressBarLayout;
    private Button mStopBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mFirebaseUser;

    private FusedLocationProviderClient mClient;
    private LocationCallback mLocationCallback;

    private String mCurrentUserId, mDocId, mRequesterName, mSharing;
    private Map<String, Object> mLiveLocationMap;
    private boolean mIsFound = false;

    private GoogleMap mMap;

    private GeoApiContext mGeoApiContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_live_location);

        mLatitudeTV = findViewById(R.id.latitudeTV);
        mLongitudeTV = findViewById(R.id.longitudeTV);
        mShareLocationTV = findViewById(R.id.shareLocationTV);
        mMainLayout = findViewById(R.id.mainLayout);
        mApprovalLayout = findViewById(R.id.approvalLayout);
        mMapView = findViewById(R.id.mapView);
        mProgressBarLayout = findViewById(R.id.progressBarLayout);
        mStopBtn = findViewById(R.id.stopBtn);

        mMainLayout.setVisibility(View.GONE);
        mApprovalLayout.setVisibility(View.VISIBLE);
        mProgressBarLayout.setVisibility(View.GONE);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Share Your Live Location");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mShareLocationTV.setText(mRequesterName + " wants to view your live location.\n\nDo you want to share it?");

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mCurrentUserId = mAuth.getUid();

        mLiveLocationMap = new HashMap<>();

        initGoogleMap(savedInstanceState);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        findViewById(R.id.shareBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setTitle("Share")
                        .setMessage("Share your live location to " + mRequesterName + "?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mMainLayout.setVisibility(View.VISIBLE);
                                mApprovalLayout.setVisibility(View.GONE);

                                mSharing = "sharing";

                                getCurrentLocation();
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

        findViewById(R.id.rejectBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setTitle("Reject")
                        .setMessage("Are you sure you want to reject the location request from " + mRequesterName + "?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mSharing = "rejected";

                                mProgressBarLayout.setVisibility(View.VISIBLE);
                                mStopBtn.setEnabled(false);

                                getCurrentLocation();
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

        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setTitle("Stop sharing")
                        .setMessage("Are you sure you want to stop your live location sharing to " + mRequesterName + "?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mProgressBarLayout.setVisibility(View.VISIBLE);
                                mStopBtn.setEnabled(false);
                                mSharing = "stop";
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

    public void getCurrentLocation() {
        LocationRequest request = new LocationRequest();
        request.setInterval(1000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();

                if (location != null) {
                    mLatitudeTV.setText(String.valueOf(location.getLatitude()));
                    mLongitudeTV.setText(String.valueOf(location.getLongitude()));

                    refreshMap(mMap);
                    markStartingLocationOnMap(mMap, new LatLng(location.getLatitude(), location.getLongitude()));

                    mLiveLocationMap.put("latitude", location.getLatitude());
                    mLiveLocationMap.put("longitude", location.getLongitude());
                    mLiveLocationMap.put("sharing", mSharing);

                    /*mFirestore.collection("LiveLocations").get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult() != null) {
                                            // If collection "LiveLocations" exists
                                            // then update it
                                            if (task.getResult().size() > 0) {
                                                mIsFound = false;
                                                for (DocumentSnapshot doc : task.getResult()) {
                                                    if (doc.getString("userId").equals(mCurrentUserId)) {
                                                        mDocId = doc.getId();
                                                        mIsFound = true;
                                                        break;
                                                    } else
                                                        mIsFound = false;
                                                }
                                                if (mIsFound) {
                                                    mFirestore.collection("LiveLocations").document(mDocId)
                                                            .update(mLiveLocationMap)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        if (mSharing.equals("stop") || mSharing.equals("rejected")) {
                                                                            mClient.removeLocationUpdates(mLocationCallback);
                                                                            finish();
                                                                        }
                                                                    }
                                                                }
                                                            });

                                                } else {
                                                    mLiveLocationMap.put("userId", mCurrentUserId);

                                                    mFirestore.collection("LiveLocations").add(mLiveLocationMap)
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                    if (task.isSuccessful()) {
                                                                        if (mSharing.equals("stop") || mSharing.equals("rejected")) {
                                                                            mClient.removeLocationUpdates(mLocationCallback);
                                                                            finish();
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                }

                                            } else {
                                                mLiveLocationMap.put("userId", mCurrentUserId);

                                                mFirestore.collection("LiveLocations").add(mLiveLocationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                if (task.isSuccessful()) {
                                                                    if (mSharing.equals("stop") || mSharing.equals("rejected")) {
                                                                        mClient.removeLocationUpdates(mLocationCallback);
                                                                        finish();
                                                                    }
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    }
                                }
                            });*/

                    mFirestore.collection("Users").document(mCurrentUserId).update(mLiveLocationMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        if (mSharing.equals("stop") || mSharing.equals("rejected")) {
                                            mClient.removeLocationUpdates(mLocationCallback);
                                            finish();
                                        }
                                    }
                                }
                            });
                }
            }
        };

        mClient.requestLocationUpdates(request, mLocationCallback, null);
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

    @Override
    public void onBackPressed() {
        if (mSharing.equals("sharing")) {
            super.onBackPressed();
        }
    }
}
