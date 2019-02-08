package com.mosisproject.mosisproject.model;

import java.util.List;

public class Event {
    public String description;
    public String placeName;
    public List<User> attendersList;
    public double longitude;
    public double latitude;

    public Event() {
    }

    public Event(String placeName) {
        this.placeName = placeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public List<User> getAttendersList() {
        return attendersList;
    }

    public void setAttendersList(List<User> attendersList) {
        this.attendersList = attendersList;
    }

    public void addAttender(User user) {
        attendersList.add(user);
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
