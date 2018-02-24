package com.bciRobotAdapter.drivers.interfaces;

public interface Robot {

    //Movement methods

    //ROTATION: Direction of the Robot, -180/180; SPEED: Speed of the Robot, 0-1
    void moveForward(double rotation, double speed);
    void turnL(double rotation);//Face Left
    void turnR(double rotation);//Face Right
    void stop();

    //Led methods
    void setLedRed();
    void setLedBlue();
    void setLedGreen();
    void setLedYellow();
    void setLedWhite();
    void setLedOff();

    void disconnect();//Disconnect the robot

    void searchAndConnect();//Search for robots and connect to the first available
    void stopSearching();//Stop searching for robots
}
