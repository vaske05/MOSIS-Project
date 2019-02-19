package com.mosisproject.mosisproject.model;

public class Filters {
    private static final Filters ourInstance = new Filters();

    public static Filters getInstance() {
        return ourInstance;
    }

    private Filters() {
    }

    public boolean isRestaurantChecked = false;
    public boolean isTavernChecked = false;
    public boolean isCoffeeChecked = false;
    public String SelectedDate = "";
    public String SelectedTime = "";
    public String Friends;
}
