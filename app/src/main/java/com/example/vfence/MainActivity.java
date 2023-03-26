package com.example.vfence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vfence.ar.ArCanvas;
import com.example.vfence.ar.AvgPoint;
import com.example.vfence.ar.Vector2d;
import com.example.vfence.ar.Vector3d;
import com.example.vfence.camera.YUVtoRGB;
import com.google.common.util.concurrent.ListenableFuture;
import com.mrx.indoorservice.api.IndoorService;
import com.mrx.indoorservice.domain.model.BeaconsEnvironmentInfo;
import com.mrx.indoorservice.domain.model.Point;
import com.mrx.indoorservice.domain.model.PositionInfo;
import com.mrx.indoorservice.domain.model.StateEnvironment;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    IndoorService indoorService;

    Button buttonStart;
    TextView textViewBeacons;
    TextView textViewPosition;
    TextView textViewAzimuth;

    SensorManager sensorManager;
    Sensor sensorAccel;
    Sensor sensorMagnet;
    int rotation;

    DrawView canvas;
    private List<AvgPoint> points;
    Vector3d point1 = new Vector3d(1,0.5,-0.3);
    Vector3d point2 = new Vector3d(1,20,0.3);
    Vector3d position = new Vector3d(-4,0,0);


    //camera
    private static final int PERMISSION_REQUEST_CAMERA = 83854;
    private ImageView preview;
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    YUVtoRGB translator = new YUVtoRGB();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canvas = new DrawView(this);
        //setContentView(canvas);
        setContentView(R.layout.activity_main);
        ArCanvas.getInstance().init(Arrays.asList(point1, point2), Math.PI/2, Math.PI/2);

