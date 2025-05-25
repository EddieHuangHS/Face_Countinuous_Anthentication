package com.example.faceauth;

import android.content.res.AssetManager;

public class FaceRecognizer {
    static {
        System.loadLibrary("arcface");
    }

    // 传入 AssetManager 供 native 加载
    public native void init(AssetManager mgr, String paramName, String binName);

    public native float[] getEmbedding(byte[] bgrData, int width, int height);
    public native float cosineSimilarity(float[] emb1, float[] emb2);
}
