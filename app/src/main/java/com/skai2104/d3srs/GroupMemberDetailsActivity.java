package com.skai2104.d3srs;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class GroupMemberDetailsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private LinearLayout mNicknameLayout;
    private EditText mNameET, mNicknameET, mEmailET, mPhoneET;

    private String mDocId, mName, mEmail, mPhone, mNickname, mType;
    private String mCurrentUserId;
    private boolean mHasError;
    private Map<String, Object> mUpdateMap;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;

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

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getUid();

        mUpdateMap = new HashMap<>();

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

        mHasError = false;
        findViewById(R.id.saveBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mType.equals("existing")) {
                    mHasError = false;
                    String nickname = mNicknameET.getText().toString().trim();
                    mUpdateMap.put("nickname", nickname);

                } else {
                    String name = mNameET.getText().toString().trim();
                    String email = mEmailET.getText().toString().trim();
                    String phone = mPhoneET.getText().toString().trim();

                    if (!hasValidationError(name, phone)) {
                        mHasError = false;
                        mUpdateMap.put("nickname", name);
                        mUpdateMap.put("email", email);
                        mUpdateMap.put("phone", phone);

                    } else {
                        mHasError = true;
                    }
                }
                if (!mHasError) {
                    mFirestore.collection("Users")
                            .document(mCurrentUserId)
                            .collection("Group")
                            .document(mDocId)
                            .update(mUpdateMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(GroupMemberDetailsActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                }
            }
        });
    }

    public boolean hasValidationError(String name, String phone) {
        if (name.isEmpty()) {
            mNameET.setError("Name is required");
            mNameET.requestFocus();
            return true;
        }

        if (phone.isEmpty()) {
            mPhoneET.setError("Phone number is required");
            mPhoneET.requestFocus();
            return true;
        }
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteBtn:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this group member?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteGroupMember();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                break;
        }
        
        return super.onOptionsItemSelected(item);
    }

    public void deleteGroupMember() {
        mFirestore.collection("Users")
                .document(mCurrentUserId)
                .collection("Group")
                .document(mDocId)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(GroupMemberDetailsActivity.this, "Group member deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }
}
