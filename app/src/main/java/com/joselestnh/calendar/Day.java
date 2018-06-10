package com.joselestnh.calendar;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(indices = {@Index(value = {"did"}, unique = true)})
public class Day {

    @PrimaryKey(autoGenerate = true)
    private int did;

    @ColumnInfo(name = "year")
    private int year;

    @ColumnInfo(name = "month")
    private int month;

    @ColumnInfo(name = "day")
    private int day;

    @ColumnInfo(name = "NTasks")
    private int NTasks;

    public Day(int year, int month, int day, int NTasks) {
        this.did = did;
        this.year = year;
        this.month = month;
        this.day = day;
        this.NTasks = NTasks;
    }

    public int getDid() {
        return did;
    }

    public void setDid(int did) {
        this.did = did;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getNTasks() {
        return NTasks;
    }

    public void setNTasks(int NTasks) {
        this.NTasks = NTasks;
    }
}
