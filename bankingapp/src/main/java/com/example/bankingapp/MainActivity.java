package com.example.bankingapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSIONS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestCameraPermissions();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 60, 40, 60);

        // 👉 注册按钮
        Button btnRegister = new Button(this);
        btnRegister.setText("注册新用户");
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        // ✅ 修改后：跳转到 FaceVerifyActivity（含选择框和图表）
        Button btnStart = new Button(this);
        btnStart.setText("开始人脸识别");
        btnStart.setOnClickListener(v -> {
            startActivity(new Intent(this, FaceVerifyActivity.class));
        });

        Button btnDatabase = new Button(this);
        btnDatabase.setText("数据库管理");
        btnDatabase.setOnClickListener(v -> {
            startActivity(new Intent(this, FaceDatabaseActivity.class));
        });

        layout.addView(btnRegister);
        layout.addView(btnStart);
        layout.addView(btnDatabase);

        setContentView(layout);
    }

    private void requestCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
