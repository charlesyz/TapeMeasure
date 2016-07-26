package com.placeholder.tapemeasure;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

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
    private double yAccelOld = 0.0;
    private double velocity = 0.0;
    private double posDead = -0.1;
    private double negDead = -0.2;
    private int timeDiff = 0;
    private boolean isOn = false;

    private Timer timer;
    private static final String TAG = "timeDiff";

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

        startTimer();

        // Assign TextView
        xText = (TextView)findViewById(R.id.xText);
        yText = (TextView)findViewById(R.id.yText);
        zText = (TextView)findViewById(R.id.zText);
        velocityText = (TextView)findViewById(R.id.velocityText);

        // Assigning on/off buttons
        // the "ON" button
        Button button_on = (Button) (findViewById(R.id.button_velocity_on));
        button_on.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                isOn = true;
            }
        });
        // "OFF" button
        Button button_off = (Button) (findViewById(R.id.button_velocity_off));
        button_off.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                isOn = false;
                // Reset text and vars
                timeDiff = 0;
                velocity = 0.0;
                xText.setText("X");
                yText.setText("Y");
                zText.setText("Z");
                velocityText.setText("Velocity");
            }
        });
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (isOn) {
            // set acceleration
            yAccelOld = yAccel;
            yAccel = sensorEvent.values[1];

            // check dead zone and calculate velocity
            if (yAccel < negDead || yAccel > posDead){
                velocity += (yAccelOld + 0.14) * ((double) timeDiff / 1000);
            }
            xText.setText("X: " + sensorEvent.values[0]);
            yText.setText("Y: " + sensorEvent.values[1]);
            zText.setText("Z: " + sensorEvent.values[2]);
            velocityText.setText("Velocity: " + velocity + "m/s");
            Log.i(TAG, "TimeDiff = " + timeDiff);
            // reset
            timeDiff = 0;
        }
    }

    public void startTimer() {

        myTimerTask counter = new myTimerTask();
        //set a new Timer
        timer = new Timer();
        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(counter, 0, 1); //
    }

    // ever 1 ms checks for change in yAccel and adjust timeDiff
    class myTimerTask extends TimerTask {
        public void run() {
            if (isOn){
                if (yAccelOld == yAccel){
                    timeDiff++;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // not used
    }
}
