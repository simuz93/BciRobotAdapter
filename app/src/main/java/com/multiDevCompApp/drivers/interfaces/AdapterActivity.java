package com.multiDevCompApp.drivers.interfaces;

import android.app.Activity;


/**
 * Created by sserr on 05/02/2018.
 */


public interface AdapterActivity {
    Activity getActivity();

    void onControllerConnected(boolean connected);
    void onRobotConnected(boolean connected);

    void moveForward(double rotation, double speed);
    void moveBackward(double rotation, double speed);
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

    void log(int n, String toWrite);
}
