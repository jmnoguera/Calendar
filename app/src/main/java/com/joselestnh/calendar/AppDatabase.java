package com.joselestnh.calendar;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Day.class, Task.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DayDao dayDao();
    public abstract TaskDao taskDao();
}
