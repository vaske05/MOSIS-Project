package com.mosisproject.mosisproject.filter;

import com.mosisproject.mosisproject.model.Event;

import java.util.List;

public class LocationTypeFilter implements Filter{

    private List<Event.LocationType> mLocationTypes;

    public LocationTypeFilter(List<Event.LocationType> locationType)
    {
        mLocationTypes = locationType;
    }

    public boolean Filter(Event event) {
        for (int i = 0; i < mLocationTypes.size(); i++)
        {
            if (event.locationType == mLocationTypes.get(i)) return true;
        }

        return false;
    }
}
