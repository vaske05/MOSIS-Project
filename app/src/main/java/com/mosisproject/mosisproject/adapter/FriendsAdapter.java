package com.mosisproject.mosisproject.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mosisproject.mosisproject.module.GlideApp;
import com.mosisproject.mosisproject.R;
import com.mosisproject.mosisproject.fragment.FriendsFragment;
import com.mosisproject.mosisproject.model.User;

import java.util.ArrayList;
import java.util.List;

public class FriendsAdapter extends ArrayAdapter<String> {

    Context mContext;
    FragmentManager mFragmentManager;
    List<User> userList = new ArrayList<>();
    List<String> emails = new ArrayList<>();
    List<String> names = new ArrayList<>();
    List<String> ids = new ArrayList<>();


    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    final List<Bitmap> photoList = new ArrayList<>();

    public FriendsAdapter(Context context, List<User> userList, FragmentManager fragmentManager) {
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

            convertView = mInflater.inflate(R.layout.friend_list_item, parent, false);

            mViewHolder.mEmails = (TextView) convertView.findViewById(R.id.friend_email);
            mViewHolder.mNames = (TextView) convertView.findViewById(R.id.friend_full_name);
            mViewHolder.mPhotos = (ImageView) convertView.findViewById(R.id.friend_image);
            mViewHolder.mButton = (Button) convertView.findViewById(R.id.friend_delete_btn);
            convertView.setTag(mViewHolder);
        }
        else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Confirmation popup with YES OR NO BUTTON
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                // Yes button clicked
                                databaseReference = FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child(firebaseAuth.getUid());

                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final User user = dataSnapshot.getValue(User.class);
                                        user.removeFriend(ids.get(position));
                                        databaseReference.setValue(user);
                                        refreshFriendsFragment(); // Refresh current fragment to see changes
                                        Toast.makeText(mContext, names.get(position) + " removed from your friends list", Toast.LENGTH_LONG)
                                                .show();
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                // No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Do you really want to remove friend " + names.get(position))
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
                        .show();
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
        Button mButton;
    }

    private void refreshFriendsFragment() {
        FriendsFragment friendsFragment = new FriendsFragment();
        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, friendsFragment,"fragment_friends").commit();
        String tag = friendsFragment.getTag();
        Log.w("TAG:", tag);
    }
}
