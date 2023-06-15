package com.example.subwayjpgsqluse.DataBase;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class RealtimeSubwayPosition {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String stationName;
    public String trainLine;
    public String trainNumber;
    public String updatedTime;
    public String updownDivision;
    public String destinationStation;
    public String trainState;
    public String expressDivision;
    public String lastDivision;
}