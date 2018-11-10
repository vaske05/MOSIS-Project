package com.mosisproject.mosisproject.service;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mosisproject.mosisproject.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserService {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    String userId;
    List<String> userIdList = new ArrayList<>();
    List<User> userList = new ArrayList<>();



    public UserService() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userId = firebaseUser.getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();

    }

    public List<String> getFriendsIds(final String userId) {
        firebaseDatabase.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.child("Users").child(userId).getValue(User.class);
                userIdList = new ArrayList<>(user.getFriendsList());
                userIdList.remove(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("GET USER ID LIST ERROR:", databaseError.getMessage());

            }

        });
        return userIdList;
    }

    public List<User> getFreinds(final String userId) {

        userIdList = getFriendsIds(userId);
        for(int i = 0; i < userList.size(); i++) {
            firebaseDatabase.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final User user = dataSnapshot.child("Users").child(userId).getValue(User.class);
                    if(user != null) {
                        userList.add(user);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("GET USER LIST ERROR:", databaseError.getMessage());
                }
            });
        }
        return userList;
    }


}
