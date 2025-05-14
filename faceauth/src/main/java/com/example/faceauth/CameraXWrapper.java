package com.example.faceauth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.Image;
import android.util.Size;

import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraXWrapper {
    public interface FrameCallback {
        void onFrame(Bitmap bitmap);
    }

    private final Context context;
    private final FrameCallback callback;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CameraXWrapper(Context context, FrameCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @SuppressLint("UnsafeOptInUsageError")
    public void start() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(context);
        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(executor, image -> {
                    Bitmap bitmap = imageToBitmap(image);
                    if (bitmap != null) {
                        callback.onFrame(bitmap);
                    }
                    image.close();
                });

                CameraSelector selector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        DummyLifecycleOwner.INSTANCE, selector, imageAnalysis
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }

//    private Bitmap imageToBitmap(ImageProxy image) {
//        ImageProxy.PlaneProxy[] planes = image.getPlanes();
//        if (planes.length == 0) return null;
//
//        ByteBuffer buffer = planes[0].getBuffer();
//        byte[] bytes = new byte[buffer.remaining()];
//        buffer.get(bytes);
//
//        return BitmapUtils.convertYUV420ToBitmap(image);
//    }

    // TODO: 暂时跳过图像转换，后期再处理
    private Bitmap imageToBitmap(ImageProxy image) {
        return null; // 暂时跳过图像转换
    }

}
