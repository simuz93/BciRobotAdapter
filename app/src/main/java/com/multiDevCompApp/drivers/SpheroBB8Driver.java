package com.multiDevCompApp.drivers;

import android.util.Log;

import com.multiDevCompApp.MainActivity;
import com.orbotix.ConvenienceRobot;
import com.orbotix.Sphero;
import com.orbotix.common.DiscoveryAgentEventListener;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.le.RobotRadioDescriptor;

import java.util.ArrayList;
import java.util.List;

public class SpheroBB8Driver implements RobotChangedStateListener, com.multiDevCompApp.drivers.interfaces.Robot {
    private MainActivity mainActivity;
    private DiscoveryAgentLE discoveryAgent;
    private static ConvenienceRobot robot;
    private DiscoveryAgentEventListener discoveryAgentEventListener;

    private ArrayList<String> spinnerRobotList;

    public SpheroBB8Driver(MainActivity ma) {
        this.mainActivity = ma;
        spinnerRobotList = new ArrayList<>();

        discoveryAgentEventListener = new DiscoveryAgentEventListener() {
            @Override
            public void handleRobotsAvailable(List<Robot> robots) {

                mainActivity.writeScreenLog("Found "+robots.size()+" robots");

                spinnerRobotList.clear();
                for (Robot r : robots) {
                    spinnerRobotList.add(r.getName());
                }
            }
        };

        discoveryAgent = DiscoveryAgentLE.getInstance();
        discoveryAgent.addDiscoveryListener(discoveryAgentEventListener);

        RobotRadioDescriptor robotRadioDescriptor = new RobotRadioDescriptor();
        robotRadioDescriptor.setNamePrefixes(new String[]{"BB-"});
        discoveryAgent.setRadioDescriptor(robotRadioDescriptor);

        discoveryAgent.addRobotStateListener(this);


    }

    //Calibration
    void startCalibrating(){
        robot.calibrating(true);
    }
    void stopClaibrating(){
        robot.calibrating(false);
    }

    //Movimento
    @Override
    public void moveForward(double rotation, double speed){robot.drive((float) rotation, (float) speed);}
    @Override
    public void moveBackward(double rotation, double speed) {robot.drive((float)rotation, (float)speed, true);}
    @Override
    public void stop(){robot.drive(0, 0);}
    @Override
    public void turnL() {robot.drive(270, 0);}
    @Override
    public void turnR() {robot.drive(90, 0);}

    //Led
    @Override
    public void setLedRed() {robot.setLed(1,0,0);}
    @Override
    public void setLedBlue() {robot.setLed(0,0,1);}
    @Override
    public void setLedGreen() {robot.setLed(0,1,0);}
    @Override
    public void setLedYellow() {robot.setLed(0,1,1);}
    @Override
    public void setLedWhite() {robot.setLed(1, 1, 1);}
    @Override
    public void setLedOff() {robot.setLed(0,0,0);}

    @Override
    public void handleRobotChangedState(Robot r, RobotChangedStateNotificationType robotChangedStateNotificationType) {
            switch (robotChangedStateNotificationType) {
                case Online:
                    mainActivity.writeScreenLog("Robot "+r.getName()+" is online");
                    robot = new Sphero(r);
                    setLedGreen();
                    break;

                case Offline:
                    mainActivity.writeScreenLog("Robot "+r.getName()+" is offline");
                    break;

                case Connecting:
                    mainActivity.writeScreenLog("Connecting...");
                    break;

                case Connected:
                    mainActivity.writeScreenLog("Connected to Robot "+r.getName());
                    break;

                case Disconnected:
                    mainActivity.writeScreenLog("Disconnected from Robot "+r.getName());
                    break;

                case FailedConnect:
                    mainActivity.writeScreenLog("ERROR: Connection failed to Robot "+r.getName());
                    break;
            }
    }

    @Override
    public void connect(int index) {
        stopSearching();
        List<Robot> availableRobots = discoveryAgent.getRobots();
        Robot choosenRobot = availableRobots.get(index);
        robot = new Sphero(choosenRobot);
        setLedGreen();
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void startSearching() {
        mainActivity.writeScreenLog("looking for robot");
        try {
            discoveryAgent.startDiscovery(mainActivity.getApplicationContext());
        } catch (DiscoveryException e) {
            Log.e("Sphero", "Discovery Error: " + e);
            e.printStackTrace();
        }
    }

    public void stopSearching() {
        discoveryAgent.stopDiscovery();
    }

    public ArrayList<String> getRobotList() {return this.spinnerRobotList;}

    public void deviceListChanged() {

    }
}
