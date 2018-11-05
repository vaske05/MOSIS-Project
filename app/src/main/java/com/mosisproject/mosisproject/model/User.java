package com.mosisproject.mosisproject.model;

import java.util.List;

public class User {
    public String name;
    public String surname;
    public String phone;
    public String email;
    public List<String> friendsList;

    public User() {
    }

    public User(String name, String surname, String email, String phone) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
    }

    public List<String> getFriendsList() {
        return friendsList;
    }

    public void setFriendsList(List<String> friendsList) {
        this.friendsList = friendsList;
    }

    public void addFriend(String friend) {
        this.friendsList.add(friend);
    }
}
