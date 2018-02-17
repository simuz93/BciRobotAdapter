package com.bciRobotAdapter;

import android.app.Activity;
import android.content.Context;

//Main activity MUST implements this interface
public interface AdapterActivity {
    Activity getActivity(); //Return current adapterActivity instance
    Context getContext(); //Return adapterActivity Context

    //Notify when a controller/robot connects (true) or disconnects (false).
    void onControllerConnected(boolean connected);
    void onRobotConnected(boolean connected);

    //Movements methods
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

    //Log and debug methods
    void setGeneralLog(String toWrite);
    void setControllerLog(String toWrite);
    void setRobotLog(String toWrite);
}
