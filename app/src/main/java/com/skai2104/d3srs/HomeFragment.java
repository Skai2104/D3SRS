package com.skai2104.d3srs;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private Spinner mStatusSpinner;
    private RecyclerView mGroupListRV;
    private View mView;
    private RelativeLayout mGroupListLayout, mSpinnerLayout;
    private Button mAddGroupBtn;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;

    private SmsManager mSmsManager;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private String mCurrentUserId, mCurrentUserName;
    private List<GroupMember> mGroupMemberList;
    private List<GroupMember> mGroupMemberUpdatedList;
    private List<User> mUserList;
    private List<String> mAuthIdList;
    private GroupListMainRecyclerAdapter mAdapter;
    private User mCurrentUser;
    private double mLatitude = 0.0, mLongitude = 0.0;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_home, container, false);

        getActivity().setTitle("D3SRS");

        mStatusSpinner = mView.findViewById(R.id.statusSpinner);
        mGroupListRV = mView.findViewById(R.id.groupListRV);
        mGroupListLayout = mView.findViewById(R.id.groupListLayout);
        mSpinnerLayout = mView.findViewById(R.id.spinnerLayout);
        mAddGroupBtn = mView.findViewById(R.id.addGroupBtn);

        mSmsManager = SmsManager.getDefault();

        mGroupListLayout.setVisibility(View.GONE);
        mAddGroupBtn.setVisibility(View.GONE);

        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(mView.getContext(), R.array.safetyStatus, R.layout.safety_status_spinner_item);
        mStatusSpinner.setAdapter(arrayAdapter);

        mStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String color = "#B0C4DE";
                switch (position) {
                    case 0:
                        color = "#B0C4DE";
                        break;

                    case 1:
                        color = "#32CD32";
                        break;

                    case 2:
                        color = "#FF8C00";
                        break;
                }
                mSpinnerLayout.setBackgroundColor(Color.parseColor(color));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mView.findViewById(R.id.sendSOSBtn).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent callIntent;

                if (ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    callIntent = new Intent(Intent.ACTION_DIAL);
                } else {
                    //callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent = new Intent(Intent.ACTION_DIAL);
                }
                callIntent.setData(Uri.parse("tel:999"));
                startActivity(callIntent);

                for (User user : mUserList) {
                    sendSOSToUser(user.userId);
                }

                for (String authId : mAuthIdList) {
                    sendSOSToAuth(authId);
                }
                Toast.makeText(getContext(), "SOS sent!", Toast.LENGTH_SHORT).show();

                return true;
            }
        });

        mView.findViewById(R.id.sendStatusBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();

                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT);
                Date date = new Date();
                String dateTime = df.format(date);

                Map<String, Object> updateStatusMap = new HashMap<>();
                updateStatusMap.put("datetime", dateTime);
                updateStatusMap.put("latitude", String.valueOf(mLatitude));
                updateStatusMap.put("longitude", String.valueOf(mLongitude));
                updateStatusMap.put("status", String.valueOf(mStatusSpinner.getSelectedItem()));

                mFirestore.collection("Users").document(mCurrentUserId).update(updateStatusMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //Toast.makeText(getContext(), "Safety status successfully sent!", Toast.LENGTH_SHORT).show();
                            }
                        });

                for (GroupMember groupMember : mGroupMemberList) {
                    if (groupMember.getType().equals("existing")) {
                        sendStatusUpdate(groupMember.getUserId(), dateTime, mLatitude, mLongitude, String.valueOf(mStatusSpinner.getSelectedItem()));
                    }
                    // Send SMS to the group members
                    String smsMessage = "RM0.00 D3SRS: I have updated my safety status to: \n\n" + String.valueOf(mStatusSpinner.getSelectedItem()).toUpperCase() + "\n\nYou can view more details of the status in D3SRS app.";
                    if ((ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS)) == PackageManager.PERMISSION_GRANTED) {
                        mSmsManager.sendTextMessage(groupMember.getPhone(), null, smsMessage, null, null);
                    }
                }
                Toast.makeText(getContext(), "Safety status successfully sent!", Toast.LENGTH_SHORT).show();
            }
        });

        mAddGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SetUpGroupActivity.class);
                startActivity(i);
            }
        });

        mGroupMemberList = new ArrayList<>();
        mGroupMemberUpdatedList = new ArrayList<>();
        mUserList = new ArrayList<>();
        mAuthIdList = new ArrayList<>();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getUid();

        mAdapter = new GroupListMainRecyclerAdapter(getContext(), mGroupMemberUpdatedList);

        mGroupListRV.setHasFixedSize(true);
        mGroupListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        mGroupListRV.setAdapter(mAdapter);

        getCurrentLocation();

        return mView;
    }

    public void sendStatusUpdate(final String userId, final String datetime, final double latitude, final double longitude, final String status) {
        if (mFirebaseUser != null) {
            final String message = "Tap to view the details.";

            mFirestore.collection("Users").document(mCurrentUserId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            mCurrentUserName = documentSnapshot.getString("name");

                            Map<String, Object> statusUpdateMap = new HashMap<>();
                            statusUpdateMap.put("message", message);
                            statusUpdateMap.put("from", mCurrentUserName);
                            statusUpdateMap.put("fromId", mCurrentUserId);
                            statusUpdateMap.put("latitude", String.valueOf(latitude));
                            statusUpdateMap.put("longitude", String.valueOf(longitude));
                            statusUpdateMap.put("status", status);
                            statusUpdateMap.put("datetime", datetime);

                            mFirestore.collection("Users").document(userId).collection("StatusNotification")
                                    .add(statusUpdateMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    refreshPage();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
        }
    }

    public void sendSOSToUser(final String userId) {
        if (mFirebaseUser != null) {
            final String message = "Someone nearby needs your help!";

            mFirestore.collection("Users").document(mCurrentUserId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT);
                            Date date = new Date();
                            String dateTime = df.format(date);

                            mCurrentUserName = documentSnapshot.getString("name");

                            Map<String, Object> SOSMap = new HashMap<>();
                            SOSMap.put("message", message);
                            SOSMap.put("from", mCurrentUserName);
                            SOSMap.put("fromId", mCurrentUserId);
                            SOSMap.put("latitude", String.valueOf(mLatitude));
                            SOSMap.put("longitude", String.valueOf(mLongitude));
                            SOSMap.put("datetime", String.valueOf(dateTime));

                            mFirestore.collection("Users").document(userId).collection("SOSNotification")
                                    .add(SOSMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
        }
    }

    public void sendSOSToAuth(final String userId) {
        if (mFirebaseUser != null) {
            final String message = "Someone nearby needs your help!";

            mFirestore.collection("Users").document(mCurrentUserId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT);
                            Date date = new Date();
                            String dateTime = df.format(date);

                            mCurrentUserName = documentSnapshot.getString("name");

                            Map<String, Object> SOSMap = new HashMap<>();
                            SOSMap.put("message", message);
                            SOSMap.put("from", mCurrentUserName);
                            SOSMap.put("fromId", mCurrentUserId);
                            SOSMap.put("latitude", String.valueOf(mLatitude));
                            SOSMap.put("longitude", String.valueOf(mLongitude));
                            SOSMap.put("datetime", dateTime);

                            mFirestore.collection("Authorities").document(userId).collection("SOSNotification")
                                    .add(SOSMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
        }
    }

    public void getCurrentLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(20 * 1000);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null)
                    return;

                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        mLatitude = location.getLatitude();
                        mLongitude = location.getLongitude();
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    mLatitude = location.getLatitude();
                    mLongitude = location.getLongitude();
                } else if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            }
        });
    }

    public void refreshPage() {
        mFirebaseUser = mAuth.getCurrentUser();

        if (mFirebaseUser != null) {
            mGroupMemberList.clear();
            mGroupMemberUpdatedList.clear();
            mUserList.clear();

            mFirestore.collection("Users")
                    .document(mCurrentUserId)
                    .collection("Group")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (queryDocumentSnapshots != null) {
                                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                    String docId = doc.getDocument().getId();

                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        GroupMember groupMember = doc.getDocument().toObject(GroupMember.class);
                                        groupMember.setDocId(docId);

                                        mGroupMemberList.add(groupMember);
                                    }
                                }
                            }
                        }
                    });

            mFirestore.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String userId = doc.getDocument().getId();

                                User user = doc.getDocument().toObject(User.class).WithId(userId);
                                if (!userId.equals(mCurrentUserId)) {
                                    mUserList.add(user);

                                } else if (userId.equals(mCurrentUserId)) {
                                    mCurrentUser = user;
                                    int position = 0;
                                    String color = "#B0C4DE";
                                    switch (mCurrentUser.getStatus()) {
                                        case "Unknown":
                                            position = 0;
                                            color = "#B0C4DE";
                                            break;

                                        case "Safe":
                                            position = 1;
                                            color = "#32CD32";
                                            break;

                                        case "Waiting for help":
                                            position = 2;
                                            color = "#FF8C00";
                                            break;
                                    }
                                    mStatusSpinner.setSelection(position);
                                    mSpinnerLayout.setBackgroundColor(Color.parseColor(color));
                                }
                                mAdapter.notifyDataSetChanged();
                            }
                        }

                        for (GroupMember groupMember : mGroupMemberList) {
                            for (User user : mUserList) {
                                // To determine that the group member is an existing user
                                // then add to the new list: mGroupMemberUpdatedList
                                if (groupMember.getUserId().equals(user.userId)) {
                                    groupMember.setStatus(user.getStatus());
                                    groupMember.setLatitude(user.getLatitude());
                                    groupMember.setLongitude(user.getLongitude());
                                    groupMember.setDateTime(user.getDatetime());
                                    break;

                                } else {
                                    groupMember.setStatus("Unknown");
                                    groupMember.setLatitude("");
                                    groupMember.setLongitude("");
                                    groupMember.setDateTime("");

                                }
                            }
                            mGroupMemberUpdatedList.add(groupMember);
                        }
                        if (mGroupMemberUpdatedList.isEmpty()) {
                            mGroupListLayout.setVisibility(View.GONE);
                            mAddGroupBtn.setVisibility(View.VISIBLE);
                        } else {
                            mGroupListLayout.setVisibility(View.VISIBLE);
                            mAddGroupBtn.setVisibility(View.GONE);
                        }
                    }
                }
            });

            mFirestore.collection("Authorities").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String authId = doc.getDocument().getId();

                                mAuthIdList.add(authId);
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

        refreshPage();
    }
}
