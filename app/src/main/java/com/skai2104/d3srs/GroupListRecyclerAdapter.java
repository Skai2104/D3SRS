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

import java.util.List;

public class GroupListRecyclerAdapter extends RecyclerView.Adapter<GroupListRecyclerAdapter.ViewHolder> {
    private List<GroupMember> mGroupList;
    private Context mContext;

    public GroupListRecyclerAdapter(Context context, List<GroupMember> groupList) {
        mGroupList = groupList;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_list_item, parent, false);

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

        final String type = mGroupList.get(position).getType();
        String color = "#B0C4DE";
        switch (type) {
            case "existing":
                color = "#ff7f51";
                break;

            case "phone":
                color = "#ffffff";
                break;
        }
        viewHolder.mTypeTV.setBackgroundColor(Color.parseColor(color));

        // docId to update the document
        final String docId = mGroupList.get(position).getDocId();
        final String email = mGroupList.get(position).getEmail();
        final String phone = mGroupList.get(position).getPhone();

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, GroupMemberDetailsActivity.class);
                i.putExtra("docId", docId);
                i.putExtra("name", name);
                i.putExtra("email", email);
                i.putExtra("phone", phone);
                i.putExtra("nickname", nickname);
                i.putExtra("type", type);
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
