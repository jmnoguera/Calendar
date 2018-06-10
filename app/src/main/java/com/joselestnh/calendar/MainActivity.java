package com.joselestnh.calendar;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    final static int NUMBER_OF_DAYS = 42;
    final static float SWIPE_DISTANCE = 50;

    private static AppDatabase db;
    GridView gridView;
    DayGridAdapter adapter;

    Calendar calendar;
    int currentMonth;
    int currentYear;
    List<LocalDate> daysList;

    private float x1;
    private float x2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = Room.databaseBuilder(getApplicationContext(),AppDatabase.class,
                "Calendar-DB").build();

        calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);    //Jan == 0
        currentYear = calendar.get(Calendar.YEAR);
        updateDaysList();

        gridView = this.findViewById(R.id.monthGrid);
        adapter = new DayGridAdapter(MainActivity.this, daysList);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                Intent intent = new Intent(MainActivity.this, TaskActivity.class);
                int[] dateNumbers = {daysList.get(position).getYear(),
                    daysList.get(position).getMonthValue(),
                    daysList.get(position).getDayOfMonth()};
                intent.putExtra(TaskActivity.DATE_NUMBERS,dateNumbers);
                startActivity(intent);


            }
        });


        ImageButton previousButton = findViewById(R.id.previousMonth);
        final ImageButton nextButton = findViewById(R.id.nextMonth);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousMonth();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMonth();
            }
        });

        //onGestos
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateDaysList();
    }

    //could be enhanced
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                return true;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                if(x2>x1 && x2-x1>SWIPE_DISTANCE){
                    previousMonth();
                }else if(x1>x2 && x1-x2>SWIPE_DISTANCE){
                    nextMonth();
                }
                return true;
        }
        return super.onTouchEvent(event);

    }

    void nextMonth(){
        calendar.add(Calendar.MONTH, 1);
        updateDaysList();
    }

    void previousMonth(){
        calendar.add(Calendar.MONTH, -1);
        updateDaysList();
    }

    void updateMonthTag(){
        //fix
        String tag = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH)+ " "+
                calendar.get(Calendar.YEAR);

        ((TextView)findViewById(R.id.monthName)).setText(tag);

    }
    // https://goo.gl/hJJeuE
    void updateDaysList(){
        daysList = new ArrayList<>();
        Calendar mCal = (Calendar)calendar.clone();
        mCal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfTheMonth;
        //dirty fix to US calendar
        if(mCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            firstDayOfTheMonth = 6;
        }else{
            firstDayOfTheMonth = mCal.get(Calendar.DAY_OF_WEEK) -2;
        }
        mCal.add(Calendar.DAY_OF_MONTH, -firstDayOfTheMonth);
        while(daysList.size() < NUMBER_OF_DAYS){
            daysList.add(mCal.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            mCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (adapter != null) {
            adapter.updateData(daysList);
        }
        updateMonthTag();
    }


    public static AppDatabase getDb() {
        return db;
    }
}
