package com.example.bankingapp;

import android.content.*;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.*;

import java.io.File;
import java.util.*;

public class FaceVerifyActivity extends AppCompatActivity {

    private Spinner userSpinner;
    private Button startButton;
    private LineChart chart;
    private LineDataSet dataSet;
    private List<Entry> entries;
    private int timeIndex = 0;

    private BroadcastReceiver scoreReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float score = intent.getFloatExtra("score", 0);
            Log.d("FaceVerifyActivity", "Received score: " + score);
            entries.add(new Entry(timeIndex++, score));
            dataSet.notifyDataSetChanged();
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.invalidate();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_verify);

        userSpinner = findViewById(R.id.userSpinner);
        startButton = findViewById(R.id.startButton);
        chart = findViewById(R.id.lineChart);

        // 图表初始化
        entries = new ArrayList<>();
        dataSet = new LineDataSet(entries, "Similarity Score");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        chart.setData(new LineData(dataSet));
        Description desc = new Description();
        desc.setText("Real time score");
//        desc.setText("实时比对得分");
        chart.setDescription(desc);

        loadUserList();

        startButton.setOnClickListener(v -> {
            String selectedUser = userSpinner.getSelectedItem().toString();
            Log.d("FaceVerifyActivity", "Start service with user: " + selectedUser);

            Intent serviceIntent = new Intent(this, com.example.faceauth.FaceAuthService.class);
            serviceIntent.putExtra("targetUser", selectedUser);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        });

        // 注册广播
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(scoreReceiver, new IntentFilter("com.example.SCORE_UPDATE"), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(scoreReceiver, new IntentFilter("com.example.SCORE_UPDATE"));
        }
    }

    private void loadUserList() {
        List<String> userList = new ArrayList<>();
        File dir = new File(getFilesDir(), "face_images");
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File img : files) {
                    if (img.getName().endsWith(".jpg")) {
                        String name = img.getName().replace(".jpg", "");
                        userList.add(name);
                    }
                }
            }
        }

        if (userList.isEmpty()) userList.add("No user");
//        if (userList.isEmpty()) userList.add("暂无用户");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(scoreReceiver);
    }
}
