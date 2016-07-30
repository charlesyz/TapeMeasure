package com.placeholder.tapemeasure;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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

    private TextView xText, yText, zText, velocityText, calibrateText, distanceText;
    private Sensor mySensor;
    private SensorManager SM;
    private double yAccel = 0.0;
    private double yAccelOld = 0.0;
    private boolean isUpdated = false;
    private double velocity = 0.0;
    private double distance = 0.0;
    private double distanceChange = 0.0;
    private double posDead = -0.1;
    private double negDead = -0.18;
    private double calibrate = 0.0;
    private int timeDiff = 0;
    private int stopCounter = 0;
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
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_FASTEST);

        /*  SENSOR_DELAY_FASTEST 0 microsecond
            SENSOR_DELAY_GAME 20,000 microsecond
            SENSOR_DELAY_UI 60,000 microsecond
            SENSOR_DELAY_NORMAL 200,000 microseconds(200 milliseconds)
        */

        startTimerCounter();

        // Assign TextView
        xText = (TextView)findViewById(R.id.xText);
        yText = (TextView)findViewById(R.id.yText);
        zText = (TextView)findViewById(R.id.zText);
        velocityText = (TextView)findViewById(R.id.velocityText);
        calibrateText = (TextView)findViewById(R.id.calibrateText);
        distanceText = (TextView)findViewById(R.id.distanceText);

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
                stopCounter = 0;
                velocity = 0.0;
                distance = 0.0;
                xText.setText("X");
                yText.setText("Y");
                zText.setText("Z");
                velocityText.setText("Velocity");
                distanceText.setText("Distance");
            }
        });

        // "CALIBRATE" button
        Button button_calibrate = (Button) (findViewById(R.id.button_calibrate));
        button_calibrate.setOnClickListener(new View.OnClickListener() {

            // timer finds lowest and highest bounds during calibration
            @Override
            public void onClick(View view) {

                isOn = true;
                // reset
                posDead = -99.9;
                negDead = 99.9;
                CountDownTimer calibrateTimer;
                calibrateTimer = new CountDownTimer(10000, 1) {
                    public void onTick(long millisUntilFinished) {
                        if (yAccel > posDead){
                            posDead = yAccel;
                        }
                        if (yAccel < negDead){
                            negDead = yAccel;
                        }
                        calibrateText.setText(String.format("posDead = %.3f\n negDead =  %.3f", posDead, negDead));
                    }

                    public void onFinish() {
                        calibrate = 0 - yAccel;
                        // calibrate = 0 - (posDead + negDead) / 2;
                        calibrateText.setText(String.format("Calibration Completed!\nPosDead = %.3f \nnegDead = %.3f \nCalibrate = %.3f", posDead, negDead, calibrate));
                        isOn = false;
                        // Reset text and vars
                        timeDiff = 0;
                        stopCounter = 0;
                        velocity = 0.0;
                        distance = 0.0;
                        xText.setText("X");
                        yText.setText("Y");
                        zText.setText("Z");
                        velocityText.setText("Velocity");
                        distanceText.setText("Distance");
                    }
                }.start();

            }
        });

        // reading


    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // set acceleration
        //yAccelOld = yAccel;
        yAccel = sensorEvent.values[1];

        isUpdated = true;

        if (isOn) {
            // check dead zone and calculate velocity and distance
            if (yAccel < negDead || yAccel > posDead){
                if (stopCounter > 0){
                    stopCounter--;
                }

                distanceChange = velocity * ((double) timeDiff / 1000) + (yAccel * Math.pow(((double) timeDiff / 1000), 2))/ 2;
                distance += distanceChange;
                velocity += (yAccel + calibrate) * ((double) timeDiff / 1000);
            }
            // reset velocity if in deadzone for more than 5 ms
            else{
                stopCounter++;
            }
            if (stopCounter > 5){
                stopCounter = 0;
                velocity = 0;
            }
            xText.setText(String.format("X: %f", sensorEvent.values[0] + calibrate));
            yText.setText(String.format("Y: %f", sensorEvent.values[1] + calibrate));
            zText.setText(String.format("Z: %f", sensorEvent.values[2] + calibrate));
            velocityText.setText(String.format("Velocity: %.5f cm/s", velocity * 100));
            distanceText.setText(String.format("Distance: %.5f cm", distance * 100));
            Log.i(TAG, "TimeDiff: " + timeDiff);

            // reset
            timeDiff = 0;
        }
    }

    public void startTimerCounter() {

        TimerTask_Counter counter = new TimerTask_Counter();
        //set a new Timer\\\
        timer = new Timer();
        timer.schedule(counter, 0, 1);
    }

    // ever 1 ms checks for change in yAccel and adjust timeDiff
    class TimerTask_Counter extends TimerTask {
        public void run() {
            if (isOn){
                // check time between updates
                if (!isUpdated){
                    timeDiff++;
                }
                else {
                    isUpdated = false;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // not used
    }
}
