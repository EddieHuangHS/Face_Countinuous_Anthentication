package com.example.faceauth;

import android.app.*;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FaceAuthService extends Service {
    private static final String CHANNEL_ID = "FaceAuthChannel";
    private final IBinder binder = new LocalBinder();
    private CameraXWrapper cameraXWrapper;
    private FaceProcessor faceProcessor;

    public class LocalBinder extends Binder {
        public FaceAuthService getService() {
            return FaceAuthService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("FaceAuthService", "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotification();

        String userName = intent.getStringExtra("targetUser");
        Log.d("FaceAuthService", "Target user: " + userName);

        faceProcessor = new FaceProcessor(this, userName);

        cameraXWrapper = new CameraXWrapper(this, bitmap -> {
            Log.d("FaceAuthService", "Received frame from camera");
            float score = faceProcessor.verify(bitmap);
            Log.d("FaceAuthService", "Score computed: " + score);
            broadcastScore(score);
        });

        cameraXWrapper.start();
        return START_STICKY;
    }

    private void createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Face Authentication Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("FaceAuth 正在运行")
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .build();
        startForeground(1, notification);
    }

    private void broadcastScore(float score) {
        Intent intent = new Intent("com.example.SCORE_UPDATE");
        intent.setPackage("com.example.bankingapp"); // 目标接收包名
        intent.putExtra("score", score);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);
    }
}
