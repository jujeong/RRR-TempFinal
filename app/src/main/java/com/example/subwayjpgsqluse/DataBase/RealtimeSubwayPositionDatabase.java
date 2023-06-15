package com.example.subwayjpgsqluse.DataBase;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {RealtimeSubwayPosition.class}, version = 1)
public abstract class RealtimeSubwayPositionDatabase extends RoomDatabase {
    public abstract RealtimeSubwayPositionDao getRealtimeSubwayPositionDao();
    private static RealtimeSubwayPositionDatabase INSTANCE;
    public synchronized static RealtimeSubwayPositionDatabase getDBInstance(Context context) {
        Log.e("Instance", "is NULL1");
        if (INSTANCE == null) {
            Log.e("Instance", "is NULL2");
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RealtimeSubwayPositionDatabase.class, "DB_NAME")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        if(INSTANCE == null)
        {
            Log.e("Instance", "is NULL3");
        }
        return INSTANCE;
    }
    public static void destroyInstance() {
        INSTANCE = null;
    }

}