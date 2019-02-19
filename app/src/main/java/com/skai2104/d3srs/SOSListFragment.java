package com.skai2104.d3srs;


import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class SOSListFragment extends Fragment {
    private RecyclerView mSosListRV;
    private TextView mNoSosTV;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;

    private Geocoder mGeocoder;
    private Address mReturnedAddress;

    private StringBuilder mReturnedAddressStr;
    private String mCurrentUserId;
    private String mAddress;
    private List<Address> mAddressList;
    private List<SOS> mSosList;
    private SOSListRecyclerAdapter mAdapter;

    public SOSListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_soslist, container, false);

        mSosListRV = view.findViewById(R.id.sosListRV);
        mNoSosTV = view.findViewById(R.id.noSosTV);

        mSosListRV.setVisibility(View.GONE);
        mNoSosTV.setVisibility(View.VISIBLE);

        mReturnedAddressStr = new StringBuilder();
        mSosList = new ArrayList<>();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getUid();

        mAdapter = new SOSListRecyclerAdapter(getContext(), mSosList);

        mSosListRV.setHasFixedSize(true);
        mSosListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        mSosListRV.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mFirebaseUser = mAuth.getCurrentUser();

        // Get the full address from the latitude and longitude
        mGeocoder = new Geocoder(getContext(), Locale.getDefault());
        mAddressList = new ArrayList<>();
        mAddress = "";

        if (mFirebaseUser != null) {
            mSosList.clear();

            mFirestore.collection("Users").document(mCurrentUserId)
                    .collection("SOSNotification").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            String docId = doc.getDocument().getId();

                            mReturnedAddressStr = new StringBuilder();

                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                SOS sos = doc.getDocument().toObject(SOS.class);

                                try {
                                    mAddressList = mGeocoder.getFromLocation(Double.valueOf(sos.getLatitude()), Double.valueOf(sos.getLongitude()), 1);
                                    if (!mAddressList.isEmpty()) {
                                        mReturnedAddress = mAddressList.get(0);

                                        for (int i = 0; i <= mReturnedAddress.getMaxAddressLineIndex(); i++) {
                                            mReturnedAddressStr.append(mReturnedAddress.getAddressLine(i)).append("\n");
                                        }
                                        mAddress = mReturnedAddressStr.toString();

                                    } else {
                                        Log.d("Current address error", "No address returned");
                                    }
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                                sos.setAddress(mAddress);
                                sos.setDocId(docId);
                                mSosList.add(sos);

                                mAdapter.notifyDataSetChanged();
                            }
                        }
                        if (!mSosList.isEmpty()) {
                            mSosListRV.setVisibility(View.VISIBLE);
                            mNoSosTV.setVisibility(View.GONE);
                        } else {
                            mSosListRV.setVisibility(View.GONE);
                            mNoSosTV.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
    }
}
