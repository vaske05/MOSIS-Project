package com.mosisproject.mosisproject.filter;

import com.mosisproject.mosisproject.model.Event;

import java.util.List;

public class FriendsFilter implements Filter {

    private String[] friends;

    public FriendsFilter(String f)
    {
        friends = f.split(",");
    }

    @Override
    public boolean Filter(Event event) {
        for (int i = 0; i < friends.length; i++)
        {
            if (!event.friendNames.contains(friends[i].trim())) return false;
        }
        return true;
    }
}
