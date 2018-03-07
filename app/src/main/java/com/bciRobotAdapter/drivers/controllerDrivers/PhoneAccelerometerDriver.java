package com.bciRobotAdapter.drivers.controllerDrivers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.bciRobotAdapter.AdapterActivity;
import com.bciRobotAdapter.drivers.abstractDrivers.AbstractController;

public class PhoneAccelerometerDriver extends AbstractController implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;

    public PhoneAccelerometerDriver(AdapterActivity adapterActivity, boolean isAuxiliar) {
        super(adapterActivity, isAuxiliar);
    }

    @Override
    public void disconnect() {
        mSensor = null;
        mSensorManager = null;
        notifyControllerConnected(false);
    }

    //"Connect" to the device sensors
    @Override
    public void searchAndConnect() {
        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);

        assert mSensorManager != null;
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        notifyControllerConnected(true);
    }

    @Override
    public void stopSearching() {

    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent event) {

        //Get accelerometer values and print them
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        moveRobotForward(0, 0.1);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
