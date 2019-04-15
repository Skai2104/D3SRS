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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MissingPersonDetailsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView mStatusTV;
    private Spinner mGenderSpinner;
    private EditText mNameET, mAgeET, mGenderET, mLocationET, mAttireET, mHeightET, mWeightET, mAddress1ET, mAddress2ET,
            mFacialET, mPhysicalET, mBodyET, mHabitsET, mAdditionalET, mPhoneET, mEmailET;
    private CircleImageView mImageIV;

    private String mName, mAge, mGender, mLocation, mAttire, mHeight, mWeight, mAddress1, mAddress2,
            mFacial, mPhysical, mBody, mHabits, mAdditional, mPhone, mEmail, mStatus, mReportPerson, mDocId, mImage;
    private String mCurrentUserId;
    private boolean mIsEditing = false;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missing_person_details);

        mGenderSpinner = findViewById(R.id.genderSpinner);
        mStatusTV = findViewById(R.id.statusTV);
        mNameET = findViewById(R.id.nameET);
        mAgeET = findViewById(R.id.ageET);
        mGenderET = findViewById(R.id.genderET);
        mLocationET = findViewById(R.id.locationET);
        mAttireET = findViewById(R.id.attireET);
        mHeightET = findViewById(R.id.heightET);
        mWeightET = findViewById(R.id.weightET);
        mAddress1ET = findViewById(R.id.address1ET);
        mAddress2ET = findViewById(R.id.address2ET);
        mFacialET = findViewById(R.id.facialET);
        mPhysicalET = findViewById(R.id.physicalET);
        mBodyET = findViewById(R.id.bodyET);
        mHabitsET = findViewById(R.id.habitsET);
        mAdditionalET = findViewById(R.id.additionalET);
        mPhoneET = findViewById(R.id.phoneET);
        mEmailET = findViewById(R.id.emailET);
        mImageIV = findViewById(R.id.pictureIV);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getUid();
        
        mIsEditing = false;

        mName = getIntent().getStringExtra("name");
        mAge = getIntent().getStringExtra("age");
        mGender = getIntent().getStringExtra("gender");
        mLocation = getIntent().getStringExtra("location");
        mAttire = getIntent().getStringExtra("attire");
        mHeight = getIntent().getStringExtra("height");
        mWeight = getIntent().getStringExtra("weight");
        mAddress1 = getIntent().getStringExtra("address1");
        mAddress2 = getIntent().getStringExtra("address2");
        mFacial = getIntent().getStringExtra("facial");
        mPhysical = getIntent().getStringExtra("physical");
        mBody = getIntent().getStringExtra("body");
        mHabits = getIntent().getStringExtra("habits");
        mAdditional = getIntent().getStringExtra("additional");
        mPhone = getIntent().getStringExtra("phone");
        mEmail = getIntent().getStringExtra("email");
        mStatus = getIntent().getStringExtra("status");
        mReportPerson = getIntent().getStringExtra("reportPerson");
        mDocId = getIntent().getStringExtra("docId");
        mImage = getIntent().getStringExtra("image");

        disableEdit();

        getSupportActionBar().setTitle(mName);

        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(this, R.array.gender, R.layout.safety_status_spinner_item);
        mGenderSpinner.setAdapter(arrayAdapter);

        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mStatusTV.setText(mStatus);
        mNameET.setText(mName);
        mAgeET.setText(mAge);
        mGenderET.setText(mGender);
        mLocationET.setText(mLocation);
        mAttireET.setText(mAttire);
        mHeightET.setText(mHeight);
        mWeightET.setText(mWeight);
        mAddress1ET.setText(mAddress1);
        mAddress2ET.setText(mAddress2);
        mFacialET.setText(mFacial);
        mPhysicalET.setText(mPhysical);
        mBodyET.setText(mBody);
        mHabitsET.setText(mHabits);
        mAdditionalET.setText(mAdditional);
        mPhoneET.setText(mPhone);
        mEmailET.setText(mEmail);

        if (mImage != null) {
            if (!mImage.isEmpty()) {
                Glide.with(this)
                        .load(mImage)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(mImageIV);
            }
        }

        disableEmptyFields();
    }

    private void disableEmptyFields() {
        if (mLocation.isEmpty())
            findViewById(R.id.locationLayout).setVisibility(View.GONE);

        if (mAttire.isEmpty())
            findViewById(R.id.attireLayout).setVisibility(View.GONE);

        if (mAddress1.isEmpty())
            findViewById(R.id.address1Layout).setVisibility(View.GONE);

        if (mAddress2.isEmpty())
            findViewById(R.id.address2Layout).setVisibility(View.GONE);

        if (mFacial.isEmpty())
            findViewById(R.id.facialLayout).setVisibility(View.GONE);

        if (mPhysical.isEmpty())
            findViewById(R.id.physicalLayout).setVisibility(View.GONE);

        if (mBody.isEmpty())
            findViewById(R.id.bodyLayout).setVisibility(View.GONE);

        if (mHabits.isEmpty())
            findViewById(R.id.habitsLayout).setVisibility(View.GONE);

        if (mAdditional.isEmpty())
            findViewById(R.id.additionalLayout).setVisibility(View.GONE);
    }

    public boolean hasValidationError(String name, String age, String height, String weight, String phone, String email) {
        if (name.isEmpty()) {
            mNameET.setError("Name is required");
            mNameET.requestFocus();
            return true;
        }

        if (age.isEmpty()) {
            mAgeET.setError("Age is required");
            mAgeET.requestFocus();
            return true;
        }

        if (height.isEmpty()) {
            mHeightET.setError("Height is required");
            mHeightET.requestFocus();
            return true;
        }

        if (weight.isEmpty()) {
            mWeightET.setError("Weight is required");
            mWeightET.requestFocus();
            return true;
        }

        if (phone.isEmpty()) {
            mPhoneET.setError("Contact phone number is required");
            mPhoneET.requestFocus();
            return true;
        }

        if (email.isEmpty()) {
            mEmailET.setError("Contact email is required");
            mEmailET.requestFocus();
            return true;
        }
        return false;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        int displayMenu;
        if (!mIsEditing) {
            displayMenu = R.menu.missing_person_details_menu;
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
                findViewById(R.id.locationLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.attireLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.address1Layout).setVisibility(View.VISIBLE);
                findViewById(R.id.address2Layout).setVisibility(View.VISIBLE);
                findViewById(R.id.facialLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.physicalLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.bodyLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.habitsLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.additionalLayout).setVisibility(View.VISIBLE);

                enableEdit();

                break;

            case R.id.deleteBtn:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this missing person report?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteMissingPerson();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();

                break;

            case R.id.sightingInfoBtn:
                Intent i = new Intent(this, SightingInfoListActivity.class);
                i.putExtra("name", mName);
                i.putExtra("docId", mDocId);
                startActivity(i);

                break;

            case R.id.saveBtn:
                mName = mNameET.getText().toString().trim();
                mAge = mAgeET.getText().toString().trim();
                mGender = String.valueOf(mGenderSpinner.getSelectedItem());
                mLocation = mLocationET.getText().toString().trim();
                mAttire = mAttireET.getText().toString().trim();
                mHeight = mHeightET.getText().toString().trim();
                mWeight = mWeightET.getText().toString().trim();
                mAddress1 = mAddress1ET.getText().toString().trim();
                mAddress2 = mAddress2ET.getText().toString().trim();
                mFacial = mFacialET.getText().toString().trim();
                mPhysical = mPhysicalET.getText().toString().trim();
                mBody = mBodyET.getText().toString().trim();
                mHabits = mHabitsET.getText().toString().trim();
                mAdditional = mAdditionalET.getText().toString().trim();
                mPhone = mPhoneET.getText().toString().trim();
                mEmail = mEmailET.getText().toString().trim();

                if (!hasValidationError(mName, mAge, mHeight, mWeight, mPhone, mEmail)) {
                    Map<String, Object> updateMissingReportMap = new HashMap<>();
                    updateMissingReportMap.put("name", mName);
                    updateMissingReportMap.put("age", mAge);
                    updateMissingReportMap.put("gender", mGender);
                    updateMissingReportMap.put("location", mLocation);
                    updateMissingReportMap.put("attire", mAttire);
                    updateMissingReportMap.put("height", mHeight);
                    updateMissingReportMap.put("weight", mWeight);
                    updateMissingReportMap.put("address1", mAddress1);
                    updateMissingReportMap.put("address2", mAddress2);
                    updateMissingReportMap.put("facial", mFacial);
                    updateMissingReportMap.put("physical", mPhysical);
                    updateMissingReportMap.put("body", mBody);
                    updateMissingReportMap.put("habits", mHabits);
                    updateMissingReportMap.put("additional", mAdditional);
                    updateMissingReportMap.put("phone", mPhone);
                    updateMissingReportMap.put("email", mEmail);
                    updateMissingReportMap.put("reported", mReportPerson);
                    updateMissingReportMap.put("status", mStatus);

                    mFirestore.collection("MissingPersons").document(mDocId).update(updateMissingReportMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(MissingPersonDetailsActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                                    
                                    mGenderET.setText(mGender);
                                    mGenderET.setVisibility(View.VISIBLE);
                                    disableEmptyFields();
                                    disableEdit();
                                }
                            });
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem editMenuItem = menu.findItem(R.id.editBtn);
        MenuItem deleteMenuItem = menu.findItem(R.id.deleteBtn);

        if (!mCurrentUserId.equals(mReportPerson)) {
            editMenuItem.setVisible(false);
            deleteMenuItem.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public void deleteMissingPerson() {
        mFirestore.collection("MissingPersons").document(mDocId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MissingPersonDetailsActivity.this, "Missing person report deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MissingPersonDetailsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void disableEdit() {
        mIsEditing = false;
        invalidateOptionsMenu();

        mNameET.setEnabled(false);
        mAgeET.setEnabled(false);
        mGenderET.setEnabled(false);
        mGenderET.setVisibility(View.VISIBLE);
        mGenderSpinner.setVisibility(View.GONE);
        mLocationET.setEnabled(false);
        mAttireET.setEnabled(false);
        mHeightET.setEnabled(false);
        mWeightET.setEnabled(false);
        mAddress1ET.setEnabled(false);
        mAddress2ET.setEnabled(false);
        mFacialET.setEnabled(false);
        mPhysicalET.setEnabled(false);
        mBodyET.setEnabled(false);
        mHabitsET.setEnabled(false);
        mAdditionalET.setEnabled(false);
        mPhoneET.setEnabled(false);
        mEmailET.setEnabled(false);
    }

    public void enableEdit() {
        mIsEditing = true;
        invalidateOptionsMenu();

        mNameET.setEnabled(true);
        mAgeET.setEnabled(true);
        mGenderET.setVisibility(View.GONE);
        mGenderSpinner.setVisibility(View.VISIBLE);

        if (mGender.equals("Male"))
            mGenderSpinner.setSelection(0);
        else
            mGenderSpinner.setSelection(1);

        mLocationET.setEnabled(true);
        mAttireET.setEnabled(true);
        mHeightET.setEnabled(true);
        mWeightET.setEnabled(true);
        mAddress1ET.setEnabled(true);
        mAddress2ET.setEnabled(true);
        mFacialET.setEnabled(true);
        mPhysicalET.setEnabled(true);
        mBodyET.setEnabled(true);
        mHabitsET.setEnabled(true);
        mAdditionalET.setEnabled(true);
        mPhoneET.setEnabled(true);
        mEmailET.setEnabled(true);
    }
}
