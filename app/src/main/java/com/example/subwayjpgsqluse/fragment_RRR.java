package com.example.subwayjpgsqluse;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;

import com.example.subwayjpgsqluse.DataBase.RealtimeSubwayPositionInput;

import java.util.ArrayList;

public class fragment_RRR extends Fragment {
    String API_URL = "http://swopenapi.seoul.go.kr/api/subway/6a656d4a776a756a3130347253434e6f/json/realtimePosition/0/200/7%ED%98%B8%EC%84%A0";
    CurrentTime ct = new CurrentTime();
    private ListView mListView;
    private ArrayList<String> mArrivalList;
    private ArrayAdapter<String> mAdapter, lineAdapter, directionAdapter;
    private static String directionFilter = "";
    private static String[] lineDirections;
    private String lastModifiedDirection = "";
    AutoCompleteTextView searchingLine, directionText;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_rrr, container, false);
        SetUrl setUrl = new SetUrl(getActivity());
        Button btnReset = v.findViewById(R.id.btnReset);
        mArrivalList = new ArrayList<>();
        mListView = v.findViewById(R.id.list_view);
        mAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, mArrivalList);
        mListView.setAdapter(mAdapter);
        String[] lineNums = getResources().getStringArray(R.array.lineNums);
        searchingLine = v.findViewById(R.id.searchLine); //현재 검색하는 노선 정보
        directionText = v.findViewById(R.id.searchDirection);
        lineAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1, lineNums);
        directionAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1, new String[]{});
        searchingLine.setAdapter(lineAdapter);
        directionText.setAdapter(directionAdapter);
        lineDirections = new String[0];

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnReset.setText(ct.getTime() + "에 새로고침됨");
                mAdapter.clear();
                new SubwayTask(mArrivalList, mAdapter, directionAdapter, directionFilter).execute(SetUrl.setUrlLine(lastModifiedDirection));
                new RealtimeSubwayPositionInput().DBInsert(); // 추가한 부분
            }
        });
        searchingLine.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence cS, int i, int i1, int i2)   {}
            public void onTextChanged(CharSequence cS, int i, int i1, int i2)   {
                lastModifiedDirection = cS.toString();
                lineDirections = setUrl.setDirection(lastModifiedDirection);
                try {
                    directionAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, lineDirections);
                }catch (NullPointerException e) {
                    Log.e("directionAdapter", "returned null"); //URL 전달 확인3
                }
                directionText.setText("");
                directionText.setAdapter(directionAdapter);
                mAdapter.clear();
                Log.e("MainActivity", setUrl.setUrlLine(cS.toString())); //URL 전달 확인
                new SubwayTask(mArrivalList, mAdapter, directionAdapter, directionFilter).execute(SetUrl.setUrlLine(cS.toString()));
            }
            public void afterTextChanged(Editable editable) {}
        });
        directionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence cS, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence cS, int i, int i1, int i2) {
                directionFilter = cS.toString();
                mAdapter.clear();
                setUrl.getUrlLine(lastModifiedDirection);
                Log.e("Last", lastModifiedDirection);
                new SubwayTask(mArrivalList, mAdapter, directionAdapter, directionFilter).execute(SetUrl.setUrlLine(lastModifiedDirection));
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        return v;
    }
}