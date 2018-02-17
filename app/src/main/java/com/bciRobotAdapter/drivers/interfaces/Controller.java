package com.bciRobotAdapter.drivers.interfaces;

public interface Controller {

    void disconnect(); //disconnect the device
    void searchAndConnect(); //start searching for devices and connect to the first one found
    void stopSearching(); //stop searching for devices
    void activate(boolean active); //set the boolean variable 'activate' to 'active = (true/false)'
}
