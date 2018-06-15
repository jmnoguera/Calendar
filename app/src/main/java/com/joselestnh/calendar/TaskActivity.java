package com.joselestnh.calendar;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskActivity extends AppCompatActivity {

    public final static String DAY_IDENTIFIER = "calendar.day.day_identifier";
    public final static String DATE_NUMBERS = "calendar.day.date";
    public final static String TIME_RANGE = "calendar.task.time_range";
    public final static int CREATE_TASK = 1;
    public final static int CORRECT_TASK = 2;

    ListView listView;
    ScheduleListAdapter adapter;
    private List<ScheduleItem> scheduleItems;
    private LocalDate date;
    private int[] dateNumbers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        Intent intent = getIntent();

        dateNumbers = intent.getIntArrayExtra(TaskActivity.DATE_NUMBERS);
        date = LocalDate.of(dateNumbers[0],dateNumbers[1],dateNumbers[2]);

        reloadSchedule();

        listView = findViewById(R.id.schedule);
        adapter = new ScheduleListAdapter(TaskActivity.this,scheduleItems);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(scheduleItems.get(position).getMode()){
                    case ScheduleItem.TASK:
                        break;
                    case ScheduleItem.SEPARATOR:
                        addTask(scheduleItems.get(position));
                        break;
                }

            }
        });


        findViewById(R.id.previousDay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousDay();
            }
        });

        findViewById(R.id.nextDay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextDay();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        reloadSchedule();
        MainActivity.refreshAlarm(this);
    }

    void addTask(ScheduleItem item){
        Intent intent = new Intent(this, TaskFormActivity.class);
        intent.putExtra(TaskActivity.DATE_NUMBERS,dateNumbers);
        long[] timeRange = {item.getStartTime(),item.getEndTime()};
        intent.putExtra(TaskActivity.TIME_RANGE, timeRange);
        startActivityForResult(intent,TaskActivity.CREATE_TASK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==TaskActivity.CREATE_TASK && resultCode==TaskActivity.CORRECT_TASK){
            final String name = data.getStringExtra(TaskFormActivity.TASK_NAME);
            final long startTime = data.getLongExtra(TaskFormActivity.TASK_START_TIME, -1);
            final long endTime = data.getLongExtra(TaskFormActivity.TASK_END_TIME, -1);
            final int color = data.getIntExtra(TaskFormActivity.TASK_COLOR,
                    ContextCompat.getColor(this,R.color.pureWhite));

            if(startTime!=-1 && endTime!=-1) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase db = MainActivity.getDb();
                        Integer did = db.dayDao().getDid(date.getYear(),
                                date.getMonthValue(), date.getDayOfMonth());
                        if(did == null){
                            db.dayDao().insertDays(new Day(date.getYear(),
                                    date.getMonthValue(), date.getDayOfMonth(),0));
                            did = db.dayDao().getDid(
                                    date.getYear(),date.getMonthValue(),date.getDayOfMonth());
                        }
                        db.taskDao().insertTask(
                                new Task(did, name, startTime, endTime, color));
                        db.dayDao().addNumberOfTasks(did,1);
                    }
                }).start();
            }
        }
    }

    void previousDay(){
        date = date.minusDays(1);
        reloadSchedule();
    }

    void nextDay(){
        date = date.plusDays(1);
        reloadSchedule();
    }

    void reloadSchedule(){
        ((TextView)findViewById(R.id.dayName)).setText(date.toString());


        List<Task> taskList;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<Task>> result = executorService.submit(new Callable<List<Task>>() {
            @Override
            public List<Task> call() throws Exception {
                Integer did = MainActivity.getDb().dayDao().getDid(date.getYear(),
                        date.getMonthValue(),date.getDayOfMonth());
                if(did != null){
                    return MainActivity.getDb().taskDao().getTaskByDay(did);
                }else{
                    return new ArrayList<>();
                }

            }
        });

        try {
            taskList = result.get();
        } catch (Exception e) {
            taskList = new ArrayList<>();
        }


        scheduleItems = new ArrayList<>();

        //supposing time ranges doesn't overlap
        if(!taskList.isEmpty()){
//            Collections.sort(taskList, new Comparator<Task>() {
//                @Override
//                public int compare(Task o1, Task o2) {
//                    return Long.compare(o1.getStart_time(),o2.getStart_time());
//                }
//            });

            Collections.sort(taskList);

            if(LocalTime.ofSecondOfDay(taskList.get(0).getStart_time()).isAfter(LocalTime.MIN)){
                scheduleItems.add(new ScheduleItem(LocalTime.MIN.toSecondOfDay(),
                        taskList.get(0).getStart_time()));
            }
            for (int i = 0; i < taskList.size(); i++) {
                scheduleItems.add(new ScheduleItem(taskList.get(i)));
                if (i != taskList.size() - 1) {
                    if(taskList.get(i).getEnd_time() != taskList.get(i+1).getStart_time()) {
                        scheduleItems.add(new ScheduleItem(taskList.get(i), taskList.get(i + 1)));
                    }
                }
            }
            if(LocalTime.ofSecondOfDay(taskList.get(taskList.size()-1).getStart_time()).
                    isBefore(LocalTime.of(23,59))){
                scheduleItems.add(new ScheduleItem(taskList.get(taskList.size()-1).getEnd_time(),
                        LocalTime.of(23,59).toSecondOfDay()));
            }
        }else{
            scheduleItems.add(new ScheduleItem(LocalTime.MIN.toSecondOfDay(),
                    LocalTime.of(23,59).toSecondOfDay()));
        }

        if(adapter!=null){
            adapter.updateData(scheduleItems);
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(scheduleItems.get(((AdapterView.AdapterContextMenuInfo) menuInfo).position).getMode()==ScheduleItem.TASK){
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.task_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()){
            case R.id.task_delete:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase db = MainActivity.getDb();
                        //delete the task
                        db.taskDao().deleteTask(scheduleItems.get(info.position).getTask());
                        int did = db.dayDao().getDid(
                                date.getYear(),date.getMonthValue(),date.getDayOfMonth());
                        //decrement number of task of the day
                        db.dayDao().addNumberOfTasks(did,-1);
                        //if there isn't any task the day is deleted
                        if(db.dayDao().getNumberOfTaskOf(did)<=0){
                            db.dayDao().deleteDays(db.dayDao().getDayBy(did));
                        }

                    }
                }).start();
//                recreate();

                Toast.makeText(this, "Task deleted",Toast.LENGTH_SHORT).show();
                reloadSchedule();
                return true;
            default:
                return super.onContextItemSelected(item);

        }

    }
}
