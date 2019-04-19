package com.skai2104.d3srs;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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
        if (status != null) {
            switch (status) {
                case "Unknown":
                    viewHolder.mTypeTV.setBackgroundResource(R.drawable.status_background_unknown);
                    break;

                case "Safe":
                    viewHolder.mTypeTV.setBackgroundResource(R.drawable.status_background_safe);
                    break;

                case "Waiting for help":
                    viewHolder.mTypeTV.setBackgroundResource(R.drawable.status_background_wait_help);
                    break;
            }
        } else {
            viewHolder.mTypeTV.setBackgroundColor(Color.parseColor("#FF8C00"));
        }

        final String userId = mGroupList.get(position).getUserId();
        final String latitude = mGroupList.get(position).getLatitude();
        final String longitude = mGroupList.get(position).getLongitude();
        final String datetime = mGroupList.get(position).getDateTime();
        final String type = mGroupList.get(position).getType();

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type.equals("existing")) {
                    Intent i = new Intent(mContext, StatusDetailsActivity.class);
                    i.putExtra("from_user_id", userId);

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
                    
                } else {
                    Toast.makeText(mContext, "He/She is not a registered user.", Toast.LENGTH_SHORT).show();
                }
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
        private RelativeLayout mListitemLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            mNicknameTV = mView.findViewById(R.id.nicknameTV);
            mTypeTV = mView.findViewById(R.id.typeTV);
            mListitemLayout = mView.findViewById(R.id.listItemLayout);
        }
    }
}
