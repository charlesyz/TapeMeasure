package com.placeholder.tapemeasure;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class MeasuringTape extends Activity implements SensorEventListener{

    private TextView xText, yText, zText, velocityText;
    private Sensor mySensor;
    private SensorManager SM;
    private double yAccel = 0.0;
    private double velocity = 0.0;
    private double posDead = 0.03;
    private double negDead = -0.03;
    private int timeDiff = 0;
    private boolean isOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measuring_tape);

        // Create our Sensor Manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);
        // Accelerometer Sensor
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Register sensor Listener
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_GAME);

        /*  SENSOR_DELAY_FASTEST 0 microsecond
            SENSOR_DELAY_GAME 20,000 microsecond
            SENSOR_DELAY_UI 60,000 microsecond
            SENSOR_DELAY_NORMAL 200,000 microseconds(200 milliseconds)
        */

        // Assign TextView
        xText = (TextView)findViewById(R.id.xText);
        yText = (TextView)findViewById(R.id.yText);
        zText = (TextView)findViewById(R.id.zText);
        velocityText = (TextView)findViewById(R.id.velocityText);

        // Assigning on/off buttons
        Button button_on = (Button) (findViewById(R.id.button_velocity_on));
        button_on.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                isOn = true;
            }
        });

        Button button_off = (Button) (findViewById(R.id.button_velocity_off));
        button_off.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                isOn = false;
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (isOn) {
            // set acceleration
            yAccel = sensorEvent.values[1];

            // check dead zone and calculate velocity
            if (yAccel < negDead && yAccel > posDead){
                velocity += yAccel * 0.02;
            }

            xText.setText("X: " + sensorEvent.values[0]);
            yText.setText("Y: " + sensorEvent.values[1]);
            zText.setText("Z: " + sensorEvent.values[2]);
            velocityText.setText("Velocity: " + velocity);
        }
        else{
            velocity = 0.0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // not used
    }
}
