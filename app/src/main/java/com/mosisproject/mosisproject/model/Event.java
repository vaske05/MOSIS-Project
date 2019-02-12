package com.mosisproject.mosisproject.model;

import java.util.ArrayList;
import java.util.List;

public class Event {
    public String description;
    public String placeName;
    public String eventId;
    public List<String> friendNames;
    public double longitude;
    public double latitude;

    public Event() {
        friendNames = new ArrayList<>();
    }

    public String getEventId() {
        eventId = new StringBuilder()
                .append(placeName)
                .append(longitude)
                .append(latitude)
                .toString();
        return eventId;
    }

    public List<String> getFriendNames() {
        return friendNames;
    }
    public Event(String placeName) {
        this.placeName = placeName;
    }

    public String getDescription() {
        return description;
    }

    public void addFriendName(String name) {
        friendNames.add(name);
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
