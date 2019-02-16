package com.skai2104.d3srs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

public class GroupMemberDetailsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private LinearLayout mNicknameLayout;
    private EditText mNameET, mNicknameET, mEmailET, mPhoneET;

    private String mDocId, mName, mEmail, mPhone, mNickname, mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member_details);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNicknameLayout = findViewById(R.id.nicknameLayout);
        mNameET = findViewById(R.id.nameET);
        mNicknameET = findViewById(R.id.nicknameET);
        mEmailET = findViewById(R.id.emailET);
        mPhoneET = findViewById(R.id.phoneET);

        mDocId = getIntent().getStringExtra("docId");
        mName = getIntent().getStringExtra("name");
        mEmail = getIntent().getStringExtra("email");
        mPhone = getIntent().getStringExtra("phone");
        mNickname = getIntent().getStringExtra("nickname");
        mType = getIntent().getStringExtra("type");

        String displayName;
        if (mNickname.isEmpty())
            displayName = mName;
        else
            displayName = mNickname;
        getSupportActionBar().setTitle(displayName);

        mNicknameET.setText(mNickname);
        mEmailET.setText(mEmail);
        mPhoneET.setText(mPhone);

        if (mType.equals("existing")) {
            mNameET.setText(mName);
            mNameET.setEnabled(false);
            mEmailET.setEnabled(false);
            mPhoneET.setEnabled(false);

        } else {
            mNicknameLayout.setVisibility(View.GONE);
            mNameET.setText(mNickname);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
