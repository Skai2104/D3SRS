package com.skai2104.d3srs;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class MissingPersonsFragment extends Fragment {
    private EditText mSearchET;
    private TextView mNoReportTV;
    private RecyclerView mMissingPersonListRV;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;

    private String mCurrentUserId;
    private List<MissingPerson> mMissingPersonList;
    private MissingPersonListRecyclerAdapter mAdapter;

    public MissingPersonsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_missing_persons, container, false);

        mSearchET = view.findViewById(R.id.searchET);
        mNoReportTV = view.findViewById(R.id.noReportTV);
        mMissingPersonListRV = view.findViewById(R.id.missingPersonListRV);

        mMissingPersonListRV.setVisibility(View.GONE);
        mNoReportTV.setVisibility(View.VISIBLE);
        mNoReportTV.setText("There are currently no reported missing person.");

        mSearchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshPage(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ReportMissingPersonActivity.class);
                startActivity(i);
            }
        });

        mMissingPersonList = new ArrayList<>();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getUid();

        mAdapter = new MissingPersonListRecyclerAdapter(getContext(), mMissingPersonList, mSearchET, mCurrentUserId);

        mMissingPersonListRV.setHasFixedSize(true);
        mMissingPersonListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        mMissingPersonListRV.setAdapter(mAdapter);

        return view;
    }

    public void refreshPage(final String searchName) {
        mFirebaseUser = mAuth.getCurrentUser();

        if (mFirebaseUser != null) {
            mMissingPersonList.clear();

            mFirestore.collection("MissingPersons").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            String docId = doc.getDocument().getId();

                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                MissingPerson missingPerson = doc.getDocument().toObject(MissingPerson.class);
                                missingPerson.setDocId(docId);
                                mMissingPersonList.add(missingPerson);

                                mAdapter.notifyDataSetChanged();
                            }
                        }
                        if (!mMissingPersonList.isEmpty()) {
                            mMissingPersonListRV.setVisibility(View.VISIBLE);
                            mNoReportTV.setVisibility(View.GONE);
                        } else {
                            mMissingPersonListRV.setVisibility(View.GONE);
                            mNoReportTV.setVisibility(View.VISIBLE);
                        }

                        boolean isFound = false;
                        MissingPerson missingPerson = new MissingPerson();
                        if (!searchName.isEmpty()) {
                            for (MissingPerson person : mMissingPersonList) {
                                if (person.getName().equals(searchName)) {
                                    missingPerson = person;
                                    isFound = true;
                                    break;
                                } else
                                    isFound = false;
                            }
                            if (isFound) {
                                mMissingPersonList.clear();
                                mMissingPersonList.add(missingPerson);
                            } else {
                                mMissingPersonListRV.setVisibility(View.GONE);
                                mNoReportTV.setVisibility(View.VISIBLE);
                                mNoReportTV.setText("There is no result for your search.");
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        refreshPage("");
    }
}
