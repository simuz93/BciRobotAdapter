package com.multiDevCompApp.drivers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.multiDevCompApp.drivers.interfaces.AdapterActivity;
import com.multiDevCompApp.drivers.interfaces.Controller;

public class PhoneAccelerometerDriver implements Controller, SensorEventListener {

        private AdapterActivity adapterActivity;

        private SensorManager mSensorManager;
        private Sensor mSensor;

        public PhoneAccelerometerDriver(AdapterActivity adapterActivity) {
            this.adapterActivity = adapterActivity;
        }


    @Override
    public void disconnect() {
        mSensor = null;
        mSensorManager = null;
        adapterActivity.onControllerConnected(false);
    }

    @Override
    public void startSearching() {
        mSensorManager = (SensorManager) adapterActivity.getActivity().getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        assert mSensorManager != null;
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        adapterActivity.onControllerConnected(true);
    }

    @Override
    public void stopSearching() {

    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent event) {

        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        adapterActivity.log(1, "X: "+String.format("%.2f", x)+"Y "+String.format("%.2f", y)+"Z: "+String.format("%.2f", z));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
