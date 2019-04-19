package com.skai2104.d3srs;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MissingPersonListRecyclerAdapter extends RecyclerView.Adapter<MissingPersonListRecyclerAdapter.ViewHolder> {
    private List<MissingPerson> mMissingPersonList;
    private Context mContext;
    private EditText mSearchET;

    private String mCurrentUserId;

    public MissingPersonListRecyclerAdapter(Context context, List<MissingPerson> missingPersonList, EditText searchET, String currentUserId) {
        mMissingPersonList = missingPersonList;
        mContext = context;
        mSearchET = searchET;
        mCurrentUserId = currentUserId;
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
        final String age = mMissingPersonList.get(position).getAge();
        final String gender = mMissingPersonList.get(position).getGender();
        final String location = mMissingPersonList.get(position).getLocation();
        final String attire = mMissingPersonList.get(position).getAttire();
        final String height = mMissingPersonList.get(position).getHeight();
        final String weight = mMissingPersonList.get(position).getWeight();
        final String address1 = mMissingPersonList.get(position).getAddress1();
        final String address2 = mMissingPersonList.get(position).getAddress2();
        final String facial = mMissingPersonList.get(position).getFacial();
        final String physical = mMissingPersonList.get(position).getPhysical();
        final String body = mMissingPersonList.get(position).getBody();
        final String habits = mMissingPersonList.get(position).getHabits();
        final String additional = mMissingPersonList.get(position).getAdditional();
        final String phone = mMissingPersonList.get(position).getPhone();
        final String email = mMissingPersonList.get(position).getEmail();
        final String status = mMissingPersonList.get(position).getStatus();
        final String reportPerson = mMissingPersonList.get(position).getReported();
        final String docId = mMissingPersonList.get(position).getDocId();
        final String image = mMissingPersonList.get(position).getImage();

        viewHolder.mNameTV.setText(name);

        if (image != null) {
            if (!image.isEmpty()) {
                Glide.with(mContext)
                        .load(image)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(viewHolder.mPhotoIV);
            } else {
                viewHolder.mPhotoIV.setImageResource(R.drawable.person_icon);
            }
        } else {
            viewHolder.mPhotoIV.setImageResource(R.drawable.person_icon);
        }

        if (reportPerson.equals(mCurrentUserId)) {
            viewHolder.mReportedTV.setVisibility(View.VISIBLE);

        } else {
            viewHolder.mReportedTV.setVisibility(View.GONE);
        }

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchET.setText("");

                Intent i = new Intent(mContext, MissingPersonDetailsActivity.class);
                i.putExtra("name", name);
                i.putExtra("age", age);
                i.putExtra("gender", gender);
                i.putExtra("location", location);
                i.putExtra("attire", attire);
                i.putExtra("height", height);
                i.putExtra("weight", weight);
                i.putExtra("address1", address1);
                i.putExtra("address2", address2);
                i.putExtra("facial", facial);
                i.putExtra("physical", physical);
                i.putExtra("body", body);
                i.putExtra("habits", habits);
                i.putExtra("additional", additional);
                i.putExtra("phone", phone);
                i.putExtra("email", email);
                i.putExtra("status", status);
                i.putExtra("reportPerson", reportPerson);
                i.putExtra("docId", docId);
                i.putExtra("image", image);
                mContext.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMissingPersonList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView mNameTV, mReportedTV;
        private CircleImageView mPhotoIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            mNameTV = mView.findViewById(R.id.nameTV);
            mPhotoIV = mView.findViewById(R.id.photoIV);
            mReportedTV = mView.findViewById(R.id.reportedTV);
        }
    }
}
