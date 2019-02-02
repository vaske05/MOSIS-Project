package com.mosisproject.mosisproject.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String id;
    public String name;
    public String surname;
    public String phone;
    public String email;
    public String points;
    public List<String> friendsList;
    public UserLocation userLocation;

    public User() {
    }

    public User(String id, String name, String surname, String email, String phone, String points, List<String> friends) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.points = points;
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

    public void removeFriend(String friend) {
        this.friendsList.remove(friend);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public UserLocation getUserLocation() {
        return userLocation;
    }

    public boolean setUserLocation(UserLocation location) {
        double latDiff = Math.abs(userLocation.latitude - location.latitude);
        double longDiff = Math.abs(userLocation.longitude - location.longitude);

        if (latDiff > 0.05 || longDiff > 0.05)
        {
            this.userLocation = location;
            return true;
        }
        return false;
    }
}
