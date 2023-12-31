package com.example.subwayjpgsqluse;

import static android.app.NotificationManager.IMPORTANCE_HIGH;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.subwayjpgsqluse.model.Alarm;
import com.example.subwayjpgsqluse.service.ArrivalReceiver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationUpdatesService extends Service {
    private static final String PACKAGE_NAME = "com.google.android.gms.location.sample.locationupdatesforegroundservice";
    private static final String TAG = "resTAG";
    private static final String CHANNEL_ID = "channel_01";
    static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";
    private final IBinder mBinder = new LocalBinder();
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000 * 1;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 1;
    private static final int SHOWACTIVE_NOTIFICATION_ID = 12345678;
    private static final int SHOWARRIVAL_NOTIFICATION_ID = 87654321;
    private boolean mChangingConfiguration = false;
    private NotificationManager mNotificationManager;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Handler mServiceHandler;
    Double latitude, longitude;
    private Location mLocation;

    private final double CIRCLE_CENTER_LATITUDE = 37.3852172;
    private final double CIRCLE_CENTER_LONGITUDE = 126.9352657;
    private final float GEOFENCE_RADIUS = 200;


    private static final String BUNDLE_EXTRA = "bundle_ex";
    private static final String ALARM_KEY = "location_key";
    String arrivalData = "";

    @SuppressWarnings("deprecation")
    public LocationUpdatesService() {
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };
        createLocationRequest();
        getLastLocation();
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            //mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION, false);
        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
            return START_NOT_STICKY;
        }
        startForeground(SHOWACTIVE_NOTIFICATION_ID, showNotification());
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        } catch (SecurityException unlikely) {
            stopSelf();
        }
        return START_STICKY;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    // 서비스와 클라이언트와의 상호 관계 정의
    @SuppressWarnings("deprecation")
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.e(TAG, "in Bind()");
        // foreground 상태에서 서비스 중지. 화면에 안 나와도 계속 실행 되게 함
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }
    @SuppressWarnings("deprecation")
    @Override
    public void onRebind(Intent intent) {
        Log.e(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;

        super.onRebind(intent);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "Last client unbound from service");
        if (!mChangingConfiguration && Utils.requestingLocationUpdates(this)) {
            Log.e(TAG, "Starting foreground service");
            startForeground(SHOWACTIVE_NOTIFICATION_ID, showNotification());
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }

    // 위치 업데이트를 요청하는 메서드
    public void requestLocationUpdates() {
        Log.e(TAG, "Requesting location updates");
        Utils.setRequestingLocationUpdates(this, true);
        // LocationUpdateService 시작(백그라운드에서 실행 가능)
        startService(new Intent(getApplicationContext(), LocationUpdatesService.class));
        try {
            // 위치 업데이트 요청
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, false);
            Log.e(TAG,"Lost location permission. Could not request updates." + unlikely);
        }
    }

    public void removeLocationUpdates() {
        Log.e(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            Utils.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not request updates." + unlikely);
        }
    }

    private static void createNotificationChannel(Context ctx) {
        if(SDK_INT < O) return;

        final NotificationManager mgr = ctx.getSystemService(NotificationManager.class);
        if(mgr == null) return;

        final String name = ctx.getString(R.string.channel_name);
        if(mgr.getNotificationChannel(name) == null) {
            final NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[] {1000,500,1000,500,1000,500});
            channel.setBypassDnd(true);
            mgr.createNotificationChannel(channel);
        }
    }

    private Notification showNotification() {
        Intent intent = new Intent(this, LocationUpdatesService.class);

        CharSequence text = Utils.getLocationText(mLocation);

        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentText(text)
                .setContentTitle(Utils.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }


        return builder.build();
    }

    // Notification 생성 메서드
    private Notification getNotification() {
        Intent intent = new Intent(this, LocationUpdatesService.class);

        CharSequence text = Utils.getLocationText(mLocation);
        try {
            arrivalData = new ArrivalReceiver().execute("명학").get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);
        final NotificationManager manager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel(getApplicationContext());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        builder.setSmallIcon(R.drawable.ic_alarm_white);
        builder.setColor(ContextCompat.getColor(this, R.color.green_200));
        bigTextStyle.bigText(arrivalData); // Initialize with empty text
        builder.setStyle(bigTextStyle);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setAutoCancel(true);
        builder.setOngoing(false);
        builder.setPriority(Notification.PRIORITY_HIGH);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }
        return builder.build();
    }

    // 가장 최근 저장된 위치 가져옴
    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLocation = task.getResult();
                        } else {
                            Log.w(TAG, "Failed to get location.");
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.d(TAG, "Lost location permission." + unlikely);
        }
    }

    // 위치 정보를 받고 표시하는 메서드
    private void onNewLocation(Location location) {
        Log.d(TAG, "New location: " + location);

        // 위치 정보 저장
        mLocation = location;
        // 위치 정보를 담은 인탠트 생성
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        // geofence 실험
        Log.e("RequestTest_5_1", "intoThePoint");
        double myLatitude = location.getLatitude();
        double myLongitude = location.getLongitude();

        float[] distance = new float[1];
        Location.distanceBetween(myLatitude, myLongitude, CIRCLE_CENTER_LATITUDE, CIRCLE_CENTER_LONGITUDE, distance);

        if (distance[0] <= GEOFENCE_RADIUS) {
            mNotificationManager.notify(SHOWARRIVAL_NOTIFICATION_ID, getNotification());
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            sendData(mLocation);
            Log.e("RequestTest_5_2", "inPoint");
        } else {
            sendData(mLocation);
            Log.e("RequestTest_5_3", "outPoint");
        }
        // 만약 service 가 foreground 에서 실행 중일 경우 알람
        if (serviceIsRunningInForeground(this)) {
//            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
//            latitude = location.getLatitude();
//            longitude = location.getLongitude();
//            sendData(mLocation);
        }
    }


    // 위치 업데이트 갱신 설정(갱신 시간 : 1초, 최소 갱신 시간 : 0.5초, 더 정확한 위치 업데이트 가능)
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };
    }

    public class LocalBinder extends Binder {
        LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }

    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    private void sendData(Location location) {
        Toast.makeText(this, "Save to server", Toast.LENGTH_SHORT).show();
        Log.d("resMM", "Send to server");
        Log.d("resML", String.valueOf(latitude));
        Log.d("resMLL", String.valueOf(longitude));

    }
}