package com.skai2104.d3srs;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private EditText mNameET, mEmailET, mPasswordET, mPhoneET;
    private ProgressBar mProgressBar;

    private String mName, mEmail, mPhone, mTokenId;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mNameET = findViewById(R.id.nameET);
        mEmailET = findViewById(R.id.emailET);
        mPasswordET = findViewById(R.id.passwordET);
        mPhoneET = findViewById(R.id.phoneET);
        mProgressBar = findViewById(R.id.progressBar);

        mProgressBar.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        findViewById(R.id.loginBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        findViewById(R.id.registerBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    public void register() {
        mName = mNameET.getText().toString().trim();
        mEmail = mEmailET.getText().toString().trim();
        String password = mPasswordET.getText().toString().trim();
        mPhone = mPhoneET.getText().toString().trim();

        if (!hasValidationError(mName, mEmail, password, mPhone)) {
            mProgressBar.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(mEmail, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                    @Override
                                    public void onSuccess(InstanceIdResult instanceIdResult) {
                                        mTokenId = instanceIdResult.getToken();
                                        String userId = mAuth.getCurrentUser().getUid();

                                        Map<String, Object> userMap = new HashMap<>();
                                        userMap.put("name", mName);
                                        userMap.put("email", mEmail);
                                        userMap.put("phone", mPhone);
                                        userMap.put("token_id", mTokenId);

                                        mFirestore.collection("Users").document(userId).set(userMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(SignUpActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                                                        Intent i = new Intent(SignUpActivity.this, ProfileActivity.class);
                                                        startActivity(i);
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                            } else {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                    Intent i = new Intent(SignUpActivity.this, LoginActivity.class);
                                    startActivity(i);
                                    finish();

                                } else {
                                    mProgressBar.setVisibility(View.INVISIBLE);
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
        }

        if (phone.isEmpty()) {
            mPhoneET.setError("Phone required");
            mPhoneET.requestFocus();
            return true;
        }
        return false;
    }
}
