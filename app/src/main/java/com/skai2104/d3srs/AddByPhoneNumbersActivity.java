package com.skai2104.d3srs;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddByPhoneNumbersActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText mNameET, mPhoneET, mEmailET;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;

    private String mCurrentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_by_phone_numbers);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Add By Phone Numbers");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNameET = findViewById(R.id.nameET);
        mPhoneET = findViewById(R.id.phoneET);
        mEmailET = findViewById(R.id.emailET);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getUid();

        findViewById(R.id.saveBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mNameET.getText().toString().trim();
                String phone = mPhoneET.getText().toString().trim();
                String email = mEmailET.getText().toString().trim();

                if (!hasValidationError(name, phone)) {
                    Map<String, Object> groupMemberMap = new HashMap<>();
                    groupMemberMap.put("userId", "");
                    groupMemberMap.put("name", "");
                    groupMemberMap.put("email", email);
                    groupMemberMap.put("phone", phone);
                    groupMemberMap.put("nickname", name);
                    groupMemberMap.put("type", "phone");

                    mFirestore.collection("Users")
                            .document(mCurrentUserId)
                            .collection("Group")
                            .add(groupMemberMap)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(AddByPhoneNumbersActivity.this, "Group Member Added!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AddByPhoneNumbersActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
}
