package com.example.vfence;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.os.Bundle;
import android.widget.Button;
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

public class MainActivity extends AppCompatActivity {

    IndoorService indoorService;

    Button buttonStart;
    TextView textViewBeacons;
    TextView textViewPosition;
    TextView textViewAzimuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        indoorService = IndoorService.INSTANCE.getInstance(this);
        indoorService.getPosition().setEnvironment(stateEnvironment);

        buttonStart = findViewById(R.id.btn_control);
        textViewBeacons = findViewById(R.id.textView_beacons);
        textViewPosition = findViewById(R.id.textView_position);
        textViewAzimuth = findViewById(R.id.textView_azimuth);

        indoorService.getBeaconsEnvironment().getRangingViewModel().observe(this, observerForIndoorServiceBeacons);
        indoorService.getAzimuthManager().getAzimuthViewModel().observe(this, observerForIndoorServiceAzimuth);

        buttonStart.setOnClickListener(it -> {
            indoorService.getBeaconsEnvironment().startRanging();
            indoorService.getAzimuthManager().startListen();
        });
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
        textViewBeacons.setText(temp);

        // Определение позиции далее
        if (beacons.size() > 2) {
            try {
                PositionInfo position = indoorService.getPosition().getPosition(indoorService.getMapper().fromBeaconsEnvironmentInfoToEnvironmentInfo(beacons));
                textViewPosition.setText("Position: (" + position.getPosition().getX() + ", " + position.getPosition().getY() + ")");

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
    }
}