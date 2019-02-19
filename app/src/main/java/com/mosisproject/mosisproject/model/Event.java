package com.mosisproject.mosisproject.model;

import android.content.res.Resources;
import android.location.Location;

import com.mosisproject.mosisproject.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class Event {
    public String description;
    public String placeName;
    public String eventId;
    public Date dateCreated;
    public List<String> friendNames;
    public double longitude;
    public double latitude;
    public LocationType locationType;


    public enum LocationType {
        RESTAURANT,
        TAVERN,
        COFFEE_SHOP
    }

    public Event() {
        friendNames = new ArrayList<>();
    }

    public String getEventId() {
        eventId = new StringBuilder()
                .append(getTitle())
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

    public Date getDateCreated() {return dateCreated; }
    public String getDescription() {
        return description;
    }
    public String getTitle()
    {
        return new StringBuilder()
                .append(LocationTypeToString())
                .append(": ")
                .append(placeName)
                .toString();
    }

    private String LocationTypeToString()
    {
        String location = "Restaurant";

        if (locationType == Event.LocationType.TAVERN) {
            location = "Tavern";
        }
        else if (locationType == Event.LocationType.COFFEE_SHOP)
        {
            location = "Coffee shop";
        }
        return location;
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

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

}
