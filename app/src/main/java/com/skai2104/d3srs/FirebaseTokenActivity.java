package com.skai2104.d3srs;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class FirebaseTokenActivity extends AppCompatActivity {
    private TextView mTokenTV;
    private FirebaseAuth mAuth;

    public static final String USERS_NODE = "users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_token);

        mTokenTV = findViewById(R.id.tokenTV);

        mAuth = FirebaseAuth.getInstance();

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    mTokenTV.setText("Token: " + token);

                    saveTokenToFirebase(token);
                } else
                    mTokenTV.setText(task.getException().getMessage());
            }
        });
    }

    private void saveTokenToFirebase(String token) {
        String email = mAuth.getCurrentUser().getEmail();
        User user = new User(email, token);

        DatabaseReference dbUsers = FirebaseDatabase.getInstance().getReference(USERS_NODE);
        dbUsers.child(mAuth.getCurrentUser().getUid())
                .setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                            Toast.makeText(FirebaseTokenActivity.this, "Token saved", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() == null) {
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }
}
