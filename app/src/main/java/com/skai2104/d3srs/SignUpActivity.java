package com.skai2104.d3srs;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 222;

    private EditText mNameET, mEmailET, mPasswordET, mPhoneET;
    private LinearLayout mProgressBarLayout;
    private CircleImageView mProfilePicIV;

    private String mName, mEmail, mPhone, mTokenId;
    private double mLatitude, mLongitude;
    private Uri mImageUri;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private StorageReference mStorage;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mNameET = findViewById(R.id.nameET);
        mEmailET = findViewById(R.id.emailET);
        mPasswordET = findViewById(R.id.passwordET);
        mPhoneET = findViewById(R.id.phoneET);
        mProgressBarLayout = findViewById(R.id.progressBarLayout);
        mProfilePicIV = findViewById(R.id.profilePicIV);

        mProgressBarLayout.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child("images");

        mProfilePicIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Picture"), PICK_IMAGE);
            }
        });

        findViewById(R.id.loginBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.registerBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(SignUpActivity.this, String.valueOf(mLatitude) + "," + String.valueOf(mLongitude), Toast.LENGTH_SHORT).show();
                register();
            }
        });

        getCurrentLocation();
    }

    public void register() {
        mName = mNameET.getText().toString().trim();
        mEmail = mEmailET.getText().toString().trim();
        String password = mPasswordET.getText().toString().trim();
        mPhone = mPhoneET.getText().toString().trim();

        if (!hasValidationError(mName, mEmail, password, mPhone)) {
            mProgressBarLayout.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(mEmail, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                    @Override
                                    public void onSuccess(InstanceIdResult instanceIdResult) {
                                        mTokenId = instanceIdResult.getToken();
                                        final String userId = mAuth.getCurrentUser().getUid();

                                        Map<String, Object> userMap = new HashMap<>();
                                        userMap.put("name", mName);
                                        userMap.put("email", mEmail);
                                        userMap.put("phone", mPhone);
                                        userMap.put("status", "Unknown");
                                        userMap.put("token_id", mTokenId);
                                        userMap.put("latitude", String.valueOf(mLatitude));
                                        userMap.put("longitude", String.valueOf(mLongitude));

                                        mFirestore.collection("Users").document(userId).set(userMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        if (mImageUri != null) {
                                                            final StorageReference userRegis = mStorage.child(userId + ".jpg");

                                                            UploadTask uploadTask = userRegis.putFile(mImageUri);

                                                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                                                @Override
                                                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                                                    if (!task.isSuccessful()) {
                                                                        throw task.getException();
                                                                    }

                                                                    // Continue with the task to get the download URL
                                                                    return userRegis.getDownloadUrl();
                                                                }
                                                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Uri> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Uri downloadUri = task.getResult();
                                                                        String downloadUrl = downloadUri.toString();

                                                                        Map<String, Object> profilePicMap = new HashMap<>();
                                                                        profilePicMap.put("image", downloadUrl);

                                                                        mFirestore.collection("Users").document(userId).update(profilePicMap)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        Toast.makeText(SignUpActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                                                                                        Intent i = new Intent(SignUpActivity.this, ProfileActivity.class);
                                                                                        startActivity(i);
                                                                                        finish();
                                                                                    }
                                                                                });
                                                                    } else {
                                                                        Toast.makeText(SignUpActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            Toast.makeText(SignUpActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                                                            Intent i = new Intent(SignUpActivity.this, ProfileActivity.class);
                                                            startActivity(i);
                                                            finish();
                                                        }
                                                    }
                                                });
                                    }
                                });
                            } else {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    mProgressBarLayout.setVisibility(View.INVISIBLE);
                                    Intent i = new Intent(SignUpActivity.this, LoginActivity.class);
                                    startActivity(i);
                                    finish();

                                } else {
                                    mProgressBarLayout.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }

    public boolean hasValidationError(String name, String email, String password, String phone) {
        if (name.isEmpty()) {
            mNameET.setError("Name required");
            mNameET.requestFocus();
            return true;
        }

        if (email.isEmpty()) {
            mEmailET.setError("Email required");
            mEmailET.requestFocus();
            return true;
        }

        if (password.isEmpty()) {
            mPasswordET.setError("Password required");
            mPasswordET.requestFocus();
            return true;

        } else if (password.length() < 6 || password.length() > 30) {
            mPasswordET.setError("Password length should be between 6 to 30");
            mPasswordET.requestFocus();
            return true;
        }

        if (phone.isEmpty()) {
            mPhoneET.setError("Phone required");
            mPhoneET.requestFocus();
            return true;
        }
        return false;
    }

    public void getCurrentLocation() {
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    mLatitude = location.getLatitude();
                    mLongitude = location.getLongitude();
                } else if (ActivityCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE) {
            mImageUri = data.getData();
            mProfilePicIV.setImageURI(mImageUri);
        }
    }
}
