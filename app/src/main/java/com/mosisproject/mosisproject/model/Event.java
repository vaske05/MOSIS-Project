package com.mosisproject.mosisproject.model;

import java.util.List;

public class Event {
    public String description;
    public String placeName;
    public List<String> attendersIds;
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

    public List<String> getAttendersIds() {
        return attendersIds;
    }

    public void setAttendersIds(List<String> attendersIds) {
        this.attendersIds = attendersIds;
    }

    public void addAttender(String id) {
        attendersIds.add(id);
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
