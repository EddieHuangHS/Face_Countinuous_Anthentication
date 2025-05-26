package com.example.bankingapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FaceDatabaseActivity extends AppCompatActivity {

    private GridView gridView;
    private List<FaceItem> faceList = new ArrayList<>();
    private FaceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_database);

        gridView = findViewById(R.id.gridView);
        loadFaces();
        adapter = new FaceAdapter(this, faceList);
        gridView.setAdapter(adapter);
    }

    private void loadFaces() {
        File dir = new File(getFilesDir(), "face_images");
        if (!dir.exists()) return;

        for (File imgFile : dir.listFiles()) {
            String fileName = imgFile.getName();
            String name = fileName.substring(0, fileName.lastIndexOf('.'));
            faceList.add(new FaceItem(name, imgFile.getAbsolutePath()));
        }
    }
}
