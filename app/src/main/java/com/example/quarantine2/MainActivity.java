package com.example.quarantine2;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    //variables for activity

    //Our main view/class
    CalendarView cv;

    //variables for Time/Date Picker
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;
    private Calendar c;
    private Context ctx = this;

    //ui elements
    private Button setQuarantineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialise variables

        //button click code
        setQuarantineButton = findViewById(R.id.buttonSetQuarantine);
        setQuarantineButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dateTimePicker();
            }
        });

        //set events code (unused currently)
        HashSet<Date> events = new HashSet<>();
        events.add(new Date());

        //instantiate the calender view object
        cv = findViewById(R.id.calendar_view);
        //get the rootview object from our calendar view then set the background color
        View root = cv.getRootView();
        root.setBackgroundColor(getResources().getColor(R.color.spring));


        //chack for the calendar the saved state of the calender (true for full 42 days, false for 21 days
        SharedPreferences calendarSP = getSharedPreferences("calendarState", MODE_PRIVATE);
        boolean calendar = calendarSP.getBoolean("calendarState", false);
        cv.setGridSize(calendar);

        //update the calendar
        cv.updateCalendar(events);

        //check for and set Quarantine date in shared preferences
        SharedPreferences sp = getSharedPreferences("QuarantineDate", MODE_PRIVATE);
        long result = sp.getLong("QuarantineDate", 1);
        if (result != 1) {
            cv.setQuarantineTimer(new Date(result));
        }

        // assign event handler
        cv.setEventHandler(new CalendarView.EventHandler() {
            @Override
            public void onDayLongPress(Date date) {
                // show returned day
                DateFormat df = SimpleDateFormat.getDateInstance();
                Toast.makeText(MainActivity.this, df.format(date), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void dateTimePicker() {
        mYear = Calendar.getInstance().get(Calendar.YEAR);
        mMonth = Calendar.getInstance().get(Calendar.MONTH);
        mDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        mHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        mMinute = Calendar.getInstance().get(Calendar.MINUTE);

        c = Calendar.getInstance();
        int mYearParam = mYear;
        int mMonthParam = mMonth;
        int mDayParam = mDay;

        DatePickerDialog datePickerDialog = new DatePickerDialog(ctx,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        monthOfYear++;
                        showTimepicker(year, monthOfYear, dayOfMonth);
                    }
                }, mYearParam, mMonthParam, mDayParam);

        datePickerDialog.show();
    }

    private void showTimepicker(final int year, final int monthOfYear, final int dayOfMonth) {


        TimePickerDialog timePickerDialog = new TimePickerDialog(ctx,
                new TimePickerDialog.OnTimeSetListener() {

                    //runs after user selects time
                    @Override
                    public void onTimeSet(TimePicker view, int pHour,
                                          int pMinute) {

                        //set up a new date
                        Date date = new Date();
                        date.setYear(year - 1900);
                        date.setDate(dayOfMonth);
                        date.setMonth(monthOfYear - 1);
                        date.setHours(pHour);
                        date.setMinutes(pMinute);

                        boolean flag = true;

                        //check if the date is in the future, if not send user message and recall the dateTimePicker
                        if (date.compareTo(Calendar.getInstance().getTime()) < 0 || date.compareTo(Calendar.getInstance().getTime()) == 0) {
                            //Toast.makeText(MainActivity.this, "Please select a date in the future", Toast.LENGTH_LONG).show();

                            //AlertDialog to display error message to user
                            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                            alertDialog.setTitle("Date is in the past");
                            alertDialog.setMessage("Please select a date in the future");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                            dateTimePicker();
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                            flag = false;

                        }

                        //check if the date is within 14 days, if not send user message and recall the dateTimePicker
                        long diffInMillies = Math.abs(date.getTime() - Calendar.getInstance().getTime().getTime());
                        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                        if (diff > 14) {
                            //Toast.makeText(MainActivity.this, "The date must be within 14 days of today", Toast.LENGTH_LONG).show();

                            //AlertDialog to display the error to the user
                            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                            alertDialog.setTitle("Date is too far in the future");
                            alertDialog.setMessage("Please select a date within 14 days of the current date " + Calendar.getInstance().getTime().toString());
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dateTimePicker();
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();

                            flag = false;
                        }

                        //if everything is all right do this
                        if (flag) {
                            //set countdowntimer
                            cv.setQuarantineTimer(date);
                            //update the calendar
                            cv.updateCalendar();

                            SharedPreferences sp = getSharedPreferences("QuarantineDate", MODE_PRIVATE);
                            SharedPreferences.Editor edit = sp.edit();
                            edit.putLong("QuarantineDate", date.getTime());
                            edit.apply();

                        }
                    }
                }, mHour, mMinute, true);

        timePickerDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*
    * Menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.fullMonth) {
            cv.setGridSize(true);
            cv.updateCalendar();
            //Store users choice
            SharedPreferences sp = getSharedPreferences("calendarState", MODE_PRIVATE);
            SharedPreferences.Editor edit = sp.edit();
            edit.putBoolean("calendarState", true);
            edit.apply();
            return true;
        }
        if (id == R.id.halfMonth) {
            cv.setGridSize(false);
            cv.updateCalendar();
            //store users choice
            SharedPreferences sp = getSharedPreferences("calendarState", MODE_PRIVATE);
            SharedPreferences.Editor edit = sp.edit();
            edit.putBoolean("calendarState", false);
            edit.apply();
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_exit) {
            System.exit(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
