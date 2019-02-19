package com.skai2104.d3srs;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddSightingInfoActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView mNameTV;
    private EditText mContentET, mLocationET;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;

    private String mName, mDocId, mCurrentUserId, mCurrentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sighting_info);

        mNameTV = findViewById(R.id.nameTV);
        mContentET = findViewById(R.id.contentET);
        mLocationET = findViewById(R.id.locationET);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mName = getIntent().getStringExtra("name");
        mDocId = getIntent().getStringExtra("docId");

        getSupportActionBar().setTitle("Add Sighting Info");

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getUid();

        mLocationET.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });

        mContentET.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_button_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveBtn:
                saveSightingInfo();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean hasValidationError(String location, String content) {
        if (location.isEmpty()) {
            mLocationET.setError("Location is required");
            mLocationET.requestFocus();
            return true;
        }

        if (content.isEmpty()) {
            mContentET.setError("Sighting info is required");
            mContentET.requestFocus();
            return true;
        }

        return false;
    }

    public void saveSightingInfo() {
        mFirebaseUser = mAuth.getCurrentUser();

        if (mFirebaseUser != null) {
            final String location = mLocationET.getText().toString().trim();
            final String content = mContentET.getText().toString().trim();

            if (!hasValidationError(location, content)) {
                mFirestore.collection("Users").document(mCurrentUserId).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                mCurrentUserName = documentSnapshot.getString("name");

                                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT);
                                Date date = new Date();
                                String dateTime = df.format(date);

                                Map<String, Object> addSightingInfoMap = new HashMap<>();
                                addSightingInfoMap.put("datetime", dateTime);
                                addSightingInfoMap.put("content", content);
                                addSightingInfoMap.put("reportPersonName", mCurrentUserName);
                                addSightingInfoMap.put("reportPersonId", mCurrentUserId);
                                addSightingInfoMap.put("location", location);

                                mFirestore.collection("MissingPersons").document(mDocId)
                                        .collection("SightingInfo").add(addSightingInfoMap)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(AddSightingInfoActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(AddSightingInfoActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
            }
        }
    }
}
