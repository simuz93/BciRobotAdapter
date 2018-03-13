package com.bciRobotAdapter.drivers.robotDrivers;

import android.util.Log;

import com.bciRobotAdapter.AdapterActivity;
import com.bciRobotAdapter.drivers.abstractDrivers.AbstractRobot;

import com.orbotix.ConvenienceRobot;
import com.orbotix.Sphero;
import com.orbotix.common.DiscoveryAgentEventListener;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.le.RobotRadioDescriptor;

import java.util.List;

public class SpheroBB8Driver extends AbstractRobot implements RobotChangedStateListener{

    private static final int CALIBRATING_DEG_UNITY = 10; //BB8 will turn 10 degrees (left or right) per click while calibrating
    private float heading = 0; //Current heading in degrees

    private DiscoveryAgentLE discoveryAgent;
    private static ConvenienceRobot robot;
    private DiscoveryAgentEventListener discoveryAgentEventListener;
    private boolean calibrating = false;

    public SpheroBB8Driver(final AdapterActivity adapterActivity) {
        super(adapterActivity);
        setFrequency(80); //Set how often (in hertz) bb8 will use a packet to move. Others are discarded

        discoveryAgentEventListener = new DiscoveryAgentEventListener() {
            @Override
            public void handleRobotsAvailable(List<Robot> robots) {
                setRobotOutput("Robot Found! Bring this device near BB8 to connect");
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
    private void startCalibrating(){
        calibrating = true;
        setRobotOutput("CALIBRATION: L/R to rotate and move the robot forward to confirm");
        robot.calibrating(true);
    }
    private void stopCalibrating(){
        calibrating = false;
        setRobotOutput("Calibration done. Now you can move BB8");
        robot.calibrating(false);
    }

    //Movements methods
    //Rotation is almost always recalculated since BB8 uses 0/360 degrees system while adapterActivity uses a -180/180 degrees system
    //Some methods have different behaviors if calibrating or not
    public void moveForward(double rotation, double speed){
        double realRotation = (rotation + heading + 360)%360;

        if(!calibrating) robot.drive((float) realRotation, (float) speed);
        else if(calibrating && (realRotation>350 || realRotation < 10) && speed>=0.9) stopCalibrating();
    }

    //Stop the robot
    public void stop(){
        if(!calibrating) {
            robot.drive(heading, 0);
            robot.stop();
        }
    }

    //Rotate BB8 left (degrees = rotation) and set the new heading. Used also to calibrate
    public void turnL(double rotation) {
        double realRotation =(heading-rotation+360)%360;
        if(!calibrating) {
            robot.drive((float)realRotation, 0);
            heading = robot.getLastHeading();
        }
        else {
            robot.drive((robot.getLastHeading()-CALIBRATING_DEG_UNITY+360)%360, 0);
        }
    }

    //Rotate BB8 right (degrees = rotation) and set the new heading. Used also to calibrate
    public void turnR(double rotation) {
        double realRotation = (rotation+heading+360)%360;
        if(!calibrating){
            robot.drive((float)(realRotation), 0);
            heading = robot.getLastHeading();
        }
        else robot.drive((robot.getLastHeading()+CALIBRATING_DEG_UNITY+360)%360, 0);
    }

    //Led
    public void setLedRed() {robot.setLed(1,0,0);}
    public void setLedBlue() {robot.setLed(0,0,1);}
    public void setLedGreen() {robot.setLed(0,1,0);}
    public void setLedYellow() {robot.setLed(1,1,0);}
    public void setLedWhite() {robot.setLed(1, 1, 1);}
    public void setLedOff() {robot.setLed(0,0,0);}

    @Override
    public void handleRobotChangedState(Robot r, RobotChangedStateNotificationType robotChangedStateNotificationType) {
            switch (robotChangedStateNotificationType) {
                case Online:
                    robot = new Sphero(r);
                    setLedGreen();
                    notifyRobotConnected(true);
                    startCalibrating();
                    break;

                case Offline:
                    break;

                case Connecting:
                    break;

                case Connected:
                    break;

                case Disconnected:
                    robot  = null;
                    r.disconnect();
                    disconnect();
                    break;

                case FailedConnect:
                    break;
            }
    }

    @Override
    public void disconnect() {
        if(robot != null) {
            robot.disconnect();
            notifyRobotConnected(false);
        }

    }

    @Override
    public void searchAndConnect() {
        try {
            discoveryAgent.startDiscovery(getContext());
        } catch (DiscoveryException e) {
            Log.e("Sphero", "Discovery Error: " + e);
            e.printStackTrace();
        }
    }

    @Override
    public void stopSearching() {
        discoveryAgent.stopDiscovery();
    }
}
