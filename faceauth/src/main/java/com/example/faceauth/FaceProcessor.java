package com.example.faceauth;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.nio.file.Files;

public class FaceProcessor {
    private final FaceRecognizer faceRecognizer;
    private final float[] referenceEmbedding;
    private String currentUserName = "unknown";

    public FaceProcessor(Context context) {
        faceRecognizer = new FaceRecognizer();
        faceRecognizer.init(context.getAssets(), "arcface-opt.param", "arcface-opt.bin");
        referenceEmbedding = loadEmbeddingFromFile(context);
    }

    public float verify(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        byte[] bgr = new byte[width * height * 3];
        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];
            bgr[i * 3] = (byte) ((p >> 16) & 0xFF);     // R
            bgr[i * 3 + 1] = (byte) ((p >> 8) & 0xFF);   // G
            bgr[i * 3 + 2] = (byte) (p & 0xFF);          // B
        }

        float[] currentEmbedding = faceRecognizer.getEmbedding(bgr, width, height);
        if (currentEmbedding == null || currentEmbedding.length != 512) {
            Log.e("FaceProcessor", "Invalid embedding");
            return 0.0f;
        }

        float score = faceRecognizer.cosineSimilarity(currentEmbedding, referenceEmbedding);
        Log.d("FaceProcessor", "Similarity score with " + currentUserName + ": " + score);
        return score;
    }

    private float[] loadEmbeddingFromFile(Context context) {
        File dir = context.getFilesDir();
        File[] files = dir.listFiles();
        if (files == null) return null;

        float maxSim = -1f;
        String bestMatchUser = "unknown";
        float[] bestEmbedding = new float[512];

        for (File file : files) {
            if (file.getName().endsWith("_feature.txt")) {
                try {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    String[] parts = content.trim().split("\\s+");
                    if (parts.length != 512) continue;

                    float[] candidate = new float[512];
                    for (int i = 0; i < 512; i++) {
                        candidate[i] = Float.parseFloat(parts[i]);
                    }

                    // 用随机向量或当前帧临时比对（这里可忽略，真正比对发生在 verify() 中）
                    // 暂存候选向量（真实比对放 verify()）

                    // 如果你需要一开始就知道是哪个用户注册的，可以记录名称：
                    Log.d("FaceProcessor", "Loaded embedding from: " + file.getName());

                    // return 提前，先返回第一个用户（可改为多用户比对）
                    this.currentUserName = file.getName().replace("_feature.txt", "");
                    return candidate;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return new float[512];
    }

}
