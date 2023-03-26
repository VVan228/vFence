package com.example.vfence.camera;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.Image;
import android.os.Bundle;
import android.util.Size;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.vfence.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 83854;

    private ImageView preview;

    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    YUVtoRGB translator = new YUVtoRGB();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preview = findViewById(R.id.viewFinder);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        } else {
            initializeCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeCamera();
        }
    }

    private void initializeCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                            .setTargetResolution(new Size(1024, 768))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();

                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();

                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(CameraActivity.this),
                            image -> {
                                Image img = image.getImage();
                                Bitmap bitmap = translator.translateYUV(img, CameraActivity.this);

                                int size = bitmap.getWidth() * bitmap.getHeight();
                                int[] pixels = new int[size];
                                bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0,
                                        bitmap.getWidth(), bitmap.getHeight());

                                for (int i = 0; i < size; i++) {
                                    int color = pixels[i];
                                    int r = color >> 16 & 0xff;
                                    int g = color >> 8 & 0xff;
                                    int b = color & 0xff;
                                    int gray = (r + g + b) / 3;
                                    pixels[i] = 0xff000000 | gray << 16 | gray << 8 | gray;
                                }
                                bitmap.setPixels(pixels, 0, bitmap.getWidth(), 0, 0,
                                        bitmap.getWidth(), bitmap.getHeight());

                                preview.setRotation(image.getImageInfo().getRotationDegrees());
                                preview.setImageBitmap(bitmap);

                                Canvas canvas = new Canvas(bitmap);
                                canvas.drawColor(Color.TRANSPARENT);
                                Paint paint = new Paint();
                                paint.setColor(Color.GREEN); // установим белый цвет
                                paint.setStrokeWidth(5);
                                paint.setStyle(Paint.Style.FILL); // заливаем
                                paint.setAntiAlias(true);
                                canvas.drawLine(0,0, bitmap.getWidth(), bitmap.getHeight(), paint);

                                image.close();
                            });

                    cameraProvider.bindToLifecycle(CameraActivity.this, cameraSelector, imageAnalysis);

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }
}
