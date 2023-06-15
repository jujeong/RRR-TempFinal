package com.example.subwayjpgsqluse.service;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ArrivalReceiver extends AsyncTask<String, Void, String> {
    private boolean upTrain, dwnTrain;
    public String upNoty = "상행 - ", dwnNoty = "하행 - ";
    public static String targetStation;
    private String API_URL = "http://swopenapi.seoul.go.kr/api/subway/6a656d4a776a756a3130347253434e6f/json/realtimeStationArrival/0/100/";
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
    private String getResponseFromUrl(String urlString) throws IOException {
        targetStation = urlString;
        URL url = new URL(API_URL + urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        InputStream inputStream = connection.getInputStream();
        Scanner scanner = new Scanner(inputStream, "UTF-8");
        String response = scanner.useDelimiter("\\A").next();
        scanner.close();
        inputStream.close();
        connection.disconnect();
        return response;
    }
    protected String doInBackground(String... urls) {
        String result = "";
        try {
            result = getResponseFromUrl(urls[0]);
        } catch (IOException e) {
            Log.e("TAG", "Error getting data from API: " + e.getMessage());
        }
        if (result != null && !result.isEmpty()) {
            try {
                Log.e("-1", "-1");
                JSONObject jsonObject = new JSONObject(result);
                JSONArray realTimeArrivalList = jsonObject.getJSONArray("realtimeArrivalList");
                for (int i = 0; i < realTimeArrivalList.length(); i++) {
                    JSONObject arrival = realTimeArrivalList.getJSONObject(i);
                    if (!upTrain && arrival.optString("updnLine", "no value").equals("상행")) {
                        upTrain = true;
                        upNoty += arrival.optString("bstatnNm", "no value") + "방면 열차 "
                                + arrival.optString("arvlMsg2", "no value") + " ("
                                + arrival.optString("arvlMsg3", "no value") + ")";
                        Log.e("4", upNoty);
                    }
                    if (!dwnTrain && arrival.optString("updnLine", "no value").equals("하행")) {
                        dwnTrain = true;
                        dwnNoty += arrival.optString("bstatnNm", "no value") + "방면 열차 "
                                + arrival.optString("arvlMsg2", "no value") + " ("
                                + arrival.optString("arvlMsg3", "no value") + ")";
                    }
                }
            } catch (JSONException e) {
                Log.e("TAG", "Error parsing JSON data: " + e.getMessage());
            }
        }
        Log.e("onPostExcute", targetStation + "역 기준 도착정보\n");
        Log.e("onPostExcute", upNoty);
        Log.e("onPostExcute", dwnNoty);
        return targetStation + "역 기준 도착정보\n" + upNoty + "\n" + dwnNoty;
    }
}