package com.joselestnh.calendar;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = Day.class,
                                    parentColumns = "did",
                                    childColumns = "did",
                                    onDelete = CASCADE))
public class Task  implements Comparable{

    @PrimaryKey(autoGenerate = true)
    private int tid;

    @ColumnInfo(name = "did")
    private int did;

    @ColumnInfo(name = "task_name")
    private String task_name;

    @ColumnInfo(name = "start_time")
    private long start_time;

    @ColumnInfo(name = "end_time")
    private long end_time;

    @ColumnInfo(name = "color")
    private int color;

    public Task(int did, String task_name, long start_time, long end_time, int color) {
        this.did = did;
        this.task_name = task_name;
        this.start_time = start_time;
        this.end_time = end_time;
        this.color = color;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public int getDid() {
        return did;
    }

    public void setDid(int did) {
        this.did = did;
    }

    public String getTask_name() {
        return task_name;
    }

    public void setTask_name(String task_name) {
        this.task_name = task_name;
    }

    public long getStart_time() {
        return start_time;
    }

    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }

    public long getEnd_time() {
        return end_time;
    }

    public void setEnd_time(long end_time) {
        this.end_time = end_time;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }


    @Override
    public int compareTo(@NonNull Object o) {
        return Long.compare(this.getStart_time(),((Task)o).getStart_time());
    }
}
