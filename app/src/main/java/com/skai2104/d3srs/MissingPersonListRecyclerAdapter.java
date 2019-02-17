package com.skai2104.d3srs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MissingPersonListRecyclerAdapter extends RecyclerView.Adapter<MissingPersonListRecyclerAdapter.ViewHolder> {
    private List<MissingPerson> mMissingPersonList;
    private Context mContext;

    public MissingPersonListRecyclerAdapter(Context context, List<MissingPerson> missingPersonList) {
        mMissingPersonList = missingPersonList;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.missing_person_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final String name = mMissingPersonList.get(position).getName();

        viewHolder.mNameTV.setText(name);
    }

    @Override
    public int getItemCount() {
        return mMissingPersonList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView mNameTV;
        private ImageView mPhotoIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            mNameTV = mView.findViewById(R.id.nameTV);
            mPhotoIV = mView.findViewById(R.id.photoIV);
        }
    }
}
