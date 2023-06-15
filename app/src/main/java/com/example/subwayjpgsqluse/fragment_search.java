package com.example.subwayjpgsqluse;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class fragment_search extends Fragment implements OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener{
    private static final String API_KEY_ID = "xmup7b4lvc";
    private static final String SECRET_KEY_ID = "NDm9LbuJJYiqVycjcfbBk2uhLDVhSUccip3cL6ZM";
    private static final String GEOCODE_URL = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=";
    public final String string_address = "만안구 성결대학로 53";
    private static final String DIRECTION5_URL = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start=";
    public final String start_latlng = "126.9266623,37.3799";
    public final String goal_latlng = "126.9352657,37.3852172";
    private MapView mapView;
    private static NaverMap naverMap;
    private LatLng myLatLng;
    List<String> paths = new ArrayList<>();
    PathOverlay path = new PathOverlay();
    List<LatLng> LatLngs = new ArrayList<>();
    private FusedLocationSource locationSource;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    // 백그라운드 서비스
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private MyReceiver myReceiver;
    private LocationUpdatesService mService = null;
    private boolean mBound = false;
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;

    private Context mContext;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };
    //////////////////////////////////////////
    @SuppressLint("MissingInflatedId")
//    ////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        Button btnLatLng = v.findViewById(R.id.btnLatLng);
        mapView = v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        Log.e("query2 : ", "1");
        locationSource = new FusedLocationSource(getActivity(), LOCATION_PERMISSION_REQUEST_CODE);
        Log.e("query2 : ", "2");
        /* 추가 부분 */
        LocationManager locationManager = (LocationManager) this.getContext().getSystemService(Context.LOCATION_SERVICE);
        BroadcastReceiver proximityAlertReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(mContext, "목표 근접 중", Toast.LENGTH_LONG).show();
            }
        };
        Intent proximityIntent = new Intent("com.example.PROXIMITY_ALERT");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getContext(), 0
                , proximityIntent, PendingIntent.FLAG_MUTABLE);
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return v;
        }Log.e("query2 : ", "3");
        locationManager.addProximityAlert(37.3799, 126.9266623, 1000, 10, pendingIntent);
        this.getContext().registerReceiver(proximityAlertReceiver, new IntentFilter("com.example.PROXIMITY_ALERT"));
        btnLatLng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        requestGeocode();
                        requestDirections5(new Runnable() {
                            @Override
                            public void run() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        requestPathoverlay();
                                        naverMap.moveCamera(CameraUpdate.scrollTo(myLatLng));
                                        path.setMap(naverMap);
                                    }
                                });
                            }
                        });
                    }
                }).start();
            }
        });
        Log.e("query2 : ", "10");


        // 백그라운드 추가 부분


        PreferenceManager.getDefaultSharedPreferences(mContext)
                .registerOnSharedPreferenceChangeListener(this);


        myReceiver = new MyReceiver();
        if (Utils.requestingLocationUpdates(getActivity())) {
            if (!checkPermissions()) {
                requestPermissions(v);
            }
        }

        mRequestLocationUpdatesButton = (Button) v.findViewById(R.id.btnServiceOn);
        mRemoveLocationUpdatesButton = (Button) v.findViewById(R.id.btnServiceOff);

        mRequestLocationUpdatesButton.setOnClickListener(view -> {
            if (!checkPermissions()) {
                requestPermissions(v);
            } else {
                mService.requestLocationUpdates();
            }
        });

        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.removeLocationUpdates();
            }
        });
        setButtonState(Utils.requestingLocationUpdates(mContext));
        getActivity().bindService(new Intent(mContext, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        Log.e("query2 : ", "5");
        naverMap.setLocationSource(locationSource);
        ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);
        Log.e("query2 : ", "6");
    }
    public void requestGeocode() {
        Log.e("query2 : ", "4");
        TextView textView = (TextView) getView().findViewById(R.id.textView);
        try {
            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();
            String query = GEOCODE_URL + URLEncoder.encode(string_address, "UTF-8");
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn != null) {
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", API_KEY_ID);
                conn.setRequestProperty("X-NCP-APIGW-API-KEY", SECRET_KEY_ID);
                conn.setDoInput(true);
                Log.e("query2 : ", "6");
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "/n");
                }
                int indexFirst;
                int indexLast;
                indexFirst = stringBuilder.indexOf("\"x\":\"");
                indexLast = stringBuilder.indexOf("\",\"y\":");
                String x = stringBuilder.substring(indexFirst + 5, indexLast);

                indexFirst = stringBuilder.indexOf("\"y\":\"");
                indexLast = stringBuilder.indexOf("\",\"distance\":");
                String y = stringBuilder.substring(indexFirst + 5, indexLast);

                textView.setText("위도:" + y + " 경도:" + x);
                myLatLng = new LatLng(Double.parseDouble(y), Double.parseDouble(x));
                bufferedReader.close();
                conn.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void requestDirections5(Runnable callback) {
        try {
            BufferedReader bufferedReader2;
            StringBuilder stringBuilder2 = new StringBuilder();
            String query2 = DIRECTION5_URL + URLEncoder.encode(start_latlng, "UTF-8")
                    + "&goal=" + URLEncoder.encode(goal_latlng, "UTF-8");
            URL url2 = new URL(query2);
            HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
            if (conn2 != null) {
                conn2.setConnectTimeout(5000);
                conn2.setReadTimeout(5000);
                conn2.setRequestMethod("GET");
                conn2.setRequestProperty("X-NCP-APIGW-API-KEY-ID", API_KEY_ID);
                conn2.setRequestProperty("X-NCP-APIGW-API-KEY", SECRET_KEY_ID);
                conn2.setDoInput(true);

                int responseCode2 = conn2.getResponseCode();

                if (responseCode2 == 200) {
                    bufferedReader2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
                } else {
                    bufferedReader2 = new BufferedReader(new InputStreamReader(conn2.getErrorStream()));
                }

                String line2 = null;
                while ((line2 = bufferedReader2.readLine()) != null) {
                    stringBuilder2.append(line2 + "\n");
                }

                int indexFirst2;
                int indexLast2;

                indexFirst2 = stringBuilder2.indexOf("\"path\":");
                indexLast2 = stringBuilder2.indexOf("\"section\":");
                String pathpath = stringBuilder2.substring(indexFirst2 + 9, indexLast2 - 3);
                paths = Arrays.asList(pathpath.split("\\],\\["));
                LatLngs.clear();
                for (int i = 0; i < paths.size(); i++) {
                    String[] paTH = paths.get(i).split(",");
                    LatLngs.add(new LatLng(Double.parseDouble(paTH[1]), Double.parseDouble(paTH[0])));
                }
                bufferedReader2.close();
                conn2.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (callback != null) {
            callback.run();
        }
    }

    public void requestPathoverlay() {
        path.setCoords(LatLngs);
    }

    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }
    ///////////////////////////////////
    @SuppressLint("ResourceType")
//    /////////////////////////////
    private void requestPermissions(View v) {
        boolean shouldProvideRationale  = ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Snackbar.make(
                            v.findViewById(R.layout.fragment_search),
                            R.string.permission_rationale,
                            Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }

    }

    // 권한 요청 결과 처리 메서드
//    //////////////////////////////
    @SuppressLint("ResourceType")
//    //////////////////////////////
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, View v) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length < 0) {
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mService.requestLocationUpdates();
            } else {
                setButtonState(false);
                Snackbar.make(
                                v.findViewById(R.layout.fragment_search),
                                R.string.permission_denied_explanation,
                                Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        mContext.getPackageName(), null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mBound) {
            getActivity().unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .unregisterOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Toast.makeText(mContext, Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setButtonState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestLocationUpdatesButton.setEnabled(false);
            mRemoveLocationUpdatesButton.setEnabled(true);
        } else {
            mRequestLocationUpdatesButton.setEnabled(true);
            mRemoveLocationUpdatesButton.setEnabled(false);
        }
    }
}