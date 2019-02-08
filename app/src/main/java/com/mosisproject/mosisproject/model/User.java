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
    public int points;
    public List<String> friendsList;
    public UserLocation userLocation = new UserLocation();
    public Icon markerIcon;
    public List<Event> eventList;

    public User() {
    }

    public User(String id, String name, String surname, String email, String phone, int points, List<String> friends, List<Event> events) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.points = points;
        this.friendsList = friends;
        this.eventList = events;
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

    public int getPoints() {
        return points;
    }

    public void addPoints() {
        this.points++;
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

    public List<Event> getEventList() {
        return eventList;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
    }

    public void addEvent(Event event) {
        this.eventList.add(event);
    }
}
