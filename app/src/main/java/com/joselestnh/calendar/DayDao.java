package com.joselestnh.calendar;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface DayDao {

    @Query("SELECT * FROM Day")
    List<Day> getAll();

    @Query("SELECT * FROM Day WHERE did = :did")
    Day getDayBy(int did);


    @Query("SELECT * FROM Day WHERE year >= :year AND month >= :month AND day >= :day")
    List<Day> getNextDays(int year, int month, int day);

    @Query("SELECT did FROM Day WHERE year = :year AND month = :month AND day = :day")
    Integer getDid(int year, int month, int day);

    @Query("SELECT nTasks FROM Day WHERE did = :did")
    int getNumberOfTaskOf(int did);

    //must be used to update nTask, when removing or adding
    @Query("UPDATE Day SET nTasks = nTasks + :increment WHERE did = :did")
    void addNumberOfTasks(int did, int increment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDays(Day... days);

    @Delete
    void deleteDays(Day... days);
}
