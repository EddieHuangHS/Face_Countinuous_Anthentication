package com.example.bankingapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
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
import java.nio.file.Files;

public class RegisterActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button captureButton, registerButton;
    private Bitmap latestBitmap;
    private boolean arcfaceInitialized = false;

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

        // Initialize ArcFace model
        // 初始化 ArcFace 模型
        try {
            init(getAssets(), "arcface-opt.param", "arcface-opt.bin");
            arcfaceInitialized = true;
        } catch (Exception e) {
            Toast.makeText(this, "Failed to initialize ArcFace", Toast.LENGTH_SHORT).show();
//            Toast.makeText(this, "ArcFace 初始化失败", Toast.LENGTH_SHORT).show();
        }

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
        if (!arcfaceInitialized) {
            Toast.makeText(this, "ArcFace is not initialized", Toast.LENGTH_SHORT).show();
//            Toast.makeText(this, "ArcFace 未初始化", Toast.LENGTH_SHORT).show();
            return;
        }

        if (latestBitmap == null) {
            Toast.makeText(this, "Please take photo first", Toast.LENGTH_SHORT).show();
//            Toast.makeText(this, "请先拍摄照片", Toast.LENGTH_SHORT).show();

            return;
        }

        float[] feature = extractFeature(latestBitmap);
        if (feature == null || feature.length == 0) {
            Toast.makeText(this, "Failed to extract features", Toast.LENGTH_SHORT).show();
//            Toast.makeText(this, "提取特征失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // Repeated face check
        // 检查是否为重复人脸
        if (isFaceAlreadyRegistered(feature)) {
            Toast.makeText(this, "This face has registered", Toast.LENGTH_LONG).show();
//            Toast.makeText(this, "该用户人脸数据已注册", Toast.LENGTH_LONG).show();
            return;
        }

        // Enter username
        // 输入用户名
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please enter username");
//        builder.setTitle("请输入用户名");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Yes", (dialog, which) -> {
//        builder.setPositiveButton("确定", (dialog, which) -> {
            String username = input.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
//                Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            File faceImage = new File(getFilesDir(), "face_images/" + username + ".jpg");
            if (faceImage.exists()) {
                Toast.makeText(this, "Username exist, please use other name", Toast.LENGTH_SHORT).show();
//                Toast.makeText(this, "用户名已存在，请使用其他用户名", Toast.LENGTH_SHORT).show();
                return;
            }

            saveImage(username, latestBitmap);
            saveFeatureToFile(username, feature);
            Toast.makeText(this, "Register Success", Toast.LENGTH_SHORT).show();

            // 注册成功后返回主界面
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
//        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
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

    private boolean isFaceAlreadyRegistered(float[] newFeature) {
        File filesDir = getFilesDir();
        File[] allFiles = filesDir.listFiles();

        if (allFiles == null) return false;

        for (File file : allFiles) {
            if (file.getName().endsWith("_feature.txt")) {
                try {
                    String[] values = new String(Files.readAllBytes(file.toPath())).trim().split("\\s+");

                    if (values.length != 512) continue;

                    float[] existing = new float[512];
                    for (int i = 0; i < 512; i++) {
                        existing[i] = Float.parseFloat(values[i]);
                    }

                    float sim = cosineSimilarity(newFeature, existing);
                    Log.d("FaceSimCheck", "Similarity with " + file.getName() + " is: " + sim);
                    if (sim > 0.65f) return true;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private float cosineSimilarity(float[] a, float[] b) {
        float dot = 0f, normA = 0f, normB = 0f;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (float) (Math.sqrt(normA) * Math.sqrt(normB) + 1e-5);
    }
}
