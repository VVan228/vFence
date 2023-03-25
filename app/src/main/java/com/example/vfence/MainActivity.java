package com.example.vfence;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mrx.indoorservice.api.IndoorService;
import com.mrx.indoorservice.domain.model.BeaconsEnvironmentInfo;
import com.mrx.indoorservice.domain.model.Point;
import com.mrx.indoorservice.domain.model.PositionInfo;
import com.mrx.indoorservice.domain.model.StateEnvironment;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    IndoorService indoorService;

    Button buttonStart;
    TextView textViewBeacons;
    TextView textViewPosition;
    TextView textViewAzimuth;

    SensorManager sensorManager;
    Sensor sensorAccel;
    Sensor sensorMagnet;
    ImageView line;
    TextView rotate;
    int rotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                System.out.println(e.getMessage());
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getDeviceOrientation();
                        getActualDeviceOrientation();
                        showInfo();
                    }
                });
            }
        };
        timer.schedule(task, 0, 50);
        WindowManager windowManager = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
        Display display = windowManager.getDefaultDisplay();
        rotation = display.getRotation();

    }
    String format(float values[]) {
        return String.format("%1$.1f\t\t%2$.1f\t\t%3$.1f", values[0], values[1], values[2]);
    }

    void showInfo() {
        System.out.println("Orientation : " + format(valuesResult)+"\nOrientation 2: " + format(valuesResult2));
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
        return;
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