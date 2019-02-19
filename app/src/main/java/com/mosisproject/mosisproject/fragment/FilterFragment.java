package com.mosisproject.mosisproject.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.mosisproject.mosisproject.R;
import com.mosisproject.mosisproject.activity.MainActivity;
import com.mosisproject.mosisproject.filter.DateTimeFilter;
import com.mosisproject.mosisproject.filter.Filter;
import com.mosisproject.mosisproject.filter.FilterHelper;
import com.mosisproject.mosisproject.filter.FriendsFilter;
import com.mosisproject.mosisproject.filter.LocationTypeFilter;
import com.mosisproject.mosisproject.model.Event;
import com.mosisproject.mosisproject.model.Filters;

import org.xml.sax.Locator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FilterFragment extends Fragment {

    private CheckBox cbTavern;
    private CheckBox cbRestaurant;
    private CheckBox cbCoffee;
    private EditText date;
    private EditText time;
    private EditText friends;
    private Button submitBtn;
    private ArrayList<Filter> filters = new ArrayList<>();
    private FilterHelper helper = FilterHelper.getInstance();
    private Filters model = Filters.getInstance();
    private Date dateTime;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getActivity().setTitle(R.string.filter);

        View view = inflater.inflate(R.layout.fragment_filter, container,false);

        cbTavern = view.findViewById(R.id.filter_tavern);
        cbRestaurant = view.findViewById(R.id.filter_restaurant);
        cbCoffee = view.findViewById(R.id.filter_coffee);
        time = view.findViewById(R.id.filter_time);
        date = view.findViewById(R.id.filter_date);
        submitBtn = view.findViewById(R.id.filter_submit_btn);
        friends = view.findViewById(R.id.filter_friends);

        submitBtn.setOnClickListener(onSubmitClicked);
        date.setOnClickListener(onDateClicked);
        time.setOnClickListener(onTimeClicked);

        Calendar cal = Calendar.getInstance();
        cal.set(1990, 1, 1);
        dateTime = cal.getTime();
        InitView();
        return view;
    }

    private void InitView() {
        cbRestaurant.setChecked(model.isRestaurantChecked);
        cbCoffee.setChecked(model.isCoffeeChecked);
        cbTavern.setChecked(model.isTavernChecked);
        time.setText(model.SelectedTime);
        date.setText(model.SelectedDate);
        friends.setText(model.Friends);
    }

    private void setFilters() {
        filters.clear();
        List<Event.LocationType> types = new ArrayList<>() ;
        if (cbTavern.isChecked()) types.add(Event.LocationType.TAVERN);
        if (cbCoffee.isChecked()) types.add(Event.LocationType.COFFEE_SHOP);
        if (cbRestaurant.isChecked()) types.add(Event.LocationType.RESTAURANT);

        if (types.size() > 0) filters.add(new LocationTypeFilter(types));
        if (!friends.getText().toString().isEmpty())filters.add(new FriendsFilter(friends.getText().toString()));
        filters.add(new DateTimeFilter(dateTime));

        SaveState();
        helper.setFilters(filters);
    }

    private void SaveState() {
        model.isCoffeeChecked = cbCoffee.isChecked();
        model.isRestaurantChecked = cbRestaurant.isChecked();
        model.isTavernChecked = cbTavern.isChecked();
        model.SelectedDate = date.getText().toString();
        model.SelectedTime = time.getText().toString();
        model.Friends = friends.getText().toString();
    }

    private View.OnClickListener onSubmitClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setFilters();
            ((MainActivity)getActivity()).openMapFragment();
        }

    };
    private View.OnClickListener onTimeClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DialogFragment newFragment = new TimePickerFragment();
            newFragment.setTargetFragment(FilterFragment.this,0);
            newFragment.show(getFragmentManager(), "timePicker");
        }
    };
    private View.OnClickListener onDateClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.setTargetFragment(FilterFragment.this,0);
            newFragment.show(getFragmentManager(), "datePicker");
        }
    };

    public void updateDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        dateTime = cal.getTime();

        String dateText = new StringBuilder()
                .append(day)
                .append("/")
                .append(month + 1)
                .append("/")
                .append(year)
                .toString();

        date.setText(dateText);
    }

    public void updateTime(int hourOfDay, int minute)
    {
        dateTime.setHours(hourOfDay);
        dateTime.setMinutes(minute);

        String hour = String.valueOf(hourOfDay);
        String min = String.valueOf(minute);
        if (hourOfDay % 12 < 10) hour = "0" + hour;
        if (minute < 10) min = "0" + min;

        String timeText = new StringBuilder()
                .append(hour)
                .append(":")
                .append(min)
                .toString();
        time.setText(timeText);
    }

}
