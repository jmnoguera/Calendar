package com.joselestnh.calendar;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM Task")
    List<Task> getAll();

    @Query("SELECT * FROM Task WHERE did = :day")
    List<Task> getTaskByDay(int day);

    @Query("SELECT color FROM Task WHERE did = :did")
    List<Integer> getColorListOf(int did);

    @Insert
    void insertTask(Task... tasks);

    @Delete
    void deleteTask(Task... tasks);

}
