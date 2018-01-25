package com.multiDevCompApp.drivers.interfaces;

/**
 * Created by sserr on 23/01/2018.
 */

public interface Robot {

    void forward();
    void backward();
    void stop();
    void turnL();
    void turnR();
    void ledOn(float red, float green, float blue);
    void ledOff();

    boolean connect(int index);
    boolean disconnect();

}
