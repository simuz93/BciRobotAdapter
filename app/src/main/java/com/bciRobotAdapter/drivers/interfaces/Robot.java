package com.bciRobotAdapter.drivers.interfaces;

public interface Robot {

    //Movement methods

    //ROTATION: Direction of the Robot, -180/180; SPEED: Speed of the Robot, 0-1
    void moveRobotForward(double rotation, double speed);
    void turnRobotL(double rotation);//Face Left
    void turnRobotR(double rotation);//Face Right
    void stopRobot();

    //Led methods
    void setRobotLedRed();
    void setRobotLedBlue();
    void setRobotLedGreen();
    void setRobotLedYellow();
    void setRobotLedWhite();
    void setRobotLedOff();

    void disconnect();//Disconnect the robot

    void searchAndConnect();//Search for robots and connect to the first avaiable
    void stopSearching();//Stop searching for robots
}
