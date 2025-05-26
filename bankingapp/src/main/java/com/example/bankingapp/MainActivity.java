package com.example.bankingapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.net.Uri;
import android.os.Environment;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.core.content.FileProvider;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSIONS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestCameraPermissions(); // 请求相机权限

        // 创建 UI 布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 60, 40, 60);

        // 👉 按钮 1：注册新用户
        Button btnRegister = new Button(this);
        btnRegister.setText("注册新用户");
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        // 👉 按钮 2：开始人脸识别
        Button btnStart = new Button(this);
        btnStart.setText("开始人脸识别");
        btnStart.setOnClickListener(v -> {
            // 启动后台服务
            Intent serviceIntent = new Intent(this, com.example.faceauth.FaceAuthService.class);
            startForegroundService(serviceIntent);

            // 启动图表页面
            startActivity(new Intent(this, ScoreChartActivity.class));
        });

        // 👉 按钮 3：进入人脸数据库管理界面
        Button btnDatabase = new Button(this);
        btnDatabase.setText("数据库管理");
        btnDatabase.setOnClickListener(v -> {
            startActivity(new Intent(this, FaceDatabaseActivity.class));
        });

        // 添加按钮到布局
        layout.addView(btnRegister);
        layout.addView(btnStart);
        layout.addView(btnDatabase);

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
        // 权限处理逻辑（可选添加提示）
    }
}
