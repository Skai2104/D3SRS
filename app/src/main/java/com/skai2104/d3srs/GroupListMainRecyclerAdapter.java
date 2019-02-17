package com.skai2104.d3srs;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class GroupListMainRecyclerAdapter extends RecyclerView.Adapter<GroupListMainRecyclerAdapter.ViewHolder> {
    private List<GroupMember> mGroupList;
    private Context mContext;

    public GroupListMainRecyclerAdapter(Context context, List<GroupMember> groupList) {
        mGroupList = groupList;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_list_main_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final String nickname = mGroupList.get(position).getNickname();
        final String name = mGroupList.get(position).getName();

        if (nickname.isEmpty())
            viewHolder.mNicknameTV.setText(name);
        else
            viewHolder.mNicknameTV.setText(nickname);

        final String status = mGroupList.get(position).getStatus();

        viewHolder.mTypeTV.setText(status);
        switch (status) {
            case "Unknown":
                viewHolder.mTypeTV.setBackgroundColor(Color.parseColor("#B0C4DE"));
                break;

            case "Safe":
                viewHolder.mTypeTV.setBackgroundColor(Color.parseColor("#32CD32"));
                break;

            case "Waiting for help":
                viewHolder.mTypeTV.setBackgroundColor(Color.parseColor("#FF8C00"));
                break;
        }

        final String userId = mGroupList.get(position).getUserId();
        final String latitude = mGroupList.get(position).getLatitude();
        final String longitude = mGroupList.get(position).getLongitude();
        final String datetime = mGroupList.get(position).getDateTime();

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, StatusDetailsActivity.class);
                i.putExtra("userId", userId);

                if (nickname.isEmpty()) {
                    i.putExtra("from_user", name);
                } else {
                    i.putExtra("from_user", nickname);
                }

                i.putExtra("status", status);
                i.putExtra("latitude", latitude);
                i.putExtra("longitude", longitude);
                i.putExtra("datetime", datetime);
                mContext.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mGroupList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView mNicknameTV, mTypeTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            mNicknameTV = mView.findViewById(R.id.nicknameTV);
            mTypeTV = mView.findViewById(R.id.typeTV);
        }
    }
}