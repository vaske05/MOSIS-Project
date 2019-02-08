package com.mosisproject.mosisproject.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

public class EventFriendsAdapter extends ArrayAdapter<String> {

    Context mContext;
    FragmentManager mFragmentManager;
    List<User> userList;

    List<User> eventUsers = new ArrayList<>();
    List<String> emails = new ArrayList<>();
    List<String> names = new ArrayList<>();
    List<String> ids = new ArrayList<>();

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    final List<Bitmap> photoList = new ArrayList<>();

    public EventFriendsAdapter(Context context, List<User> userList, FragmentManager fragmentManager) {
        super(context, R.layout.friend_list_item);

        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.storageReference = firebaseStorage.getReference();

        this.userList = userList;
        this.mContext = context;
        this.mFragmentManager = fragmentManager;
        for(int i = 0; i < userList.size(); i++) {
            this.emails.add(userList.get(i).getEmail());
            this.names.add(userList.get(i).getName() + " " + userList.get(i).getSurname());
            this.ids.add(userList.get(i).getId());
        }
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = new ViewHolder();

        if(convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.event_friend_list_item, parent, false);

            mViewHolder.mEmails = (TextView) convertView.findViewById(R.id.event_friend_email);
            mViewHolder.mNames = (TextView) convertView.findViewById(R.id.event_friend_full_name);
            mViewHolder.mPhotos = (ImageView) convertView.findViewById(R.id.event_friend_image);
            mViewHolder.mCheckBox = (CheckBox) convertView.findViewById(R.id.event_friend_checkbox);
            convertView.setTag(mViewHolder);

        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mViewHolder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    eventUsers.add(userList.get(position));
                } else {
                    eventUsers.remove(userList.get(position));
                }
            }
        });

        mViewHolder.mEmails.setText(emails.get(position));
        mViewHolder.mNames.setText(names.get(position));

        StorageReference sRef = storageReference.child("profile_images/" + ids.get(position) + ".jpg");

        GlideApp.with(mContext)
                .load(sRef)
                .into(mViewHolder.mPhotos);

        return convertView;
    }

    static class ViewHolder {
        TextView mNames;
        TextView mEmails;
        ImageView mPhotos;
        CheckBox mCheckBox;
    }

    public List<User> getEventUsers() {
        return eventUsers;
    }

    public void setEventUsers(List<User> eventUsers) {
        this.eventUsers = eventUsers;
    }


}
