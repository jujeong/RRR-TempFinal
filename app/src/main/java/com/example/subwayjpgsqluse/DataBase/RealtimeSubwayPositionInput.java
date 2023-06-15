package com.example.subwayjpgsqluse.DataBase;

import static com.example.subwayjpgsqluse.MainActivity.context;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class RealtimeSubwayPositionInput extends AppCompatActivity {
    private RealtimeSubwayPositionDatabase db;
    //String API_URL = "http://swopenapi.seoul.go.kr/api/subway/6a656d4a776a756a3130347253434e6f/json/realtimePosition/0/200/7%ED%98%B8%EC%84%A0";
    String API_URL = "http://swopenapi.seoul.go.kr/api/subway/sample/json/realtimePosition/0/5/1%ED%98%B8%EC%84%A0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //db = Room.databaseBuilder(getApplicationContext(), RealtimeSubwayPositionDatabase.class, "rsp").build();
    }
    public void DBInsert() {
        new Thread(() -> {
            db = RealtimeSubwayPositionDatabase.getDBInstance(context);
            try {
                URL url = new URL(API_URL);
                InputStream is = url.openStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isr);
                StringBuffer buffer = new StringBuffer();
                String line = reader.readLine();
                while (line != null) {
                    buffer.append(line + "\n");
                    line = reader.readLine();
                }
                String jsonData = buffer.toString();
                JSONObject jsonObject = new JSONObject(jsonData);
                JSONArray realtimePositionList = jsonObject.getJSONArray("realtimePositionList");
                for (int j = 0; j < realtimePositionList.length(); j++) {
                    RealtimeSubwayPosition rSP = new RealtimeSubwayPosition();
                    JSONObject arrival = realtimePositionList.getJSONObject(j);
                    String stationName = arrival.optString("statnNm", "no value");
                    String trainLine = arrival.optString("subwayNm", "no value");
                    String trainNumber = arrival.optString("trainNo", "no value");
                    String updatedTime = arrival.optString("recptnDt", "no value");
                    String updownDivision = arrival.optString("updnLine", "no value");
                    String destinationStation = arrival.optString("statnTnm", "no value");
                    String trainState = arrival.optString("trainSttus", "no value");
                    String expressDivision = arrival.optString("directAt", "no value");
                    String lastDivision = arrival.optString("lstcarAt", "no value");
                    Log.e("ParseDB", stationName);
                    rSP.stationName = stationName;
                    rSP.trainLine = trainLine;
                    rSP.trainNumber = trainNumber;
                    rSP.updatedTime = updatedTime;
                    rSP.updownDivision = updownDivision;
                    rSP.destinationStation = destinationStation;
                    rSP.trainState = trainState;
                    rSP.expressDivision = expressDivision;
                    rSP.lastDivision = lastDivision;
                    if (db != null) {
                        db.getRealtimeSubwayPositionDao().insert(rSP);
                    } else {
                        Log.e("Error", "Database is null");
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }
}