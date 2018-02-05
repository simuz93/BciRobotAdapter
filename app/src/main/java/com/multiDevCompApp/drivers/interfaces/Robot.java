package com.multiDevCompApp.drivers.interfaces;

import java.util.ArrayList;

/**
 * Created by sserr on 23/01/2018.
 */

public interface Robot {

    //Movement methods

    //ROTATION: Direction of the Robot, 0-360; SPEED: Speed of the Robot, 0-1
    void moveForward(double rotation, double speed);
    void moveBackward(double rotation, double speed);

    void stop();
    void turnL();//Face Left
    void turnR();//Face Right

    //Led methods
    void setLedRed();
    void setLedBlue();
    void setLedGreen();
    void setLedYellow();
    void setLedWhite();
    void setLedOff();

    //Connection methods
    void connect(int index);
    void disconnect();

    void startSearching();
    void stopSearching();

    ArrayList<String> getRobotList();

}