//        canvas.setOnClickListener(view -> {
//        });


        indoorService = IndoorService.INSTANCE.getInstance(this);
        indoorService.getPosition().setEnvironment(stateEnvironment);

        /*buttonStart = findViewById(R.id.btn_control);
        textViewBeacons = findViewById(R.id.textView_beacons);
        textViewPosition = findViewById(R.id.textView_position);
        textViewAzimuth = findViewById(R.id.textView_azimuth);*/

        indoorService.getBeaconsEnvironment().getRangingViewModel().observe(this, observerForIndoorServiceBeacons);
        indoorService.getAzimuthManager().getAzimuthViewModel().observe(this, observerForIndoorServiceAzimuth);

        /*buttonStart.setOnClickListener(it -> {
            indoorService.getBeaconsEnvironment().startRanging();
            indoorService.getAzimuthManager().startListen();
        });*/

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        //camera
        preview = findViewById(R.id.viewFinder);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
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

                    //Preview preview = new Preview.Builder().build();

                    //ImageCapture imageCapture = new ImageCapture.Builder().build();

                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                            .setTargetResolution(new Size(1024, 768))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();

                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();

                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(MainActivity.this),
                            image -> {
                                Image img = image.getImage();
                                Bitmap bitmap = translator.translateYUV(img, MainActivity.this);

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
//                                canvas.drawColor(Color.TRANSPARENT);
//                                Paint paint = new Paint();
//                                paint.setColor(Color.GREEN); // установим белый цвет
//                                paint.setStrokeWidth(5);
//                                paint.setStyle(Paint.Style.FILL); // заливаем
//                                paint.setAntiAlias(true);
//
//                                canvas.drawLine(0,0, bitmap.getWidth(), bitmap.getHeight(), paint);
                                drawCircle(canvas);
                                image.close();
                            });

                    cameraProvider.bindToLifecycle(MainActivity.this, cameraSelector, imageAnalysis);

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }
    public void drawCircle(Canvas canvas){
        canvas.drawColor(Color.TRANSPARENT);
        Paint paint = new Paint();
        paint.setColor(Color.GREEN); // установим белый цвет
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.FILL); // заливаем
        paint.setAntiAlias(true);

        for(AvgPoint p: points){
            if(p.isVisible()){
                canvas.drawCircle((float) (canvas.getWidth()*p.getPoint().getX()),
                        (float) (canvas.getHeight()*p.getPoint().getY()),
                        20, paint);
            }
        }

    }

    // Функция обратного вызова
    Observer<Collection<Beacon>> observer = beacons -> {
        textViewBeacons.setText("Ranged: " + beacons.size() + " beacons");
    };

    Observer<Collection<BeaconsEnvironmentInfo>> observerForIndoorServiceBeacons = beacons -> {
        String temp = "Ranged: " + beacons.size() + " beacons\n";
        for (BeaconsEnvironmentInfo beacon : beacons) {
            temp += beacon.getBeaconId() + " -> " + beacon.getDistance() + "\n";
        }
        //textViewBeacons.setText(temp);

        // Определение позиции далее
        if (beacons.size() > 2) {
            try {
                PositionInfo position = indoorService.getPosition().getPosition(indoorService.getMapper().fromBeaconsEnvironmentInfoToEnvironmentInfo(beacons));
                //textViewPosition.setText("Position: (" + position.getPosition().getX() + ", " + position.getPosition().getY() + ")");

            } catch (IllegalArgumentException e){
                //System.out.println(e.getMessage());
            }
                    }

    };

    Observer<Float> observerForIndoorServiceAzimuth = azimuth -> {
        textViewAzimuth.setText(Float.toString(azimuth));
    };

    ArrayList stateEnvironment = new ArrayList(Arrays.asList(
            new StateEnvironment("DF:6A:59:AE:F9:CC", new Point<>(0.0, 0.0)),
            new StateEnvironment("D3:81:75:66:79:B8", new Point<>(10.0, 0.0)),
            new StateEnvironment("E4:C1:3F:EF:49:D7", new Point<>(10.0, 10.0)),
            new StateEnvironment("E6:96:DA:5C:82:59", new Point<>(0.0, 10.0))
    ));

    @Override
    protected void onPause() {
        super.onPause();

        indoorService.getBeaconsEnvironment().stopRanging();
        indoorService.getAzimuthManager().stopListen();
        sensorManager.unregisterListener(listener);
    }

    Timer timer;

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorMagnet, SensorManager.SENSOR_DELAY_NORMAL);

        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    getDeviceOrientation();
                    getActualDeviceOrientation();
                    renderCanvas();
                });
            }
        };
        timer.schedule(task, 0, 10);
        WindowManager windowManager = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
        Display display = windowManager.getDefaultDisplay();
        rotation = display.getRotation();

    }
    String format(float values[]) {
        return String.format("%1$.1f\t\t%2$.1f\t\t%3$.1f", values[0], values[1], values[2]);
    }

    void renderCanvas() {

        float a1 = valuesResult2[0];
        float a2 = valuesResult2[1];
        float a3 = valuesResult2[2];

        /*Vector2d res = ar.getPointCoord(Math.PI/2, Math.PI/2,
                ar.rotateVector(point, a1, a2, a3));
        if(res == null){
            System.out.println("null!");
            canvas.setX(0f);
            canvas.setY(0f);
            canvas.invalidate();
            return;
        }

        realPoint.setPoint(res);
        Vector2d resres = realPoint.getPoint();*/
        List<AvgPoint> res = ArCanvas.getInstance().updateData(position, a1,a2,a3);

//        canvas.setPoints(res);
//        canvas.invalidate();
        points = res;
        //System.out.println("Orientation : " + format(valuesResult)+"\nOrientation 2: " + format(valuesResult2));
    }

    float[] r = new float[9];

    void getDeviceOrientation() {
        SensorManager.getRotationMatrix(r, null, valuesAccel, valuesMagnet);
        SensorManager.getOrientation(r, valuesResult);

        //valuesResult[0] = (float) Math.toDegrees(valuesResult[0]);
        //valuesResult[1] = (float) Math.toDegrees(valuesResult[1]);
        //valuesResult[2] = (float) Math.toDegrees(valuesResult[2]);
        return;
    }

    float[] inR = new float[9];
    float[] outR = new float[9];

    void getActualDeviceOrientation() {
        SensorManager.getRotationMatrix(inR, null, valuesAccel, valuesMagnet);
        int x_axis = SensorManager.AXIS_X;
        int y_axis = SensorManager.AXIS_Y;
        switch (rotation) {
            case (Surface.ROTATION_0): break;
            case (Surface.ROTATION_90):
                x_axis = SensorManager.AXIS_Y;
                y_axis = SensorManager.AXIS_MINUS_X;
                break;
            case (Surface.ROTATION_180):
                y_axis = SensorManager.AXIS_MINUS_Y;
                break;
            case (Surface.ROTATION_270):
                x_axis = SensorManager.AXIS_MINUS_Y;
                y_axis = SensorManager.AXIS_X;
                break;
            default: break;
        }
        SensorManager.remapCoordinateSystem(inR, x_axis, y_axis, outR);
        SensorManager.getOrientation(outR, valuesResult2);
        //valuesResult2[0] = (float) Math.toDegrees(valuesResult2[0]);
        //valuesResult2[1] = (float) Math.toDegrees(valuesResult2[1]);
        //valuesResult2[2] = (float) Math.toDegrees(valuesResult2[2]);
    }

    float[] valuesAccel = new float[3];
    float[] valuesMagnet = new float[3];
    float[] valuesResult = new float[3];
    float[] valuesResult2 = new float[3];


    SensorEventListener listener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    for (int i=0; i < 3; i++){
                        valuesAccel[i] = event.values[i];
                    }
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    for (int i=0; i < 3; i++){
                        valuesMagnet[i] = event.values[i];
                    }
                    break;
            }
        }
    };
}