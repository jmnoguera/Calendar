package com.joselestnh.calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    final static int NUMBER_OF_DAYS = 42;
    final static float SWIPE_DISTANCE = 50;
    public final static int NOTIFICATION_CODE = 230;
    public final static String CHANNEL_ID = "CALENDAR_CHANNEL";

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
        refreshAlarm(this);

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


    public static void refreshAlarm(Context context){

        //comprobar si habia alarma antes de saltar

        final Calendar calendar = Calendar.getInstance();
        List<Day> days;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<Day>> result = executorService.submit(new Callable<List<Day>>() {
            @Override
            public List<Day> call() throws Exception {
                return MainActivity.getDb().dayDao().getNextDays(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
            }
        });

        try{
            days = result.get();
        }catch (Exception e){
            days = null;
        }
        executorService.shutdown();
        if(days == null){
            return;
        }
        Collections.sort(days);

        Long taskTime = getSoonestTaskTime(days);

        if(taskTime == null){
            return;
        }

        //poner alarma



        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(TaskFormActivity.TASK_START_TIME, LocalDateTime.ofEpochSecond(taskTime/1000,0, OffsetDateTime.now().getOffset()).toLocalTime().toString());
        PendingIntent broadcast = PendingIntent.getBroadcast(context, NOTIFICATION_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

//        Calendar calendar1 = Calendar.getInstance();
//        calendar1.add(Calendar.SECOND,5);
//        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar1.getTimeInMillis(), broadcast);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, taskTime, broadcast);



//        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
//                .setContentTitle("Alarm from"+R.string.app_name)
//                .setContentText("Time to do the scheduled task")
//                .setAutoCancel(true)
//                .setSmallIcon(R.drawable.ball).build();
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(0, notification);



    }

    private static Long getSoonestTaskTime(@NonNull final List<Day> daysList){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Long> result = executorService.submit(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                AppDatabase db = MainActivity.getDb();
                int now = LocalTime.now().toSecondOfDay();
                List<Task> taskList;
                for (Day day : daysList){

                    taskList = db.taskDao().getTaskByDay(day.getDid());
                    Collections.sort(taskList);
                    for (Task t : taskList){
                        if(now < t.getStart_time()){
                            return LocalDateTime.of(LocalDate.of(day.getYear(),day.getMonth(),
                                    day.getDay()),LocalTime.ofSecondOfDay(t.getStart_time())).
                                    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        }
                    }
                }
                return null;
            }

        });
        Long taskTime;
        try{
            taskTime = result.get();
        } catch (Exception e) {
            taskTime = null;
        }

        return taskTime;
    }
}
