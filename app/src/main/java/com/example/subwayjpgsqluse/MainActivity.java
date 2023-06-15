package com.example.subwayjpgsqluse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.example.subwayjpgsqluse.DataBase.RealtimeSubwayPositionInput;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static final Locale DEFAULT_LOCALE = Locale.KOREAN;
    public static Context context;
    SubsamplingScaleImageView imageView;
    private String[] imageMapArray;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private fragment_search fragment_search = new fragment_search();
    private fragment_map fragment_map = new fragment_map();
    private fragment_RRR fragment_RRR = new fragment_RRR();
    private fragment_alarm fragment_alarm = new fragment_alarm();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        context = getApplicationContext();
        imageView = findViewById(R.id.abcd);
        imageView.setImage(ImageSource.resource(R.drawable.naver_subway));
        imageMapArray = getResources().getStringArray(R.array.subway_map);
        imageView.setMinimumScaleType(1); //최소축소크기 설정
        imageView.setScaleY(1.7f);
        final GestureDetector gestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(@NonNull MotionEvent ev) {
                if (imageView.isReady()) {
                    PointF sCoord = imageView.viewToSourceCoord(ev.getX(), ev.getY());
                    int x_cor = (int) sCoord.x;
                    int y_cor = (int) sCoord.y;
                    for (int i = 0; i < imageMapArray.length; i++)
                    {
                        String[] subwaySplitInfo = imageMapArray[i].split("_");
                        String stationName = subwaySplitInfo[0];
                        String[] coordSplitInfo = subwaySplitInfo[1].split(",");
                        int X1 = Integer.parseInt(coordSplitInfo[0]);
                        int Y1 = Integer.parseInt(coordSplitInfo[1]);
                        int X2 = Integer.parseInt(coordSplitInfo[2]);
                        int Y2 = Integer.parseInt(coordSplitInfo[3]);
                        if ((x_cor > X1) && (x_cor < X2) && (y_cor > Y1) && (y_cor < Y2)) {
                            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_subway, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setView(dialogView);
                            AlertDialog dialog = builder.create();
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
                            wmlp.x = (int) ev.getX();
                            wmlp.y = (int) ev.getY() - 550;
                            LinearLayout btn_start = dialogView.findViewById(R.id.btn_start); // 출발
                            LinearLayout btn_plus = dialogView.findViewById(R.id.btn_plus);   // 경유
                            LinearLayout btn_end = dialogView.findViewById(R.id.btn_end);     // 도착
                            LinearLayout btn_more = dialogView.findViewById(R.id.btn_more);   // 상세
                            btn_start.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // 다이얼로그 종료
                                    dialog.dismiss();
                                }
                            });
                            btn_plus.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // 다이얼로그 종료
                                    dialog.dismiss();
                                }
                            });
                            btn_end.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // 다이얼로그 종료
                                    dialog.dismiss();
                                }
                            });
                            btn_more.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // 다이얼로그 종료
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();

                            Toast.makeText(MainActivity.this, stationName + " 역 입니다 !", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                return super.onSingleTapUp(ev);
            }
        });
        imageView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });
