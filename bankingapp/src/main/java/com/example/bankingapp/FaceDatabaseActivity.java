package com.example.bankingapp;

import android.app.AlertDialog;
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

        // Long press to delete user
        gridView.setOnItemLongClickListener((parent, view, position, id) -> {
            FaceItem item = faceList.get(position);

            new AlertDialog.Builder(FaceDatabaseActivity.this)
                    .setTitle("Delete User")
//                    .setTitle("删除用户")
                    .setMessage("Confirm to delete user \"" + item.name + "\" ？")
//                    .setMessage("确认删除用户 \"" + item.name + "\" 吗？")
                    .setPositiveButton("Delete", (dialog, which) -> {
//                    .setPositiveButton("删除", (dialog, which) -> {
                        deleteUser(item.name);
                        faceList.remove(position);
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("Cancel", null)
//                    .setNegativeButton("取消", null)
                    .show();

            return true;
        });
    }

    private void loadFaces() {
        File dir = new File(getFilesDir(), "face_images");
        if (!dir.exists()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File imgFile : files) {
            String fileName = imgFile.getName();
            if (!fileName.endsWith(".jpg")) continue;

            String name = fileName.substring(0, fileName.lastIndexOf('.'));
            faceList.add(new FaceItem(name, imgFile.getAbsolutePath()));
        }
    }

    private void deleteUser(String username) {
        File imgFile = new File(getFilesDir(), "face_images/" + username + ".jpg");
        File featFile = new File(getFilesDir(), username + "_feature.txt");

        if (imgFile.exists()) imgFile.delete();
        if (featFile.exists()) featFile.delete();
    }
}
