package com.example.faceauth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.Image;
import android.util.Log;
import android.util.Size;

import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import androidx.camera.core.ImageProxy;
import com.google.mlkit.vision.common.InputImage;

import android.graphics.YuvImage;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import java.io.ByteArrayOutputStream;


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

//                imageAnalysis.setAnalyzer(executor, image -> {
//                    Bitmap bitmap = imageToBitmap(image);
//                    if (bitmap != null) {
//                        callback.onFrame(bitmap);
//                    }
//                    image.close();
//                });
                // 在 imageAnalysis.setAnalyzer 中添加：
                imageAnalysis.setAnalyzer(executor, image -> {
                    Log.d("CameraXWrapper", "Image frame received");
                    Bitmap bitmap = imageToBitmap(image);
                    if (bitmap != null) {
                        callback.onFrame(bitmap);
                    } else {
                        Log.w("CameraXWrapper", "Bitmap is null, frame dropped");
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


    @androidx.camera.core.ExperimentalGetImage
    private Bitmap imageToBitmap(ImageProxy image) {
        Image mediaImage = image.getImage();
        if (mediaImage == null) return null;

        int width = image.getWidth();
        int height = image.getHeight();

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize); // 注意：顺序是 VU，而不是 UV
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, out);
        byte[] jpegBytes = out.toByteArray();

        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.length);
    }


}
