package com.example.faceauth;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.nio.file.Files;

public class FaceProcessor {
    private final FaceRecognizer faceRecognizer;
    private final float[] referenceEmbedding;
    private String currentUserName;

    public FaceProcessor(Context context, String userName) {
        this.currentUserName = userName;
        this.faceRecognizer = new FaceRecognizer();
        faceRecognizer.init(context.getAssets(), "arcface-opt.param", "arcface-opt.bin");
        this.referenceEmbedding = loadEmbeddingForUser(context, userName);
    }

    private float[] loadEmbeddingForUser(Context context, String userName) {
        File file = new File(context.getFilesDir(), userName + "_feature.txt");
        if (!file.exists()) return new float[512];

        try {
            String[] values = new String(Files.readAllBytes(file.toPath())).trim().split("\\s+");
            float[] vec = new float[512];
            for (int i = 0; i < 512; i++) vec[i] = Float.parseFloat(values[i]);
            return vec;
        } catch (Exception e) {
            e.printStackTrace();
            return new float[512];
        }
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

}
