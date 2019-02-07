package com.mosisproject.mosisproject.model;

import android.util.Log;

import com.mapbox.mapboxsdk.annotations.Icon;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String id;
    public String name;
    public String surname;
    public String phone;
    public String email;
    public String points;
    public Friend friend;
    public List<Friend> friendsList;
    public UserLocation userLocation = new UserLocation();
    public Icon markerIcon;

    public User() {
    }

    public User(String id, String name, String surname, String email, String phone, String points, List<Friend> friends) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.points = points;
        this.friendsList = friends;
    }

    public List<Friend> getFriendsList() {
        return friendsList;
    }

    public void setFriendsList(List<Friend> friendsList) {
        this.friendsList = friendsList;
    }

    public void addFriend(String friendId) {
        Friend f = new Friend(friendId);
        this.friendsList.add(f);
    }

    public void removeFriend(String friend) {//
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

    public Icon getMarkerIcon() {
        return markerIcon;
    }

    public void setMarkerIcon(Icon markerIcon) {
        this.markerIcon = markerIcon;
    }

    public UserLocation getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(UserLocation location) {

        this.userLocation = location;
    }
}
