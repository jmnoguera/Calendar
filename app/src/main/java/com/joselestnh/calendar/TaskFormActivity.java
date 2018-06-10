package com.joselestnh.calendar;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;

public class TaskFormActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    public final static String TASK_NAME = "calendar.task.name";
    public final static String TASK_START_TIME = "calendar.task.start_time";
    public final static String TASK_END_TIME = "calendar.task.end_time";
    public final static String TASK_COLOR = "calendar.task.color";

    private String taskName;
    private LocalDate date;
    private LocalTime minTime;
    private LocalTime startTime;
    private LocalTime maxTime;
    private LocalTime endTime;
    private int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_form);

        Intent intent = getIntent();
        int[] dateNumbers = intent.getIntArrayExtra(TaskActivity.DATE_NUMBERS);
        date = LocalDate.of(dateNumbers[0],dateNumbers[1],dateNumbers[2]);
        long[] timeRange = intent.getLongArrayExtra(TaskActivity.TIME_RANGE);
        minTime = LocalTime.ofSecondOfDay(timeRange[0]);
        maxTime = LocalTime.ofSecondOfDay(timeRange[1]);

        ((TextView)findViewById(R.id.dayNameAtTask)).setText(date.toString());

        findViewById(R.id.startTimeEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalTime now = LocalTime.now();
                TimePickerDialog dpd = TimePickerDialog.newInstance(
                        TaskFormActivity.this,
                        now.getHour(),
                        now.getMinute(),
                        true);
                dpd.setMinTime(new Timepoint(minTime.getHour(),minTime.getMinute()));
                if(endTime!= null && (maxTime.isAfter(endTime))){
                    dpd.setMaxTime(new Timepoint(endTime.getHour(), endTime.getMinute()));
                }else{
                    dpd.setMaxTime(new Timepoint(maxTime.getHour(),maxTime.getMinute()));
                }
                dpd.show(getFragmentManager(), "StartTimePicker");
            }
        });

        findViewById(R.id.endTimeEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalTime now = LocalTime.now();
                TimePickerDialog dpd = TimePickerDialog.newInstance(
                        TaskFormActivity.this,
                        now.getHour(),
                        now.getMinute(),
                        true);
                if(startTime!= null && (minTime.isBefore(startTime))){      //startTime>=minTime
                    dpd.setMinTime(new Timepoint(startTime.getHour(), startTime.getMinute()));
                }else{
                    dpd.setMinTime(new Timepoint(minTime.getHour(),minTime.getMinute()));
                }
                dpd.setMaxTime(new Timepoint(maxTime.getHour(),maxTime.getMinute()));

                dpd.show(getFragmentManager(), "EndTimePicker");
            }
        });

        findViewById(R.id.colorButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialogBuilder
                        .with(TaskFormActivity.this)
                        .setTitle("Choose color")
                        .initialColor(ContextCompat.getColor(TaskFormActivity.this,R.color.colorPrimary))
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                Toast.makeText(TaskFormActivity.this,
                                        "onColorSelected: 0x" +
                                                Integer.toHexString(selectedColor),
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                //changeBackgroundColor(selectedColor);
                                color = selectedColor;
                                findViewById(R.id.colorButton).setBackgroundColor(selectedColor);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();
            }
        });

        findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });
    }

    void saveTask(){
        String name = ((EditText)findViewById(R.id.taskNameInput)).getText().toString();

        if(name.isEmpty() || startTime == null || endTime == null){
            Toast.makeText(this,"Some fields are not set",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(TaskFormActivity.TASK_NAME,name);
        intent.putExtra(TaskFormActivity.TASK_START_TIME, Long.valueOf(startTime.toSecondOfDay()));
        intent.putExtra(TaskFormActivity.TASK_END_TIME, Long.valueOf(endTime.toSecondOfDay()));
        intent.putExtra(TaskFormActivity.TASK_COLOR, color);

        setResult(TaskActivity.CORRECT_TASK, intent);
        finish();
    }


    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        switch (view.getTag()){
            case "StartTimePicker":
                startTime = LocalTime.of(hourOfDay,minute);
                ((TextView)findViewById(R.id.startTimeEdit)).setText(startTime.toString());
                break;
            case "EndTimePicker":
                endTime = LocalTime.of(hourOfDay,minute);
                ((TextView)findViewById(R.id.endTimeEdit)).setText(endTime.toString());

        }
    }
}
