package com.joselestnh.calendar;

import java.util.List;

public class ScheduleItem {

    final static int SEPARATOR = 0;
    final static int TASK = 1;

    private int layout;
    private Task task;
    private int mode;
    private long startTime;
    private long endTime;

    public ScheduleItem(Task task){
        this.layout = R.layout.task_layout;
        this.task = task;
        this.startTime = task.getStart_time();
        this.endTime = task.getEnd_time();
        this.mode = ScheduleItem.TASK;


    }
    public ScheduleItem(Task initialTask, Task finalTask){
        this.layout = R.layout.separator_layout;
        this.task = null;
        this.startTime = initialTask.getEnd_time();
        this.endTime = finalTask.getStart_time();
        this.mode = ScheduleItem.SEPARATOR;

    }

    public ScheduleItem(long startTime, long endTime){
        this.layout = R.layout.separator_layout;
        this.task = null;
        this.startTime = startTime;
        this.endTime = endTime;
        this.mode = ScheduleItem.SEPARATOR;
    }

    public int getLayout() {
        return layout;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
