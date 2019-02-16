package com.skai2104.d3srs;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class AddFromExistingUsersActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText mPhoneET, mNicknameET;
    private ProgressBar mProgressBar;
    private TextView mFoundTV, mNameTV, mEmailTV, mPhoneTV;
    private LinearLayout mFoundLayout;
    private Button mAddBtn;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;

    private List<User> mUserList;
    private List<GroupMember> mGroupMemberList;
    private User mFoundUser;
    private GroupMember mAddedGroupMember;

    private String mPhone;
    private String mCurrentUserId;
    private boolean mFound, mAdded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_from_existing_users);

        mPhoneET = findViewById(R.id.phoneET);
        mProgressBar = findViewById(R.id.progressBar);
        mFoundTV = findViewById(R.id.foundTV);
        mFoundLayout = findViewById(R.id.foundLayout);
        mNameTV = findViewById(R.id.nameTV);
        mEmailTV = findViewById(R.id.emailTV);
        mPhoneTV = findViewById(R.id.phoneTV);
        mNicknameET = findViewById(R.id.nicknameET);
        mAddBtn = findViewById(R.id.addBtn);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Add From Existing Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserList = new ArrayList<>();
        mGroupMemberList = new ArrayList<>();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getUid();

        mProgressBar.setVisibility(View.GONE);
        mFoundLayout.setVisibility(View.GONE);
        mFoundTV.setText("");
        mNicknameET.setText("");
        mFound = false;
        mAdded = false;

        findViewById(R.id.searchBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhone = mPhoneET.getText().toString().trim();
                if (mPhone.isEmpty()) {
                    mPhoneET.setError("Please enter the phone number");
                    mPhoneET.requestFocus();
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    for (User user : mUserList) {
                        if (user.getPhone().equals(mPhone)) {
                            mProgressBar.setVisibility(View.GONE);
                            mFound = true;
                            mFoundUser = user;
                            break;
                        } else {
                            mProgressBar.setVisibility(View.GONE);
                            mFound = false;
                        }
                    }
                    
                    if (mFound) {
                        for (GroupMember groupMember : mGroupMemberList) {
                            if (mPhone.equals(groupMember.getPhone())) {
                                mAdded = true;
                                mAddedGroupMember = groupMember;
                                break;
                            }
                            else {
                                mAdded = false;
                            }
                        }

                        if (mAdded) {
                            mAddBtn.setEnabled(false);
                            mAddBtn.setText("ADDED");
                            mNicknameET.setText(mAddedGroupMember.getNickname());
                            mNicknameET.setEnabled(false);
                        } else {
                            mAddBtn.setEnabled(true);
                            mAddBtn.setText("ADD");
                            mNicknameET.setText("");
                            mNicknameET.setEnabled(true);
                        }

                        mFoundTV.setText("Found User:");
                        mFoundLayout.setVisibility(View.VISIBLE);
                        mNameTV.setText(mFoundUser.getName());
                        mEmailTV.setText(mFoundUser.getEmail());
                        mPhoneTV.setText(mFoundUser.getPhone());

                        mAddBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String nickname = mNicknameET.getText().toString().trim();

                                Map<String, Object> groupMemberMap = new HashMap<>();
                                groupMemberMap.put("userId", mFoundUser.userId);
                                groupMemberMap.put("name", mFoundUser.getName());
                                groupMemberMap.put("email", mFoundUser.getEmail());
                                groupMemberMap.put("phone", mFoundUser.getPhone());
                                groupMemberMap.put("nickname", nickname);
                                groupMemberMap.put("type", "existing");

                                mFirestore.collection("Users")
                                        .document(mCurrentUserId)
                                        .collection("Group")
                                        .add(groupMemberMap)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(AddFromExistingUsersActivity.this, "Group member added successfully!", Toast.LENGTH_SHORT).show();
                                                mAddBtn.setEnabled(false);
                                                mAddBtn.setText("ADDED");
                                                mNicknameET.setEnabled(false);
                                                resetGroupMemberList();
                                            }
                                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AddFromExistingUsersActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });

                    } else {
                        mFoundTV.setText("User Not Found");
                        mFoundLayout.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mFirebaseUser = mAuth.getCurrentUser();

        if (mFirebaseUser != null) {
            mUserList.clear();

            mFirestore.collection("Users").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String userId = doc.getDocument().getId();

                                if (!userId.equals(mCurrentUserId)) {
                                    User user = doc.getDocument().toObject(User.class).WithId(userId);
                                    mUserList.add(user);
                                }
                            }
                        }
                    }
                }
            });
            resetGroupMemberList();
        }
    }

    public void resetGroupMemberList() {
        mGroupMemberList.clear();

        mFirestore.collection("Users")
                .document(mCurrentUserId)
                .collection("Group")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    GroupMember groupMember = doc.getDocument().toObject(GroupMember.class);
                                    mGroupMemberList.add(groupMember);
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
