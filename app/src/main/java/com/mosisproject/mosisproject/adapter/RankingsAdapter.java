package com.mosisproject.mosisproject.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mosisproject.mosisproject.R;
import com.mosisproject.mosisproject.model.User;
import com.mosisproject.mosisproject.module.GlideApp;

import java.util.ArrayList;
import java.util.List;

public class RankingsAdapter extends ArrayAdapter {

    Context mContext;
    FragmentManager mFragmentManager;
    List<User> rankingList = new ArrayList<>();
    List<String> emails = new ArrayList<>();
    List<String> names = new ArrayList<>();
    List<String> ids = new ArrayList<>();
    List<String> points = new ArrayList<>();

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    public RankingsAdapter(Context context, List<User> rankingList) {
        super(context, R.layout.ranking_list_item);

        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.storageReference = firebaseStorage.getReference();
        this.rankingList = rankingList;
        this.mContext = context;
        for(int i = 0; i < rankingList.size(); i++) {
            this.emails.add(rankingList.get(i).getEmail());
            this.names.add(rankingList.get(i).getName() + " " + rankingList.get(i).getSurname());
            this.ids.add(rankingList.get(i).getId());
            this.points.add(rankingList.get(i).getPoints());
        }
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder mViewHolder = new ViewHolder();
        if(convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.ranking_list_item, parent, false);

            mViewHolder.mEmails = (TextView) convertView.findViewById(R.id.ranker_email);
            mViewHolder.mNames = (TextView) convertView.findViewById(R.id.ranker_full_name);
            mViewHolder.mPhotos = (ImageView) convertView.findViewById(R.id.ranker_image);
            mViewHolder.mPoints = convertView.findViewById(R.id.ranking_points);
            convertView.setTag(mViewHolder);
        }
        else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mViewHolder.mEmails.setText(emails.get(position));
        mViewHolder.mNames.setText(names.get(position));
        mViewHolder.mPoints.setText(points.get(position));

        StorageReference sRef = storageReference.child("profile_images/" + ids.get(position) + ".jpg");

        GlideApp.with(mContext)
                .load(sRef)
                .into(mViewHolder.mPhotos);

        return convertView;

    }

    static class ViewHolder {
        TextView mNames;
        TextView mEmails;
        TextView mPoints;
        ImageView mPhotos;

    }
}
