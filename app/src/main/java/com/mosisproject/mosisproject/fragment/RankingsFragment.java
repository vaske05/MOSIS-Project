package com.mosisproject.mosisproject.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.mosisproject.mosisproject.R;
import com.mosisproject.mosisproject.adapter.RankingsAdapter;
import com.mosisproject.mosisproject.model.Friend;
import com.mosisproject.mosisproject.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RankingsFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;

    private String userId;
    private List<Friend> userIdList = new ArrayList<>();
    private List<User> userList = new ArrayList<>();

    private ProgressBar spinner;

    private ListView listViewRankings;
    private RankingsAdapter rankingsAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.navigation_rankings);

        View view = inflater.inflate(R.layout.fragment_rankings, container, false);

        listViewRankings = (ListView) view.findViewById(R.id.rankingList);
        spinner = view.findViewById(R.id.spinner);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        userId = firebaseUser.getUid();
        rankingsAdapter = new RankingsAdapter(container.getContext(), userList);
        listViewRankings.setAdapter(rankingsAdapter);
        spinner.setVisibility(View.VISIBLE);
        loadRankings(container);

        return view;
    }

    private void loadRankings(final ViewGroup container) {
        firebaseDatabase.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //spinner.setVisibility(View.VISIBLE);
                final User user = dataSnapshot.child("Users").child(userId).getValue(User.class);
                userIdList = user.getFriendsList();
                if (userIdList == null) return;
                userIdList.remove(0);
                for(int i = 0; i < userIdList.size(); i++) {
                    User friend = dataSnapshot.child("Users").child(userIdList.get(i).getFriendId()).getValue(User.class);
                    if (friend != null) {
                        userList.add(friend);
                    }
                }
                userList = bubbleSort(userList); //Sorting list in descending order

                rankingsAdapter = new RankingsAdapter(container.getContext(), userList);
                listViewRankings.setAdapter(rankingsAdapter);
                rankingsAdapter.notifyDataSetChanged();
                spinner.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("GET USER ID LIST ERROR:", databaseError.getMessage());
                spinner.setVisibility(View.GONE);
            }

            private List<User> bubbleSort(@NonNull List<User> userList) {
                int size = userList.size();
                for(int i = 0; i < size-1; i++) {
                    for (int j = 0; j < size-1-i; j++) {
                        if(( userList.get(j).getPoints()) < ( userList.get(j+1).getPoints())) {
                            User tempUser = userList.get(j);
                            userList.set(j, userList.get(j+1));
                            userList.set(j+1, tempUser);
                        }
                    }
                }
                return userList;
            }
        });
    }
}
