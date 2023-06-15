package com.example.subwayjpgsqluse;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class SubwayTask extends AsyncTask<String, Void, String> {
    private static String mDirectionFilter = "";
    private ArrayList<String> mArrivalList;
    private ArrayAdapter<String> mAdapter, mDirectionAdapter;

    public SubwayTask(ArrayList<String> arrivalList, ArrayAdapter<String> adapter, ArrayAdapter<String> directionAdapter, String directionFilter) {
        mArrivalList = arrivalList;
        mAdapter = adapter;
        mDirectionAdapter = directionAdapter;
        mDirectionFilter = directionFilter;
    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null && !result.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray realTimeArrivalList = jsonObject.getJSONArray("realtimePositionList");
                for (int i = 0; i < realTimeArrivalList.length(); i++) {
                    JSONObject arrival = realTimeArrivalList.getJSONObject(i);
                    String trainLine = arrival.optString("subwayId", "no value");
                    String destination = arrival.optString("statnTnm", "no value") + "행";
                    String arrivalStation = arrival.optString("statnNm", "no value");
                    String trainStatus = arrival.optString("trainSttus", "no value");
                    String trainDir = arrival.optString("directAt", "no value");
                    String receivedTime = arrival.optString("recptnDt", "no value").substring(11);
                    //Log.e("TAG", "DicrectionFilter : " + mDirectionFilter + ", destination : " + destination);
                    if(!mDirectionFilter.equals(destination) && !mDirectionFilter.equals(""))
                    {
                        continue;
                    }
                    String dirMsg = "";
                    String trainState = "";
                    switch(trainStatus){
                        case "0":
                            trainState = " 진입 중";
                            break;
                        case "1":
                            trainState = " 도착";
                            break;
                        default:
                            trainState = "에서 출발";
                            break;
                    }
                    switch(trainDir) {
                        case "0":
                            dirMsg = "일반 ";
                            break;
                        case "1":
                            dirMsg = "급행 ";
                            break;
                        default:
                            break;
                    }
                    String message = destination + " " + dirMsg + "열차 " + arrivalStation + "역" + trainState  + " : " + receivedTime + "에 갱신됨";
                    mArrivalList.add(message);
                }
                mAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Log.e("TAG", "Error parsing JSON data: " + e.getMessage());
            }
        } else {
            Log.e("TAG", "Result is null or empty");
        }
    }
    private String getResponseFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
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
        return result;
    }
}