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

    private TextView xText, yText, zText, velocityText, calibrateText;
    private Sensor mySensor;
    private SensorManager SM;
    private double yAccel = 0.0;
    private double yAccelOld = 0.0;
    private boolean isUpdated = false;
    private double velocity = 0.0;
    private double posDead = -0.1;
    private double negDead = -0.18;
    private double calibrate = 0.0;
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

        startTimerCounter();

        // Assign TextView
        xText = (TextView)findViewById(R.id.xText);
        yText = (TextView)findViewById(R.id.yText);
        zText = (TextView)findViewById(R.id.zText);
        velocityText = (TextView)findViewById(R.id.velocityText);
        calibrateText = (TextView)findViewById(R.id.calibrateText);

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

        // "CALIBRATE" button
        Button button_calibrate = (Button) (findViewById(R.id.button_calibrate));
        button_calibrate.setOnClickListener(new View.OnClickListener() {

            // timer finds lowest and highest bounds during calibration
            @Override
            public void onClick(View view) {
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
                        calibrateText.setText( String.format("posDead = %.3f\n negDead =  %.3f", posDead, negDead));
                    }

                    public void onFinish() {
                        calibrate = 0 - (posDead + negDead) / 2;
                        calibrateText.setText(String.format("Calibration Completed!\nPosDead = %.3f \nnegDead = %.3f \nCalibrate = %.3f", posDead, negDead, calibrate));

                    }
                }.start();

            }
        });
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (isOn) {
            // set acceleration
            yAccelOld = yAccel;
            yAccel = sensorEvent.values[1];

            isUpdated = true;

            // check dead zone and calculate velocity.
            if (yAccel < negDead || yAccel > posDead){
                velocity += (yAccelOld + calibrate) * ((double) timeDiff / 1000);
            }
            // reset velocity if in deadzone
            else{
                velocity = 0;
            }
            xText.setText( String.format("X: %f", sensorEvent.values[0] + calibrate) );
            yText.setText( String.format("Y: %f", sensorEvent.values[1] + calibrate) );
            zText.setText( String.format("Z: %f", sensorEvent.values[2] + calibrate) );
            velocityText.setText( String.format("Velocity: %.5f", velocity) );
            Log.i(TAG, "TimeDiff: " + timeDiff);
            // reset
            timeDiff = 0;
        }
    }

    public void startTimerCounter() {

        TimerTask_Counter counter = new TimerTask_Counter();
        //set a new Timer
        timer = new Timer();
        timer.schedule(counter, 0, 1); //
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
