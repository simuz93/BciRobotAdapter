package com.bciRobotAdapter.drivers.abstractDrivers;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.bciRobotAdapter.AdapterActivity;
import com.bciRobotAdapter.drivers.interfaces.Controller;

//Abstract controller driver. A new controller driver should extends this class.
//However, it's also possible to directly implement the Controller interface, manually managing every method needed
public abstract class AbstractController implements Controller {

    private boolean active = true; //If false, every movement call from the controller is ignored
    private final AdapterActivity adapterActivity;//The main activity
    private int FREQUENCY; //Packet send frequency in Hz
    private boolean forceSend = false;
    private boolean isAuxiliary = false;
    private boolean hasAuxiliary = false;

    public AbstractController(AdapterActivity adapterActivity, boolean isAuxiliary) {
        this.adapterActivity = adapterActivity;
        this.isAuxiliary = isAuxiliary;
        initForceSendThread();
    }

    private void initForceSendThread() {
        final Thread forceSendThread = new Thread() {
            public void run() {
                while(true) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    forceSend=true;
                }
            }
        };
        forceSendThread.start();
    }

    //Returns the adapterActivity instance in use
    public Activity getAdapterActivity() {
        return this.adapterActivity.getActivity();
    }

    //Returns the adapterActivity context
    public Context getContext() {
        return this.adapterActivity.getContext();
    }

    public void setHasAuxiliary(boolean hasAuxiliary) {
        this.hasAuxiliary = hasAuxiliary;
    }

    //Manage the active boolean variable
    @Override
    public void activate(boolean active) {
        this.active = active;
    }

    //Notify the adapter that the controller is connected (connected = true) or disconnected (connected = false).
    // You MUST call this method everytime the controller's connection state changes
    public void notifyControllerConnected(boolean connected) {
        if(!isAuxiliary)this.adapterActivity.onMainControllerConnected(connected);
        else this.adapterActivity.onAuxControllerConnected(connected);
    }

    //Set the send frequency incoming from the robot
    public void setFrequency(int frequency_Hz) {
        this.FREQUENCY=frequency_Hz;
    }

    //Return true if the controller should send a packet, according to FREQUENCY
    public boolean readyToSend() {
        if(forceSend) {
            forceSend = false;
            return true;
        }
        else return System.currentTimeMillis()%(1000/FREQUENCY)==0;
    }

    /*================================Print and debug methods================================*/

    //Print in the robot textView the String "toWrite"
    public void setControllerLog(String toWrite) {
        if(!isAuxiliary) this.adapterActivity.setMainControllerLog(toWrite);
        else this.adapterActivity.setAuxControllerLog(toWrite);
    }

    public void setControllerOutput(String toWrite) {
        if(!isAuxiliary) this.adapterActivity.setMainControllerOutput(toWrite);
        else this.adapterActivity.setAuxControllerOutput(toWrite);
    }
    /*======================================================================================*/

    //Interface abstract methods
    @Override
    public abstract void disconnect();
    @Override
    public abstract void searchAndConnect();
    @Override
    public abstract void stopSearching();

    //Movement methods, filtered by the active flag
    public void moveRobotForward(double rotation, double speed) {
        if(active && readyToSend()) {
            if (isAuxiliary) {
                adapterActivity.setAuxCtrlDirection(rotation);
            }
            else {
                adapterActivity.moveForward(rotation, speed);
            }
        }
    }

    public void stopRobot() {
        if(active && !isAuxiliary && readyToSend()) {
            adapterActivity.stop();
        }
    }
    public void turnRobotL(double rotation) {
        if(active&&!hasAuxiliary) {
            adapterActivity.turnL(rotation);
        }
    } //Face left
    public void turnRobotR(double rotation) {
        if(active&&!hasAuxiliary) {
            adapterActivity.turnR(rotation);
        }
    } //Face right

    //Led
    public void setRobotLedRed() {
        if(active&&!isAuxiliary) {
            adapterActivity.setLedRed();
        }
    }
    public void setRobotLedBlue() {
        if(active&&!isAuxiliary) {
            adapterActivity.setLedBlue();
        }
    }
    public void setRobotLedGreen() {
        if(active&&!isAuxiliary) {
            adapterActivity.setLedGreen();
        }
    }
    public void setRobotLedYellow() {
        if(active&&!isAuxiliary) {
            adapterActivity.setLedYellow();
        }
    }
    public void setRobotLedWhite() {
        if(active&&!isAuxiliary) {
            adapterActivity.setLedWhite();
        }
    }
    public void setRobotLedOff() {
        if(active&&!isAuxiliary) {
            adapterActivity.setLedOff();
        }
    }

}
