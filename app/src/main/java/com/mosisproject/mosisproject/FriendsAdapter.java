package com.mosisproject.mosisproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mosisproject.mosisproject.model.User;

import java.util.ArrayList;
import java.util.List;

public class FriendsAdapter extends ArrayAdapter<String> {

    Context mContext;
    List<User> userList = new ArrayList<>();
    List<String> emails = new ArrayList<>();
    List<String> names = new ArrayList<>();
    List<String> ids = new ArrayList<>();


    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    final List<Bitmap> photoList = new ArrayList<>();

    public FriendsAdapter( Context context, List<User> userList) {
        super(context, R.layout.friend_list_item);

        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.storageReference = firebaseStorage.getReference();

        this.userList = userList;
        this.mContext = context;
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
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder mViewHolder = new ViewHolder();

        if(convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = mInflater.inflate(R.layout.friend_list_item, parent, false);

            mViewHolder.mEmails = (TextView) convertView.findViewById(R.id.friend_email);
            mViewHolder.mNames = (TextView) convertView.findViewById(R.id.friend_full_name);
            mViewHolder.mPhotos = (ImageView) convertView.findViewById(R.id.friend_image);
            //ImageView imageView = (ImageView) convertView.findViewById(R.id.friend_image);

            convertView.setTag(mViewHolder);
        }
        else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.mEmails.setText(emails.get(position));
        mViewHolder.mNames.setText(names.get(position));

        StorageReference sRef = storageReference.child("profile_images/" + ids.get(position) + ".jpg");

        GlideApp.with(mContext)
                .load(sRef)
                .transform(new CircleCrop())
                .into(mViewHolder.mPhotos);

        return convertView;
    }

    static class ViewHolder {
        TextView mNames;
        TextView mEmails;
        ImageView mPhotos;
    }
}
