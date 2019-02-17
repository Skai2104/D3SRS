package com.skai2104.d3srs;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class GroupListActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mGroupListRV;
    private TextView mNoMemberTV;
    private ProgressBar mProgressBar;

    private String mCurrentUserId;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;

    private List<GroupMember> mGroupMemberList;
    private GroupListRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        mGroupListRV = findViewById(R.id.groupListRV);
        mNoMemberTV = findViewById(R.id.noMemberTV);
        mProgressBar = findViewById(R.id.progressBar);

        mGroupListRV.setVisibility(View.GONE);
        mNoMemberTV.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGroupMemberList = new ArrayList<>();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getUid();

        mAdapter = new GroupListRecyclerAdapter(this, mGroupMemberList);

        mGroupListRV.setHasFixedSize(true);
        mGroupListRV.setLayoutManager(new LinearLayoutManager(this));
        mGroupListRV.setAdapter(mAdapter);

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GroupListActivity.this, SetUpGroupActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mProgressBar.setVisibility(View.VISIBLE);

        mFirebaseUser = mAuth.getCurrentUser();

        if (mFirebaseUser != null) {
            mGroupMemberList.clear();

            mFirestore.collection("Users")
                    .document(mCurrentUserId)
                    .collection("Group")
                    .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (queryDocumentSnapshots != null) {
                                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                    String docId = doc.getDocument().getId();

                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        GroupMember groupMember = doc.getDocument().toObject(GroupMember.class);
                                        groupMember.setDocId(docId);
                                        mGroupMemberList.add(groupMember);

                                        mAdapter.notifyDataSetChanged();
                                    }
                                }
                                mProgressBar.setVisibility(View.GONE);

                                if (mGroupMemberList.isEmpty()) {
                                    mGroupListRV.setVisibility(View.GONE);
                                    mNoMemberTV.setVisibility(View.VISIBLE);
                                } else {
                                    mGroupListRV.setVisibility(View.VISIBLE);
                                    mNoMemberTV.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
