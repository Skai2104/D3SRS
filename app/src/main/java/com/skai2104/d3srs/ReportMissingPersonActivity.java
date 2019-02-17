package com.skai2104.d3srs;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ReportMissingPersonActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private Spinner mGenderSpinner;
    private EditText mNameET, mAgeET, mLocationET, mAttireET, mHeightET, mWeightET, mAddress1ET, mAddress2ET,
             mFacialET, mPhysicalET, mBodyET, mHabitsET, mAdditionalET, mPhoneET, mEmailET;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_missing_person);

        mGenderSpinner = findViewById(R.id.genderSpinner);
        mNameET = findViewById(R.id.nameET);
        mAgeET = findViewById(R.id.ageET);
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

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Report Missing Person");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        findViewById(R.id.reportBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mNameET.getText().toString().trim();
                String age = mAgeET.getText().toString().trim();
                String gender = String.valueOf(mGenderSpinner.getSelectedItem());
                String location = mLocationET.getText().toString().trim();
                String attire = mAttireET.getText().toString().trim();
                String height = mHeightET.getText().toString().trim();
                String weight = mWeightET.getText().toString().trim();
                String address1 = mAddress1ET.getText().toString().trim();
                String address2 = mAddress2ET.getText().toString().trim();
                String facial = mFacialET.getText().toString().trim();
                String physical = mPhysicalET.getText().toString().trim();
                String body = mBodyET.getText().toString().trim();
                String habits = mHabitsET.getText().toString().trim();
                String additional = mAdditionalET.getText().toString().trim();
                String phone = mPhoneET.getText().toString().trim();
                String email = mEmailET.getText().toString().trim();

                if (!hasValidationError(name, age, height, weight, phone, email)) {
                    Map<String, Object> reportMissingMap = new HashMap<>();
                    reportMissingMap.put("name", name);
                    reportMissingMap.put("age", age);
                    reportMissingMap.put("gender", gender);
                    reportMissingMap.put("location", location);
                    reportMissingMap.put("attire", attire);
                    reportMissingMap.put("height", height);
                    reportMissingMap.put("weight", weight);
                    reportMissingMap.put("address1", address1);
                    reportMissingMap.put("address2", address2);
                    reportMissingMap.put("facial", facial);
                    reportMissingMap.put("physical", physical);
                    reportMissingMap.put("body", body);
                    reportMissingMap.put("habits", habits);
                    reportMissingMap.put("additional", additional);
                    reportMissingMap.put("phone", phone);
                    reportMissingMap.put("email", email);
                    reportMissingMap.put("reported", mCurrentUserId);
                    reportMissingMap.put("status", "Not Found");

                    CollectionReference collectionReference = mFirestore.collection("MissingPersons");
                    collectionReference.add(reportMissingMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(ReportMissingPersonActivity.this, "Report is sent successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ReportMissingPersonActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
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
        onBackPressed();
        return true;
    }
}
