package com.skai2104.d3srs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    final static int REQUEST_CODE = 100;
    private boolean mCallPermissionGranted = false, mCallPermissionGrantedLocation = false;
    private String mCurrentUserName, mUserId, mCurrentUserId;
    private String[] mPermissionList = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
    };
    private double mLatitude, mLongitude;

    private List<User> mUserList;
    private List<String> mAuthIdList;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        if (mCallPermissionGrantedLocation) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(20 * 1000);

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null)
                        return;

                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            mLatitude = location.getLatitude();
                            mLongitude = location.getLongitude();
                        }
                    }
                }
            };

            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mLatitude = location.getLatitude();
                        mLongitude = location.getLongitude();
                    } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        checkPermission();

                        return;
                    }
                    mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                }
            });
        }

        findViewById(R.id.skipBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFirebaseUser != null) {
                    Map<String, Object> updateLocationMap = new HashMap<>();
                    updateLocationMap.put("latitude", String.valueOf(mLatitude));
                    updateLocationMap.put("longitude", String.valueOf(mLongitude));

                    mFirestore.collection("Users").document(mCurrentUserId).update(updateLocationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent i = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(i);
                            finish();
                        }
                    });
                } else {
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });

        findViewById(R.id.sosBtn).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent callIntent;

                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    callIntent = new Intent(Intent.ACTION_DIAL);
                } else {
                    //callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent = new Intent(Intent.ACTION_DIAL);
                }
                callIntent.setData(Uri.parse("tel:999"));
                startActivity(callIntent);

                for (User user : mUserList) {
                    sendSOSToUser(user.userId);
                }

                for (String authId : mAuthIdList) {
                    sendSOSToAuth(authId);
                }
                saveSOSToDb();
                Toast.makeText(MainActivity.this, "SOS sent!", Toast.LENGTH_SHORT).show();

                return true;
            }
        });

        mUserList = new ArrayList<>();
        mAuthIdList = new ArrayList<>();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getUid();
    }

    private void checkPermission() {
        List<String> permissionNeededList = new ArrayList<>();
        for (String permission : mPermissionList) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                permissionNeededList.add(permission);
        }
        if (!permissionNeededList.isEmpty()) {
            mCallPermissionGranted = !permissionNeededList.contains("Manifest.permission.CALL_PHONE");
            
            ActivityCompat.requestPermissions(this, permissionNeededList.toArray(new String[permissionNeededList.size()]), REQUEST_CODE);
        } else
            mCallPermissionGrantedLocation = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (String permission : permissions) {
                        if (permission.equals("Manifest.permission.CALL_PHONE"))
                            mCallPermissionGranted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
                    }
                }

                return;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mFirebaseUser = mAuth.getCurrentUser();

        if (mFirebaseUser != null) {
            mUserList.clear();

            mFirestore.collection("Users").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String userId = doc.getDocument().getId();

                                if (!userId.equals(mCurrentUserId)) {
                                    User user = doc.getDocument().toObject(User.class).WithId(userId);
                                    mUserList.add(user);
                                }
                            }
                        }
                    }
                }
            });

            mFirestore.collection("Authorities").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String authId = doc.getDocument().getId();

                                mAuthIdList.add(authId);
                            }
                        }
                    }
                }
            });
        }
    }

    public void sendSOSToUser(final String userId) {
        if (mFirebaseUser != null) {
            final String message = "Someone nearby needs your help!";

            mFirestore.collection("Users").document(mCurrentUserId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT);
                            Date date = new Date();
                            String dateTime = df.format(date);

                            mCurrentUserName = documentSnapshot.getString("name");

                            Map<String, Object> SOSMap = new HashMap<>();
                            SOSMap.put("message", message);
                            SOSMap.put("from", mCurrentUserName);
                            SOSMap.put("fromId", mCurrentUserId);
                            SOSMap.put("latitude", String.valueOf(mLatitude));
                            SOSMap.put("longitude", String.valueOf(mLongitude));
                            SOSMap.put("datetime", dateTime);

                            mFirestore.collection("Users").document(userId).collection("SOSNotification")
                                    .add(SOSMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
        } else {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }
    }

    public void sendSOSToAuth(final String userId) {
        if (mFirebaseUser != null) {
            final String message = "SOS signal from the user. Tap for more details.";

            mFirestore.collection("Users").document(mCurrentUserId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT);
                            Date date = new Date();
                            String dateTime = df.format(date);

                            mCurrentUserName = documentSnapshot.getString("name");

                            Map<String, Object> SOSMap = new HashMap<>();
                            SOSMap.put("message", message);
                            SOSMap.put("from", mCurrentUserName);
                            SOSMap.put("fromId", mCurrentUserId);
                            SOSMap.put("latitude", String.valueOf(mLatitude));
                            SOSMap.put("longitude", String.valueOf(mLongitude));
                            SOSMap.put("datetime", dateTime);

                            mFirestore.collection("Authorities").document(userId).collection("SOSNotification")
                                    .add(SOSMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
        } else {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }
    }

    public void saveSOSToDb() {
        if (mFirebaseUser != null) {
            final String message = "SOS signal from the user. Tap for more details.";

            mFirestore.collection("Users").document(mCurrentUserId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT);
                            Date date = new Date();
                            String dateTime = df.format(date);

                            mCurrentUserName = documentSnapshot.getString("name");

                            Map<String, Object> saveSOSMap = new HashMap<>();
                            saveSOSMap.put("message", message);
                            saveSOSMap.put("from", mCurrentUserName);
                            saveSOSMap.put("fromId", mCurrentUserId);
                            saveSOSMap.put("latitude", String.valueOf(mLatitude));
                            saveSOSMap.put("longitude", String.valueOf(mLongitude));
                            saveSOSMap.put("datetime", dateTime);

                            mFirestore.collection("SOS").add(saveSOSMap)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });
        }
    }
}
