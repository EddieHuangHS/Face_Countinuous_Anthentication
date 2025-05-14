package com.example.faceauth;

import android.content.Context;
import android.graphics.Bitmap;

public class FaceProcessor {
    public FaceProcessor(Context context) {
        // 后续：可加载 ArcFace 模型等
    }

    public float verify(Bitmap bitmap) {
        // 暂时模拟识别分数：随机 0.6 ~ 0.9
        return (float) (0.6 + Math.random() * 0.3);
    }
}
