package com.example.bankingapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button captureButton, registerButton;
    private Bitmap latestBitmap;

    static {
        System.loadLibrary("arcface");
    }

    public native float[] extractFeature(Bitmap bitmap);
    public native void init(AssetManager mgr, String paramPath, String binPath);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        imageView = findViewById(R.id.imageView);
        captureButton = findViewById(R.id.captureButton);
        registerButton = findViewById(R.id.registerButton);

        // 初始化 ArcFace 模型
        init(getAssets(), "arcface-opt.param", "arcface-opt.bin");

        captureButton.setOnClickListener(v -> openCamera());
        registerButton.setOnClickListener(v -> registerFace());
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            latestBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(latestBitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void registerFace() {
        if (latestBitmap == null) {
            Toast.makeText(this, "请先拍摄照片", Toast.LENGTH_SHORT).show();
            return;
        }

        float[] feature = extractFeature(latestBitmap);
        if (feature == null || feature.length == 0) {
            Toast.makeText(this, "提取特征失败", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入用户名");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String username = input.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            saveImage(username, latestBitmap);
            saveFeatureToFile(username, feature);
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveImage(String username, Bitmap bitmap) {
        File dir = new File(getFilesDir(), "face_images");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, username + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFeatureToFile(String username, float[] feature) {
        File file = new File(getFilesDir(), username + "_feature.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (float f : feature) {
                writer.write(f + " ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
