package com.mosisproject.mosisproject.filter;

import com.mosisproject.mosisproject.model.Event;

import java.util.ArrayList;

public class FilterHelper {
    private static final FilterHelper ourInstance = new FilterHelper();
    private ArrayList<Event> events = new ArrayList<>();
    private ArrayList<Filter> filters = new ArrayList<>();
    public static FilterHelper getInstance() {
        return ourInstance;
    }

    private FilterHelper() {
    }

    public void setEvents(ArrayList<Event> e)
    {
        events = e;
    }

    public void setFilters(ArrayList<Filter> f)
    {
        filters = f;
    }

    public ArrayList<Event> FilterEvents()
    {
        if (filters.size() == 0) return  events;

        ArrayList<Event> newList = new ArrayList<>();
        for (Event event: events) {
            if (CheckEvent(event))
            {
                newList.add(event);
            }
        }
        return newList;
    }

    private boolean CheckEvent(Event event) {
        for (Filter filter: filters) {
           if (!filter.Filter(event)) return false;
        }
        return true;
    }
}