//---------------------------------------------------------------------------------------------
        //하단바 프레그먼트 코드
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frameLayout, fragment_search).commitAllowingStateLoss();

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.searchItem:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragment_search).commit();
                        imageView.setVisibility(View.INVISIBLE);
                        return true;
                    case R.id.mapItem:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragment_map).commit();
                        imageView.setVisibility(View.VISIBLE);
                        return true;
                    case R.id.rrrItem:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragment_RRR).commit();
                        imageView.setVisibility(View.INVISIBLE);
                        return true;
                    case R.id.alarmItem:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragment_alarm).commit();
                        imageView.setVisibility(View.INVISIBLE);
                        return true;
                }
                return false;
            }
        });
    }
    //액션바 search 메뉴 자바
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            showSearchDialog(); // 검색 다이얼로그 표시
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("지하철 역 검색");
        final EditText editText = new EditText(this);
        builder.setView(editText);

        builder.setPositiveButton("검색", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String searchStation = editText.getText().toString();
                moveToStation(searchStation); // 검색한 역으로 이동
                showStationDialog(searchStation);
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void moveToStation(String stationName) {
        for (int i = 0; i < imageMapArray.length; i++) {
            String[] subwaySplitInfo = imageMapArray[i].split("_");
            String station = subwaySplitInfo[0];

            if (station.equals(stationName)) {
                String[] coordSplitInfo = subwaySplitInfo[1].split(",");
                int X1 = Integer.parseInt(coordSplitInfo[0]);
                int Y1 = Integer.parseInt(coordSplitInfo[1]);
                int X2 = Integer.parseInt(coordSplitInfo[2]);
                int Y2 = Integer.parseInt(coordSplitInfo[3]);

                // 역의 위치로 이미지 이동 및 확대
                float centerX = (X1 + X2) / 2f;
                float centerY = (Y1 + Y2) / 2f;
                imageView.setScaleAndCenter(2f, new PointF(centerX, centerY));
                break;
            }
        }
    }
    private void showStationDialog(String stationName) {
        // 다이얼로그 뷰 생성
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_station, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // 다이얼로그 위치 설정
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.x = 0;
        wmlp.y = 0;

        // 다이얼로그 뷰 찾기
        TextView txtDeparture = dialogView.findViewById(R.id.txt_departure);
        TextView txtMoveToMap = dialogView.findViewById(R.id.txt_move_to_map);
        TextView txtArrivalInfo = dialogView.findViewById(R.id.txt_arrival_info);
        TextView txtTimetable = dialogView.findViewById(R.id.txt_timetable);

        // 출발역 설정
        txtDeparture.setText(stationName + "을(를) 출발역으로 설정");
        txtDeparture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 선택한 역을 출발역으로 설정
                // TODO: 여기에 로직을 구현하세요
                Toast.makeText(MainActivity.this, stationName + "을(를) 출발역으로 설정하였습니다.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        // 노선도로 이동
        txtMoveToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 노선도로 이동
                // TODO: 여기에 로직을 구현하세요
                Toast.makeText(MainActivity.this, stationName + "의 노선도로 이동합니다.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        // 도착 정보 표시
        txtArrivalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 선택한 역의 도착 정보 표시
                // TODO: 여기에 로직을 구현하세요
                Toast.makeText(MainActivity.this, stationName + "의 도착 정보를 표시합니다.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        // 시간표 보기
        txtTimetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 선택한 역의 시간표 보기
                // TODO: 여기에 로직을 구현하세요
                showTimetableDialog(stationName);
                Toast.makeText(MainActivity.this, stationName + "의 시간표를 보여줍니다.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        // 다이얼로그 표시
        dialog.show();
    }
    private void showTimetableDialog(String stationName) {
        // 다이얼로그 뷰 생성
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_timetable, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // 시간표 데이터
        String[] timetableData = getTimetableData(stationName);

        // 시간표 리스트뷰 설정
        ListView listViewTimetable = dialogView.findViewById(R.id.listView_timetable);
        ArrayAdapter<String> timetableAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, timetableData);
        listViewTimetable.setAdapter(timetableAdapter);

        // 다이얼로그 표시
        dialog.show();
    }
    private String[] getTimetableData(String stationName) {
        // TODO: 선택한 역에 해당하는 시간표 데이터 가져오기
        // 여기에서는 임의의 데이터를 사용하도록 하겠습니다.
        if (stationName.equals("역 이름 1")) {
            return new String[]{"시간1", "시간2", "시간3"};
        } else if (stationName.equals("역 이름 2")) {
            return new String[]{"시간A", "시간B", "시간C"};
        } else {
            return new String[]{"05시 31분 광운대\n" +
                    "\n" +
                    "05시 45분 광운대\n" +
                    "\n" +
                    "05시 58분 광운대\n" +
                    "\n" +
                    "06시 08분 광운대\n" +
                    "\n" +
                    "06시 18분 광운대\n" +
                    "\n" +
                    "06시 27분 광운대\n" +
                    "\n" +
                    "06시 35분 광운대\n" +
                    "\n" +
                    "06시 42분 광운대\n" +
                    "\n" +
                    "06시 49분 광운대\n" +
                    "\n" +
                    "06시 55분 광운대\n" +
                    "\n" +
                    "07시 03분 광운대\n" +
                    "\n" +
                    "07시 11분 광운대\n" +
                    "\n" +
                    "07시 17분 광운대\n" +
                    "\n" +
                    "07시 22분 광운대\n" +
                    "\n" +
                    "07시 30분 광운대\n" +
                    "\n" +
                    "07시 35분 광운대\n" +
                    "\n" +
                    "07시 42분 광운대\n" +
                    "\n" +
                    "07시 50분 광운대\n" +
                    "\n" +
                    "07시 55분 광운대\n" +
                    "\n" +
                    "08시 03분 광운대\n" +
                    "\n" +
                    "08시 09분 광운대\n" +
                    "\n" +
                    "08시 16분 광운대\n" +
                    "\n" +
                    "08시 24분 광운대\n" +
                    "\n" +
                    "08시 30분 광운대\n" +
                    "\n" +
                    "08시 40분 광운대\n" +
                    "\n" +
                    "08시 47분 광운대\n" +
                    "\n" +
                    "08시 52분 광운대\n" +
                    "\n" +
                    "08시 57분 광운대\n" +
                    "\n" +
                    "09시 03분 광운대\n" +
                    "\n" +
                    "09시 12분 광운대\n" +
                    "\n" +
                    "09시 23분 광운대\n" +
                    "\n" +
                    "09시 31분 광운대\n" +
                    "\n" +
                    "09시 42분 광운대\n" +
                    "\n" +
                    "09시 49분 광운대\n" +
                    "\n" +
                    "10시 01분 광운대\n" +
                    "\n" +
                    "10시 09분 광운대\n" +
                    "\n" +
                    "10시 22분 광운대\n" +
                    "\n" +
                    "10시 30분 광운대\n" +
                    "\n" +
                    "10시 44분 광운대\n" +
                    "\n" +
                    "10시 51분 광운대\n" +
                    "\n" +
                    "11시 00분 광운대\n" +
                    "\n" +
                    "11시 09분 광운대\n" +
                    "\n" +
                    "11시 21분 광운대\n" +
                    "\n" +
                    "11시 27분 광운대\n" +
                    "\n" +
                    "11시 40분 광운대\n" +
                    "\n" +
                    "11시 50분 광운대"};
        }
    }
}