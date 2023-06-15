package com.example.subwayjpgsqluse;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class SetUrl {
    private static Context context;
    private static String[] lineDirections;
    private static String line = "";
    private static final String KEY = "6a656d4a776a756a3130347253434e6f";
    private static String API_URL = "http://swopenapi.seoul.go.kr/api/subway/" + KEY + "/json/realtimePosition/0/100/%s";
    public SetUrl(Context context){
        this.context = context;
    }
    public static String setUrlLine(String line) {
        return String.format(API_URL, line);
    }
    public static String[] setDirection(String line) {
        try {
            lineDirections = new String[0];
            switch (line) {
                case "1호선":
                    lineDirections = context.getResources().getStringArray(R.array.line1Directions);
                    break;
                case "2호선":
                    lineDirections = context.getResources().getStringArray(R.array.line2Directions);
                    break;
                case "3호선":
                    lineDirections = context.getResources().getStringArray(R.array.line3Directions);
                    break;
                case "4호선":
                    lineDirections = context.getResources().getStringArray(R.array.line4Directions);
                    break;
                case "5호선":
                    lineDirections = context.getResources().getStringArray(R.array.line5Directions);
                    break;
                case "6호선":
                    lineDirections = context.getResources().getStringArray(R.array.line6Directions);
                    break;
                case "7호선":
                    lineDirections = context.getResources().getStringArray(R.array.line7Directions);
                    break;
                case "8호선":
                    lineDirections = context.getResources().getStringArray(R.array.line8Directions);
                    break;
                case "9호선":
                    lineDirections = context.getResources().getStringArray(R.array.line9Directions);
                    break;
                case "우이신설선":
                    lineDirections = context.getResources().getStringArray(R.array.lineUiSinseolDirections);
                    break;
                case "수인분당선":
                    lineDirections = context.getResources().getStringArray(R.array.lineSuInBunDangDirections);
                    break;
                case "신분당선":
                    lineDirections = context.getResources().getStringArray(R.array.lineShinBunDangDirections);
                    break;
                case "경의중앙선":
                    lineDirections = context.getResources().getStringArray(R.array.lineGyeongUiCentralDirections);
                    break;
                case "경춘선":
                    lineDirections = context.getResources().getStringArray(R.array.lineGyeongChunDirections);
                    break;
                case "공항철도":
                    lineDirections = context.getResources().getStringArray(R.array.lineAirportDirections);
                    break;
                default:
                    break;
            }
            return lineDirections;
        }catch (NullPointerException e) { return null; }
    }
    public static void getUrlLine(String lastModifiedDirection) { Log.e("Current URL", String.format(API_URL, lastModifiedDirection)); }
}
