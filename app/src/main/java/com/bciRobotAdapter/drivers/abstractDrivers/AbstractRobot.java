package com.bciRobotAdapter.drivers.abstractDrivers;

import android.app.Activity;
import android.content.Context;

import com.bciRobotAdapter.AdapterActivity;
import com.bciRobotAdapter.drivers.interfaces.Robot;

//Abstract robot driver. A new robot driver should extends this class.
//However, it's also possible to directly implement the Robot interface, manually managing every method needed
public abstract class AbstractRobot implements Robot {

    private final AdapterActivity adapterActivity; //The main activity

    public AbstractRobot(AdapterActivity adapterActivity) {
        this.adapterActivity = adapterActivity;
    }

    //Returns the adapterActivity instance in use
    public Activity getAdapterActivity() {
        return this.adapterActivity.getActivity();
    }

    //Returns the adapterActivity context
    public Context getContext() {
        return this.adapterActivity.getContext();
    }

    //Notify the adapter that the robot is connected (connected = true) or disconnected (connected = false).
    // You MUST call this method everytime the robot connection state changes
    public void notifyRobotConnected(boolean connected) {
        this.adapterActivity.onRobotConnected(connected);
    }

    public void setFrequency(int frequency_Hz) {
        adapterActivity.setFrequency(frequency_Hz);
    }

    //Print in the robot textView the String "toWrite"
    public void setRobotLog(String toWrite) {
        this.adapterActivity.setRobotLog(toWrite);
    }
    public void setRobotOutput(String toWrite) {
        this.adapterActivity.setRobotOutput(toWrite);
    }

    //Interface methods

    @Override
    public abstract void disconnect();
    @Override
    public abstract void searchAndConnect();
    @Override
    public abstract void stopSearching();

    @Override
    public abstract void moveForward(double rotation, double speed);
    @Override
    public abstract void stop();
    @Override
    public abstract void turnL(double rotation);
    @Override
    public abstract void turnR(double rotation);
    @Override
    public abstract void setLedRed();
    @Override
    public abstract void setLedBlue();
    @Override
    public abstract void setLedGreen();
    @Override
    public abstract void setLedYellow();
    @Override
    public abstract void setLedWhite();
    @Override
    public abstract void setLedOff();
}
