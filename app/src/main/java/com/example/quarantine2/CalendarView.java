package com.example.quarantine2;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * Created by a7med on 28/06/2015.
 */
public class CalendarView extends LinearLayout
{

    // for logging
    private static final String LOGTAG = "Calendar View";

    // how many days to show, defaults to six weeks, 42 days
    private static final int DAYS_COUNT = 42;

    // default date format
    private static final String DATE_FORMAT = "MMM yyyy";

    // date format
    private String dateFormat;

    // current displayed month
    private Calendar currentDate = Calendar.getInstance();

    //event handling
    private EventHandler eventHandler = null;

    // internal components
    private LinearLayout header;
    private TextView txtDate;
    private GridView grid;
    private TextView quarantineText;

    //Quarantine end date
    private Date quarantineEndDate;

    //bool for switching grid size
    private boolean gridSize;

    public CalendarView(Context context)
    {
        super(context);
    }

    public CalendarView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initControl(context, attrs);

    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initControl(context, attrs);

    }

    /**
     * Load control xml layout
     */
    private void initControl(Context context, AttributeSet attrs)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.control_calendar, this);
        gridSize = false;
        loadDateFormat(attrs);
        assignUiElements();
        assignClickHandlers();
        updateCalendar();
    }

    private void loadDateFormat(AttributeSet attrs)
    {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CalendarView);

        try
        {
            // try to load provided date format, and fallback to default otherwise
            dateFormat = ta.getString(R.styleable.CalendarView_dateFormat);
            if (dateFormat == null)
                dateFormat = DATE_FORMAT;
        }
        finally
        {
            ta.recycle();
        }
    }

    private void assignUiElements()
    {
        // layout is inflated, assign local variables to components
        header = (LinearLayout)findViewById(R.id.calendar_header);
        txtDate = (TextView)findViewById(R.id.calendar_date_display);
        grid = (GridView)findViewById(R.id.calendar_grid);
        quarantineText = (TextView)findViewById(R.id.calendar_date_display);
    }

    private void assignClickHandlers()
    {

        // long-pressing a day
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {

            @Override
            public boolean onItemLongClick(AdapterView<?> view, View cell, int position, long id)
            {
                // handle long-press
                if (eventHandler == null)
                    return false;

                eventHandler.onDayLongPress((Date)view.getItemAtPosition(position));
                return true;
            }
        });

    }

    public void setGridSize(boolean bool){
        gridSize = bool;
    }

    /**
     * Display dates correctly in grid
     */
    public void updateCalendar()
    {
        updateCalendar(null);
    }

    /**
     * Display dates correctly in grid
     */
    public void updateCalendar(HashSet<Date> events)
    {
        ArrayList<Date> cells = new ArrayList<>();
        Calendar calendar = (Calendar)currentDate.clone();

        if(gridSize) {
            // determine the cell for current month's beginning
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            // move calendar backwards to the beginning of the week
            calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

            // fill cells
            while (cells.size() < DAYS_COUNT) {
                cells.add(calendar.getTime());
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // update grid
            grid.setAdapter(new CalendarAdapter(getContext(), cells, events));

            //adjust height of grid
            setGridViewHeightBasedOnChildren(grid, 7);
        }else{
            // determine the cell for current month's beginning
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            //get the current dates position within the month
            int temp = (currentDate.getTime().getDate() / 7);
            temp = temp*7 - 7;
            //shift to begginning + multiple of 7 based on day of month
            calendar.add(Calendar.DAY_OF_MONTH, (-monthBeginningCell + temp));


            // fill cells
            while (cells.size() < 21) {
                cells.add(calendar.getTime());
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // update grid
            grid.setAdapter(new CalendarAdapter(getContext(), cells, events));

            //adjust height of grid
            setGridViewHeightBasedOnChildren(grid, 7);
        }
    }

    /**
    * automatically adjust the height of a grid based on the contents and the number of columns
     */
    public void setGridViewHeightBasedOnChildren(GridView gridView, int columns) {
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int items = listAdapter.getCount();
        int rows = 0;

        if(items==0)
            return;
        View listItem = listAdapter.getView(0, null, gridView);
        listItem.measure(0, 0);
        totalHeight = listItem.getMeasuredHeight()+20; //(padding)

        float x = 1;
        if( items > columns ){
            x = items/columns;

            if(items%columns==0)
                rows=(int) x;
            else
                rows = (int) (x + 1);

            totalHeight *= rows;

        }
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);

    }

    /**
    *Set the the quarantine timer text resource based on a date
     */
    public void setQuarantineTimer(Date date){
        if (date.getTime() > currentDate.getTime().getTime()) {
            quarantineEndDate = date;
            long difference = date.getTime() - currentDate.getTime().getTime();
            long days = TimeUnit.MILLISECONDS.toDays(difference);
            long totalHours = TimeUnit.MILLISECONDS.toHours(difference);
            totalHours = totalHours - days * 24;
            quarantineText.setText("" + days + " days " + totalHours + " hrs");
            quarantineText.setTextSize(50);
            quarantineText.setTextColor(Color.WHITE);
        }else{
            quarantineText.setText("Your Quarantine is over");
            quarantineText.setTextSize(30);
            quarantineText.setTextColor(getResources().getColor(R.color.summer));
        }

    }

    /**
     *  Deals with the calander grid and sets the contents
     * **/
    private class CalendarAdapter extends ArrayAdapter<Date>
    {
        // days with events
        private HashSet<Date> eventDays;

        // for view inflation
        private LayoutInflater inflater;

        CalendarAdapter(Context context, ArrayList<Date> days, HashSet<Date> eventDays)
        {
            super(context, R.layout.control_calendar_day, days);
            this.eventDays = eventDays;
            inflater = LayoutInflater.from(context);

        }

        @Override
        public View getView(int position, View view, ViewGroup parent)
        {
            // day in question
            Date date = getItem(position);
            int day = date.getDate();
            int month = date.getMonth();
            int year = date.getYear();

            // today
            Date today = new Date();

            // inflate item if it does not exist yet
            if (view == null)
                view = inflater.inflate(R.layout.control_calendar_day, parent, false);

            // if this day has an event, specify event image
            view.setBackgroundResource(R.mipmap.solid);
            if (eventDays != null)
            {
                for (Date eventDate : eventDays)
                {
                    if (eventDate.getDate() == day &&
                            eventDate.getMonth() == month &&
                            eventDate.getYear() == year)
                    {
                        // mark this day for event
                        //view.setBackgroundResource(R.drawable.ic_stat_name);
                        //view.setBackgroundColor(Color.YELLOW);
                        break;
                    }
                }
            }

            // clear styling
            ((TextView)view).setTypeface(null, Typeface.NORMAL);
            ((TextView)view).setTextColor(Color.WHITE);


            if (month != today.getMonth() || year != today.getYear())
            {
                // if this day is outside current month, grey it out
                ((TextView)view).setTextColor(getResources().getColor(R.color.greyed_out));
            }
            else if (day == today.getDate())
            {
                // if it is today, set it to blue/bold
                ((TextView)view).setTypeface(null, Typeface.BOLD);

                if(quarantineEndDate != null) {

                    view.setBackgroundResource(R.mipmap.currentdate);

                }
                view.setBackgroundResource(R.mipmap.circle2);

            }

            //check if any days have passed in the quarantine period then set the colour and strike text
            if(quarantineEndDate != null) {
                long diffInMillies = Math.abs(quarantineEndDate.getTime() - date.getTime());
                if (quarantineEndDate.getTime() > date.getTime() && TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) < 14 && date.getTime() < currentDate.getTime().getTime()) {

                    view.setBackgroundResource(R.mipmap.minus);
                    ((TextView)view).setTextColor(Color.WHITE);
                    ((TextView)view).setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);

                }
            }

            if(quarantineEndDate != null) {
                long diffInMillies = Math.abs(quarantineEndDate.getTime() - date.getTime());
                if (quarantineEndDate.getTime() > date.getTime() && TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) < 14 && date.getTime() > currentDate.getTime().getTime()) {

                    view.setBackgroundResource(R.mipmap.reddot);
                    ((TextView)view).setTextColor(Color.WHITE);

                }
            }



            // set text unless its first day out of quarintine then display the padlock and skip text
            if(quarantineEndDate != null) {

                long diffInMillies = Math.abs(quarantineEndDate.getTime() - date.getTime());
                if (quarantineEndDate.getTime() < date.getTime() && TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) == 0) {

                    view.setBackgroundResource(R.mipmap.padlock2);
                    ((TextView)view).setTextColor(Color.WHITE);
                    ((TextView)view).setText("");

                }else{

                    ((TextView)view).setText(String.valueOf(date.getDate()));

                }
            }else{

                ((TextView)view).setText(String.valueOf(date.getDate()));

            }

            return view;
        }
    }



    /**
     * Assign event handler to be passed needed events
     */
    public void setEventHandler(EventHandler eventHandler)
    {
        this.eventHandler = eventHandler;
    }

    /**
     * This interface defines what events to be reported to
     * the outside world
     */
    public interface EventHandler
    {
        void onDayLongPress(Date date);
        //void onClick(View view);
    }

}
