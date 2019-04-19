package com.skai2104.d3srs;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SightingInfoDetailsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView mMissingPersonNameTV, mDateTimeTV, mReportedTV;
    private EditText mContentET, mLocationET;
    private LinearLayout mProgressbarLayout;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;

    private String mCurrentUserId;
    private String mDateTime, mContent, mReportPersonName, mReportPersonId, mDocId, mLocation, mMissingPersonName, mMissingPersonId;
    private boolean mIsEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sighting_info_details);

        mMissingPersonNameTV = findViewById(R.id.nameTV);
        mDateTimeTV = findViewById(R.id.dateTimeTV);
        mReportedTV = findViewById(R.id.reportedTV);
        mContentET = findViewById(R.id.contentET);
        mLocationET = findViewById(R.id.locationET);
        mProgressbarLayout = findViewById(R.id.progressBarLayout);

        mProgressbarLayout.setVisibility(View.GONE);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sighting Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getUid();

        mIsEditing = false;

        mDateTime = getIntent().getStringExtra("datetime");
        mContent = getIntent().getStringExtra("content");
        mReportPersonName = getIntent().getStringExtra("reportPersonName");
        mReportPersonId = getIntent().getStringExtra("reportPersonId");
        mDocId = getIntent().getStringExtra("docId");
        mLocation = getIntent().getStringExtra("location");
        mMissingPersonName = getIntent().getStringExtra("name");
        mMissingPersonId = getIntent().getStringExtra("id");

        mMissingPersonNameTV.setText(mMissingPersonName);
        mDateTimeTV.setText(mDateTime);
        mContentET.setText(mContent);
        mLocationET.setText(mLocation);

        if (mReportPersonId.equals(mCurrentUserId)) {
            mReportedTV.setText("me");

        } else {
            mReportedTV.setText(mReportPersonName);
        }

        disableEdit();

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

    public void disableEdit() {
        mIsEditing = false;
        invalidateOptionsMenu();

        mContentET.setEnabled(false);
        mLocationET.setEnabled(false);
    }

    public void enableEdit() {
        mIsEditing = true;
        invalidateOptionsMenu();

        mContentET.setEnabled(true);
        mLocationET.setEnabled(true);
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

    public void deleteSightingInfo() {
        mFirestore.collection("MissingPersons").document(mMissingPersonId)
                .collection("SightingInfo").document(mDocId)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SightingInfoDetailsActivity.this, "Sighting info deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int displayMenu;
        if (!mIsEditing) {
            displayMenu = R.menu.sighting_info_details_menu;
        } else {
            displayMenu = R.menu.save_button_menu;
        }
        getMenuInflater().inflate(displayMenu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editBtn:
                enableEdit();

                break;

            case R.id.deleteBtn:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this sighting info?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteSightingInfo();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();

                break;

            case R.id.saveBtn:
                mLocation = mLocationET.getText().toString().trim();
                mContent = mContentET.getText().toString().trim();

                if (!hasValidationError(mLocation, mContent)) {
                    mProgressbarLayout.setVisibility(View.VISIBLE);

                    Map<String, Object> updateSightingInfoMap = new HashMap<>();
                    updateSightingInfoMap.put("location", mLocation);
                    updateSightingInfoMap.put("content", mContent);

                    mFirestore.collection("MissingPersons").document(mMissingPersonId)
                            .collection("SightingInfo").document(mDocId)
                            .update(updateSightingInfoMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(SightingInfoDetailsActivity.this, "Saved", Toast.LENGTH_SHORT).show();

                                    disableEdit();

                                    mProgressbarLayout.setVisibility(View.GONE);
                                }
                            });
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (mIsEditing) {
            disableEdit();
        } else {
            onBackPressed();
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem editMenuItem = menu.findItem(R.id.editBtn);
        MenuItem deleteMenuItem = menu.findItem(R.id.deleteBtn);

        if (!mCurrentUserId.equals(mReportPersonId)) {
            editMenuItem.setVisible(false);
            deleteMenuItem.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }
}
