package com.bciRobotAdapter.drivers.abstractDrivers;

import android.app.Activity;
import android.content.Context;

import com.bciRobotAdapter.AdapterActivity;
import com.bciRobotAdapter.drivers.interfaces.Robot;

//Abstract robot driver. A new robot driver should extends this class.
//However, it's also possible to directly implement the Robot interface, manually managing every method needed
public abstract class AbstractRobot implements Robot {

    private final AdapterActivity adapterActivity; //The main activity

    private int FREQUENCY; //Frequency (in Hertz) used by the robot to receive command packets. Default: every packet received is used.
    private boolean needPacket = true; //True if, according to FREQUENCY, the robot is waiting for a packet

    public AbstractRobot(AdapterActivity adapterActivity) {
        this.adapterActivity = adapterActivity;
    }

    //Set a receiving packets frequency and run a thread to manage it through the needPacket variable
    //If not set, the thread will not start and the robot will try to use every packet incoming
    public void setFrequency(int frequency_Hz){

        if(frequency_Hz<1) FREQUENCY = 1; //1Hz minimum
        else if(frequency_Hz>1000) FREQUENCY = 1000; //1000Hz maximum
        else this.FREQUENCY = frequency_Hz;

        Thread frequencyThread = new Thread() {
            public void run() {
                int millisecondsPerPacket = 1000/FREQUENCY;

                while (true) {
                    try {
                        sleep(millisecondsPerPacket);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    needPacket = true;
                }
            }
        };
        frequencyThread.start();
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

    //Print in the robot textView the String "toWrite"
    public void setRobotLog(String toWrite) {
        this.adapterActivity.setRobotLog(toWrite);
    }

    //Print in the main textview the String "toWrite"
    public void setGeneralLog(String toWrite) {
        this.adapterActivity.setGeneralLog(toWrite);
    }

    //Interface methods calling robot commands according to FREQUENCY values
    @Override
    public void moveRobotForward(double rotation, double speed) {
        if(needPacket) moveForward(rotation, speed);
        needPacket=false;
    }
    @Override
    public void turnRobotL(double rotation) {
        if(needPacket) turnL(rotation);
        needPacket=false;
    }
    @Override
    public void turnRobotR(double rotation) {
        if(needPacket) turnR(rotation);
        needPacket=false;
    }
    @Override
    public void stopRobot() {
        if(needPacket) stop();
        needPacket=false;
    }
    @Override
    public void setRobotLedRed() {
        if(needPacket) setLedRed();
        needPacket=false;
    }
    @Override
    public void setRobotLedBlue() {
        if(needPacket) setLedBlue();
        needPacket=false;
    }
    @Override
    public void setRobotLedGreen() {
        if(needPacket) setLedGreen();
        needPacket=false;
    }
    @Override
    public void setRobotLedYellow() {
        if(needPacket) setLedYellow();
        needPacket=false;
    }
    @Override
    public void setRobotLedWhite() {
        if(needPacket) setLedWhite();
        needPacket=false;
    }
    @Override
    public void setRobotLedOff() {
        if(needPacket) setLedOff();
        needPacket=false;
    }
    @Override
    public abstract void disconnect();
    @Override
    public abstract void searchAndConnect();
    @Override
    public abstract void stopSearching();

    //Abstract methods
    public abstract void moveForward(double rotation, double speed);
    public abstract void stop();
    public abstract void turnL(double rotation);
    public abstract void turnR(double rotation);
    public abstract void setLedRed();
    public abstract void setLedBlue();
    public abstract void setLedGreen();
    public abstract void setLedYellow();
    public abstract void setLedWhite();
    public abstract void setLedOff();
}
