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

public class SightingInfoListActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView mNoInfoTV, mMissingPersonNameTV;
    private RecyclerView mSightingInfoListRV;
    private ProgressBar mProgressBar;

    private String mCurrentUserId, mDocId, mName;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;

    private List<SightingInfo> mSightingInfoList;
    private SightingInfoListRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sighting_info_list);

        mNoInfoTV = findViewById(R.id.noInfoTV);
        mSightingInfoListRV = findViewById(R.id.sightingInfoListRV);
        mProgressBar = findViewById(R.id.progressBar);
        mMissingPersonNameTV = findViewById(R.id.nameTV);

        mSightingInfoListRV.setVisibility(View.GONE);
        mNoInfoTV.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sighting Info");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mName = getIntent().getStringExtra("name");
        mDocId = getIntent().getStringExtra("docId");

        mMissingPersonNameTV.setText(mName);

        mSightingInfoList = new ArrayList<>();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getUid();

        mAdapter = new SightingInfoListRecyclerAdapter(this, mSightingInfoList, mName, mDocId, mCurrentUserId);

        mSightingInfoListRV.setHasFixedSize(true);
        mSightingInfoListRV.setLayoutManager(new LinearLayoutManager(this));
        mSightingInfoListRV.setAdapter(mAdapter);

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SightingInfoListActivity.this, AddSightingInfoActivity.class);
                i.putExtra("name", mName);
                i.putExtra("docId", mDocId);
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
            mSightingInfoList.clear();

            mFirestore.collection("MissingPersons")
                    .document(mDocId)
                    .collection("SightingInfo")
                    .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            String docId = doc.getDocument().getId();

                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                SightingInfo sightingInfo = doc.getDocument().toObject(SightingInfo.class);
                                sightingInfo.setDocId(docId);
                                mSightingInfoList.add(sightingInfo);

                                mAdapter.notifyDataSetChanged();
                            }
                        }
                        mProgressBar.setVisibility(View.GONE);

                        if (mSightingInfoList.isEmpty()) {
                            mSightingInfoListRV.setVisibility(View.GONE);
                            mNoInfoTV.setVisibility(View.VISIBLE);
                        } else {
                            mSightingInfoListRV.setVisibility(View.VISIBLE);
                            mNoInfoTV.setVisibility(View.GONE);
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
