package com.skai2104.d3srs;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class SOSListRecyclerAdapter extends RecyclerView.Adapter<SOSListRecyclerAdapter.ViewHolder> {
    private Context mContext;
    private List<SOS> mSosList;

    public SOSListRecyclerAdapter(Context context, List<SOS> sosList) {
        mContext = context;
        mSosList = sosList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sos_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final String fromUser = mSosList.get(position).getFrom();
        final String datetime = mSosList.get(position).getDateTime();
        final String latitude = mSosList.get(position).getLatitude();
        final String longitude = mSosList.get(position).getLongitude();
        final String address = mSosList.get(position).getAddress();
        final String docId = mSosList.get(position).getDocId();

        viewHolder.mNameTV.setText(fromUser);
        viewHolder.mLocationTV.setText(address);
        viewHolder.mDateTimeTV.setText(datetime);

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, SOSDetailsActivity.class);
                i.putExtra("from_user", fromUser);
                i.putExtra("datetime", datetime);
                i.putExtra("latitude", latitude);
                i.putExtra("longitude", longitude);
                i.putExtra("docId", docId);
                mContext.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSosList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView mNameTV, mLocationTV, mDateTimeTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            mNameTV = mView.findViewById(R.id.sosNameTV);
            mLocationTV = mView.findViewById(R.id.locationTV);
            mDateTimeTV = mView.findViewById(R.id.dateTimeTV);
        }
    }
}
