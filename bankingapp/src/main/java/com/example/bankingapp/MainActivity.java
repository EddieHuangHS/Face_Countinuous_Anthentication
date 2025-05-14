package com.example.bankingapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSIONS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestCameraPermissions(); // 请求权限

        // 简单 UI 布局：一个按钮
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        Button btnStart = new Button(this);
        btnStart.setText("开始人脸识别");

        btnStart.setOnClickListener(v -> {
            // 启动图表界面
            startActivity(new Intent(this, ScoreChartActivity.class));

            // 启动后台人脸识别服务
            Intent serviceIntent = new Intent(this, com.example.faceauth.FaceAuthService.class);
            startForegroundService(serviceIntent);
        });

        layout.addView(btnStart);
        setContentView(layout);
    }

    private void requestCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
                permissions = new String[]{
                        Manifest.permission.CAMERA,
                        "android.permission.FOREGROUND_SERVICE_CAMERA"
                };
            } else {
                permissions = new String[]{
                        Manifest.permission.CAMERA
                };
            }

            boolean allGranted = true;
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                requestPermissions(permissions, REQUEST_CAMERA_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 此处可添加权限失败提示
    }
}
