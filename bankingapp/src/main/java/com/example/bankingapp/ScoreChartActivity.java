package com.example.bankingapp;

import android.content.*;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.*;

import java.util.*;

public class ScoreChartActivity extends AppCompatActivity {
    private LineChart chart;
    private LineDataSet dataSet;
    private List<Entry> entries;
    private int timeIndex = 0;

    private final BroadcastReceiver scoreReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float score = intent.getFloatExtra("score", 0);
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
        setContentView(R.layout.activity_score_chart);

        chart = findViewById(R.id.lineChart);
        entries = new ArrayList<>();
        dataSet = new LineDataSet(entries, "Face Match Score");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        Description description = new Description();
        description.setText("Real-Time Face Authentication Score");
        chart.setDescription(description);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(scoreReceiver, new IntentFilter("com.example.SCORE_UPDATE"), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(scoreReceiver, new IntentFilter("com.example.SCORE_UPDATE"));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(scoreReceiver);
    }
}
