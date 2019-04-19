package com.skai2104.d3srs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSettingsActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 333;

    private Toolbar mToolbar;
    private CircleImageView mProfilePicIV;
    private Button mSaveProfilePicBtn;
    private LinearLayout mProgressBarLayout;
    private TextView mNameTV, mEmailTV;

    private Intent mIntent;
    private Uri mImageUri;
    private String mImageUrl, mUserId;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        mProfilePicIV = findViewById(R.id.profilePicIV);
        mSaveProfilePicBtn = findViewById(R.id.saveProfilePicBtn);
        mProgressBarLayout = findViewById(R.id.progressBarLayout);
        mNameTV = findViewById(R.id.nameTV);
        mEmailTV = findViewById(R.id.emailTV);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child("images");
        mUserId = mAuth.getCurrentUser().getUid();

        mFirestore.collection("Users").document(mUserId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        mImageUrl = documentSnapshot.getString("image");

                        if (mImageUrl != null) {
                            if (!mImageUrl.isEmpty()) {
                                Glide.with(AccountSettingsActivity.this)
                                        .load(mImageUrl)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .into(mProfilePicIV);
                            }
                        }
                    }
                });

        mSaveProfilePicBtn.setVisibility(View.GONE);
        mProgressBarLayout.setVisibility(View.GONE);

        mProfilePicIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Picture"), PICK_IMAGE);
            }
        });

        findViewById(R.id.editNameBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIntent = new Intent(AccountSettingsActivity.this, EditNameActivity.class);
                startActivity(mIntent);
            }
        });

        findViewById(R.id.changeEmailBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIntent = new Intent(AccountSettingsActivity.this, ChangeEmailActivity.class);
                startActivity(mIntent);
            }
        });

        findViewById(R.id.changePasswordBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIntent = new Intent(AccountSettingsActivity.this, ChangePasswordActivity.class);
                startActivity(mIntent);
            }
        });

        mSaveProfilePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBarLayout.setVisibility(View.VISIBLE);

                if (mImageUrl != null) {
                    if (!mImageUrl.isEmpty()) {
                        StorageReference imageRef = mStorage.getStorage().getReferenceFromUrl(mImageUrl);
                        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                saveProfilePic();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AccountSettingsActivity.this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        saveProfilePic();
                    }
                } else {
                    saveProfilePic();
                }
            }
        });
    }

    private void saveProfilePic() {
        final StorageReference profilePic = mStorage.child(mUserId + ".jpg");

        UploadTask uploadTask = profilePic.putFile(mImageUri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return profilePic.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String downloadUrl = downloadUri.toString();

                    Map<String, Object> profilePicMap = new HashMap<>();
                    profilePicMap.put("image", downloadUrl);

                    mFirestore.collection("Users").document(mUserId).update(profilePicMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AccountSettingsActivity.this, "Profile picture is updated successfully", Toast.LENGTH_SHORT).show();

                                    mSaveProfilePicBtn.setVisibility(View.GONE);
                                    mProgressBarLayout.setVisibility(View.GONE);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE) {
            if (data != null) {
                mImageUri = data.getData();
                mProfilePicIV.setImageURI(mImageUri);

                mSaveProfilePicBtn.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mFirestore.collection("Users").document(mUserId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");

                        mNameTV.setText(name);
                        mEmailTV.setText(email);
                    }
                });
    }
}
