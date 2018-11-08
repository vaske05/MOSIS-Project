package com.mosisproject.mosisproject.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String name;
    public String surname;
    public String phone;
    public String email;
    public List<String> friendsList;

    public User() {
    }

    public User(String name, String surname, String email, String phone, List<String> friends) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.friendsList = friends;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
