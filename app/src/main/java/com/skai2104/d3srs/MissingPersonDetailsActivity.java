package com.skai2104.d3srs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MissingPersonDetailsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView mStatusTV;
    private EditText mNameET, mAgeET, mGenderET, mLocationET, mAttireET, mHeightET, mWeightET, mAddress1ET, mAddress2ET,
            mFacialET, mPhysicalET, mBodyET, mHabitsET, mAdditionalET, mPhoneET, mEmailET;
    private Button mSaveBtn;

    private String mName, mAge, mGender, mLocation, mAttire, mHeight, mWeight, mAddress1, mAddress2,
            mFacial, mPhysical, mBody, mHabits, mAdditional, mPhone, mEmail, mStatus, mReportPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missing_person_details);

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
        mSaveBtn = findViewById(R.id.saveBtn);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        mNameET.setEnabled(false);
        mAgeET.setEnabled(false);
        mGenderET.setEnabled(false);
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
        mSaveBtn.setVisibility(View.GONE);

        getSupportActionBar().setTitle(mName);

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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
