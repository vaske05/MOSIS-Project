package com.mosisproject.mosisproject.filter;

import com.mosisproject.mosisproject.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeFilter implements Filter {

    private Date filterDate;

    public DateTimeFilter(Date date)
    {
        filterDate = date;
    }
    @Override
    public boolean Filter(Event event) {
        if (filterDate.before(event.dateCreated))
            return true;
        return false;
    }
}
