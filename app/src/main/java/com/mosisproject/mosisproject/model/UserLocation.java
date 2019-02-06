package com.mosisproject.mosisproject.model;

import android.util.Log;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UserLocation {

    private static final String TAG = UserLocation.class.getSimpleName();


    private double latitude;
    private double longitude;
    private Date dateTime;

    public UserLocation() {
        dateTime = new Date();
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

    public Date getDateTime() { return dateTime; }

    public void setDateTime(Date dateTime) { this.dateTime = dateTime;}

    public boolean ShouldUpdate(Date oldDate)
    {
        if (dateTime == null) return true;

        Date newDate = new Date();
        long diffInMilisec = Math.abs(newDate.getTime() - oldDate.getTime());
        long diffSeconds = diffInMilisec / 1000 % 60;
        long diffHours = diffInMilisec / (60 * 60 * 1000) % 24;

        Log.i(TAG, "ShouldUpdate: " + diffSeconds);
        if (diffSeconds > 5) {
            return true;
        }

        return false;
    }

}
