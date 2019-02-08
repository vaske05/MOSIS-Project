package com.mosisproject.mosisproject.model;

import android.util.Log;

import java.util.Date;

public class Friend {

    public String friendId;
    public String lastUpdate;

    public Friend()
    {
        lastUpdate = new Date().toLocaleString();
        friendId = "";
    }

    public Friend(String id)
    {
        friendId = id;
        lastUpdate = new Date().toLocaleString();

    }

    public String getFriendId() {
        return friendId;
    }


}
