package com.skai2104.d3srs;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class LoginActivity extends AppCompatActivity {
    private EditText mEmailET, mPasswordET;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailET = findViewById(R.id.emailET);
        mPasswordET = findViewById(R.id.passwordET);
        mProgressBar = findViewById(R.id.progressBar);

        mProgressBar.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.loginBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailET.getText().toString().trim();
                String password = mPasswordET.getText().toString().trim();

                if (email.isEmpty()) {
                    mEmailET.setError("Email required");
                    mEmailET.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    mPasswordET.setError("Password required");
                    mPasswordET.requestFocus();
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent i = new Intent(LoginActivity.this, FirebaseTokenActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                mProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(LoginActivity.this, "Invalid user", Toast.LENGTH_SHORT).show();

                            } else {
                                mProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            Intent i = new Intent(this, FirebaseTokenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }
}
