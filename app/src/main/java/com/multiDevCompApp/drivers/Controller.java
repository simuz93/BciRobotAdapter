package com.multiDevCompApp.drivers;

import java.util.ArrayList;

/**
 * Created by sserr on 23/01/2018.
 */

public interface Controller {

    void forward();
    void stop();
    void turnL();
    void turnR();

    boolean connect(int index);
    boolean disconnect();

    void stopListening();
    void startListening();

    ArrayList<String> getSpinnerCtrlList();

}
