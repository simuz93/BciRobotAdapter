package com.bciRobotAdapter.drivers.abstractDrivers;

import android.app.Activity;
import android.content.Context;

import com.bciRobotAdapter.AdapterActivity;
import com.bciRobotAdapter.drivers.interfaces.Controller;

import java.sql.Timestamp;

//Abstract controller driver. A new controller driver should extends this class.
//However, it's also possible to directly implement the Controller interface, manually managing every method needed
public abstract class AbstractController implements Controller {

    private boolean active = true; //If false, every movement call from the controller is ignored
    private final AdapterActivity adapterActivity;//The main activity
    private int FREQUENCY; //Packet send frequency in Hz
    private boolean forceSend;

    public AbstractController(AdapterActivity adapterActivity) {
        this.adapterActivity = adapterActivity;
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

    //Manage the active boolean variable
    @Override
    public void activate(boolean active) {
        this.active = active;
    }

    //Notify the adapter that the controller is connected (connected = true) or disconnected (connected = false).
    // You MUST call this method everytime the controller's connection state changes
    public void notifyControllerConnected(boolean connected) {
        this.adapterActivity.onControllerConnected(connected);
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
        this.adapterActivity.setControllerLog(toWrite);
    }

    //Print in the main textview the String "toWrite"
    public void setGeneralLog(String toWrite) {
        this.adapterActivity.setGeneralLog(toWrite);
    }

    //Print more formatted logs in the general log
    //2 lines
    public void multiLog(int n_dec, String s1, String s2,
                         double v11, double v12, double v13, double v14,
                         double v21, double v22, double v23, double v24) {

        setGeneralLog(s1+": "+String.format("%.2f", v11)+" - "+String.format("%.2f",v12)+" - "+String.format("%.2f",v13)+" - "+String.format("%.2f",v14)+
                "\n"+s2+": "+String.format("%.2f",v21)+" - "+String.format("%.2f",v22)+" - "+String.format("%.2f",v23)+" - "+String.format("%.2f",v24));
    }

    //3 lines
    public void multiLog(int n_dec, String s1, String s2, String s3,
                         double v11, double v12, double v13, double v14,
                         double v21, double v22, double v23, double v24,
                         double v31, double v32, double v33, double v34) {

        setGeneralLog(s1+": "+String.format("%."+n_dec+"f", v11)+" - "+String.format("%."+n_dec+"f",v12)+" - "+String.format("%."+n_dec+"f",v13)+" - "+String.format("%."+n_dec+"f",v14)+
                "\n"+s2+": "+String.format("%."+n_dec+"f",v21)+" - "+String.format("%."+n_dec+"f",v22)+" - "+String.format("%."+n_dec+"f",v23)+" - "+String.format("%."+n_dec+"f",v24)+
                "\n"+s3+": "+String.format("%."+n_dec+"f",v31)+" - "+String.format("%."+n_dec+"f",v32)+" - "+String.format("%."+n_dec+"f",v33)+" - "+String.format("%."+n_dec+"f",v34));
    }

    //4 lines
    public void multiLog(int n_dec, String s1, String s2, String s3, String s4,
                         double v11, double v12, double v13, double v14,
                         double v21, double v22, double v23, double v24,
                         double v31, double v32, double v33, double v34,
                         double v41, double v42, double v43, double v44) {

        setGeneralLog(s1+": "+String.format("%.2f", v11)+" - "+String.format("%.2f",v12)+" - "+String.format("%.2f",v13)+" - "+String.format("%.2f",v14)+
                "\n"+s2+": "+String.format("%.2f",v21)+" - "+String.format("%.2f",v22)+" - "+String.format("%.2f",v23)+" - "+String.format("%.2f",v24)+
                "\n"+s3+": "+String.format("%.2f",v31)+" - "+String.format("%.2f",v32)+" - "+String.format("%.2f",v33)+" - "+String.format("%.2f",v34)+
                "\n"+s4+": "+String.format("%.2f",v41)+" - "+String.format("%.2f",v42)+" - "+String.format("%.2f",v43)+" - "+String.format("%.2f",v44));
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
        if(active) {
            this.adapterActivity.moveForward(rotation, speed);
        }
    }
    public void stopRobot() {

        if(active) {
            this.adapterActivity.stop();
        }
    }
    public void turnRobotL(double rotation) {
        if(active) {
            this.adapterActivity.turnL(rotation);
        }
    } //Face left
    public void turnRobotR(double rotation) {
        if(active) {
            this.adapterActivity.turnR(rotation);
        }
    } //Face right

    //Led
    public void setRobotLedRed() {
        if(active) {
            this.adapterActivity.setLedRed();
        }
    }
    public void setRobotLedBlue() {
        if(active) {
            this.adapterActivity.setLedBlue();
        }
    }
    public void setRobotLedGreen() {
        if(active) {
            this.adapterActivity.setLedGreen();
        }
    }
    public void setRobotLedYellow() {
        if(active) {
            this.adapterActivity.setLedYellow();
        }
    }
    public void setRobotLedWhite() {
        if(active) {
            this.adapterActivity.setLedWhite();
        }
    }
    public void setRobotLedOff() {
        if(active) {
            this.adapterActivity.setLedOff();
        }
    }

}
