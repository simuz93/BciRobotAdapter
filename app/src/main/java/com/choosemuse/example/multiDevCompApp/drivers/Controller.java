package com.choosemuse.example.multiDevCompApp.drivers;

/**
 * Created by sserr on 23/01/2018.
 */

public interface Controller {

    void forward();
    void stop();
    void turnL();
    void turnR();

    boolean connect();
    boolean disconnect();

}
