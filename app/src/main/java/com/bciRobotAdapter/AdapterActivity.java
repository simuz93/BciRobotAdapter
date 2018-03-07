package com.bciRobotAdapter;

import android.app.Activity;
import android.content.Context;

//Main activity MUST implements this interface
public interface AdapterActivity {

    /*===========================Get methods===========================*/
    /**
     * Get current Adapter Activity
     * @return current AdapterActivity
     */
    Activity getActivity();
    /**
     * Get current Adapter Activity context
     * @return current Adapter Activity context
     */
    Context getContext();

    /*===========================Devices management methods===========================*/
    //Listeners

    /**
     * You will receive a callback to this method whenever a MainController
     * connects or disconnects.
     * Called in Controller by notifyControllerConnected(true/false).
     * @param connected true if the Controller is connecting, false if disconnecting.
     */
    void onMainControllerConnected(boolean connected);

    /**
     * You will receive a callback to this method whenever an AuxController
     * connects or disconnects.
     * Called in Controller by notifyControllerConnected(true/false).
     * @param connected true if the Controller is connecting, false if disconnecting.
     */
    void onAuxControllerConnected(boolean connected);

    /**
     * You will receive a callback to this method whenever a Robot
     * connects or disconnects.
     * Called in Robot by notifyRobotConnected(true/false).
     * @param connected true if the Robot is connecting, false if disconnecting.
     */
    void onRobotConnected(boolean connected);

    //Misc

    /**
     * Pass the AdapterActivity the direction of the AuxController,
     * to let it override at runtime the direction of the MainController.
     * @param direction direction output of the AuxController
     */
    void setAuxCtrlDirection(double direction);

    /**
     * Set the frequency used by the Robot to move.
     * This value will limit (Controller side) the number of command packets sent to the Robot.
     * @param frequency_Hz frequency value, in Hertz
     */
    void setFrequency(int frequency_Hz);

    /*===========================Movement and led methods===========================*/
    //Movement
    void moveForward(double rotation, double speed);
    void stop();
    void turnL(double rotation); //Face left
    void turnR(double rotation); //Face right
    //Led
    void setLedRed();
    void setLedBlue();
    void setLedGreen();
    void setLedYellow();
    void setLedWhite();
    void setLedOff();

    /*===========================Log and debug methods===========================*/
    void setMainControllerLog(String toWrite);
    void setAuxControllerLog(String toWrite);
    void setRobotLog(String toWrite);

    void setMainControllerOutput(String toWrite);
    void setAuxControllerOutput(String toWrite);
    void setRobotOutput(String toWrite);
}
