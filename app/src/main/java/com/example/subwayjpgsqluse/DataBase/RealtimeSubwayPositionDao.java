package com.example.subwayjpgsqluse.DataBase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RealtimeSubwayPositionDao {
    @Insert
    void insert(RealtimeSubwayPosition rSP);

    @Update
    void update(RealtimeSubwayPosition rSP);

    @Delete
    void delete(RealtimeSubwayPosition rSP);

    @Query("SELECT * FROM RealtimeSubwayPosition")
    LiveData<List<RealtimeSubwayPosition>> getAll();
}
