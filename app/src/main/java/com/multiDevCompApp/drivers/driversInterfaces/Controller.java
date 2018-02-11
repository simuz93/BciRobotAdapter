package com.multiDevCompApp.drivers.driversInterfaces;


public interface Controller {

    void disconnect(); //disconnect the device
    void startSearching(); //start searching for devices and connect to the first one found
    void stopSearching(); //stop searching for devices
    boolean activate(boolean active); //set the boolean variable 'activate' to 'active:(true/false)'

}
