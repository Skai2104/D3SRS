package com.skai2104.d3srs;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditNameActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText mNameET;
    private LinearLayout mProgressBarLayout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private String mCurrentUserId, mCurrentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_name);

        mNameET = findViewById(R.id.nameET);
        mProgressBarLayout = findViewById(R.id.progressBarLayout);

        mProgressBarLayout.setVisibility(View.GONE);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Edit Name");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        findViewById(R.id.saveBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mNameET.getText().toString().trim();

                if (name.isEmpty()) {
                    mNameET.setError("Name is required");
                    mNameET.requestFocus();

                } else {
                    mProgressBarLayout.setVisibility(View.VISIBLE);

                    Map<String, Object> nameUpdateMap = new HashMap<>();
                    nameUpdateMap.put("name", name);

                    mFirestore.collection("Users").document(mCurrentUserId)
                            .update(nameUpdateMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(EditNameActivity.this, "Name is updated successfully!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mFirestore.collection("Users").document(mCurrentUserId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                          mCurrentUserName = documentSnapshot.getString("name");

                          mNameET.setText(mCurrentUserName);
                    }
                });
    }
}
