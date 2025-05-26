package com.example.bankingapp;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class FaceAdapter extends BaseAdapter {

    private Context context;
    private List<FaceItem> faceList;

    public FaceAdapter(Context context, List<FaceItem> faceList) {
        this.context = context;
        this.faceList = faceList;
    }

    @Override
    public int getCount() {
        return faceList.size();
    }

    @Override
    public Object getItem(int i) {
        return faceList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.face_grid_item, parent, false);
        }

        ImageView imageView = view.findViewById(R.id.faceImage);
        TextView textView = view.findViewById(R.id.faceName);

        FaceItem item = faceList.get(i);
        imageView.setImageBitmap(BitmapFactory.decodeFile(item.imagePath));
        textView.setText(item.name);

        return view;
    }
}
