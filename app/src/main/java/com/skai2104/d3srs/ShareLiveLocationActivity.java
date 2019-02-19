package com.skai2104.d3srs;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class ShareLiveLocationActivity extends AppCompatActivity {
    private TextView mLatitudeTV, mLongitudeTV;
    private Button mStartBtn, mStopBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mFirebaseUser;

    private FusedLocationProviderClient mClient;
    private LocationCallback mLocationCallback;

    private String mCurrentUserId, mDocId;
    private Double mLatitude = 0.0, mLongitude = 0.0;
    private Map<String, Object> mLiveLocationMap;
    private boolean mIsFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_live_location);

        mLatitudeTV = findViewById(R.id.latitudeTV);
        mLongitudeTV = findViewById(R.id.longitudeTV);
        mStartBtn = findViewById(R.id.startBtn);
        mStopBtn = findViewById(R.id.stopBtn);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mCurrentUserId = mAuth.getUid();

        mLiveLocationMap = new HashMap<>();

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartBtn.setEnabled(false);
                mStopBtn.setEnabled(true);
                getCurrentLocation();
            }
        });

        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartBtn.setEnabled(true);
                mStopBtn.setEnabled(false);
                mClient.removeLocationUpdates(mLocationCallback);
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

                    mLiveLocationMap.put("latitude", location.getLatitude());
                    mLiveLocationMap.put("longitude", location.getLongitude());

                    mFirestore.collection("LiveLocations").get()
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
                                                            .update(mLiveLocationMap);

                                                } else {
                                                    mLiveLocationMap.put("userId", mCurrentUserId);

                                                    mFirestore.collection("LiveLocations").add(mLiveLocationMap);
                                                }

                                            } else {
                                                mLiveLocationMap.put("userId", mCurrentUserId);

                                                mFirestore.collection("LiveLocations").add(mLiveLocationMap);
                                            }
                                        }
                                    }
                                }
                            });
                }
            }
        };

        mClient.requestLocationUpdates(request, mLocationCallback, null);
    }
}
