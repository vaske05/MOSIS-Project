package com.mosisproject.mosisproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mosisproject.mosisproject.model.User;
import com.mosisproject.mosisproject.service.UserService;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    Button buttonFindFriend;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    String userId;
   // List<User> userList = new ArrayList<>();
    List<String> userIdList = new ArrayList<>();
    List<User> userList = new ArrayList<>();
    UserService userService;
    ListView listViewFriends;
    private ArrayAdapter<String> adapter;
    private FriendsAdapter friendsAdapter;
    private ProgressBar spinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {



        View view = inflater.inflate(R.layout.fragment_friends, container,false);
        buttonFindFriend = (Button) view.findViewById(R.id.buttonFindFriend);
        listViewFriends = (ListView) view.findViewById(R.id.friendList);
        spinner = (ProgressBar) view.findViewById(R.id.spinner);


        userService = new UserService();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userId = firebaseUser.getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        //adapter = new ArrayAdapter<String>(container.getContext(), android.R.layout.simple_expandable_list_item_1, userList);

        friendsAdapter = new FriendsAdapter(container.getContext(), userList);

        listViewFriends.setAdapter(friendsAdapter);
        spinner.setVisibility(View.VISIBLE);
        getFriends(container);


        buttonFindFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBluetoothActivity();
            }
        });

        return view;
    }

    //Activity for finding new friend via bluetooth
    private void startBluetoothActivity() {
        startActivity(new Intent(getActivity(), BluetoothActivity.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        friendsAdapter.notifyDataSetChanged();
    }

    public void getFriends(final ViewGroup container) {
        firebaseDatabase.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                spinner.setVisibility(View.VISIBLE);
                final User user = dataSnapshot.child("Users").child(userId).getValue(User.class);
                userIdList = new ArrayList<>(user.getFriendsList());
                userIdList.remove(0);
                for(int i = 0; i < userIdList.size(); i++) {
                    User friend = dataSnapshot.child("Users").child(userIdList.get(i)).getValue(User.class);
                    if (friend != null) {
                        //userList.add(friend.getName() + " " + friend.getSurname() + "\n" + friend.getEmail());
                        userList.add(friend);
                    }
                }
                friendsAdapter = new FriendsAdapter(container.getContext(), userList);
                listViewFriends.setAdapter(friendsAdapter);
                friendsAdapter.notifyDataSetChanged();
                spinner.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("GET USER ID LIST ERROR:", databaseError.getMessage());

            }
        });
    }

    //TODO: Napravi Adapter klasu za novu friend list itema. Pogledaj: https://www.youtube.com/watch?v=q2XA0Pe2W04 , https://www.youtube.com/watch?v=ZEEYYvVwJGY

}
