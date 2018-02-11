package com.multiDevCompApp.drivers;

import android.util.Log;

import com.multiDevCompApp.drivers.driversInterfaces.AdapterActivity;
import com.orbotix.ConvenienceRobot;
import com.orbotix.Sphero;
import com.orbotix.common.DiscoveryAgentEventListener;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.le.RobotRadioDescriptor;

import java.util.List;

public class SpheroBB8Driver implements com.multiDevCompApp.drivers.driversInterfaces.Robot, RobotChangedStateListener{

    private static final int CALIBRATING_DEG_UNITY = 10;
    private float heading = 0;

    private AdapterActivity adapterActivity;
    private DiscoveryAgentLE discoveryAgent;
    private static ConvenienceRobot robot;
    private DiscoveryAgentEventListener discoveryAgentEventListener;
    private boolean calibrating = false;

    public SpheroBB8Driver(final AdapterActivity adapterActivity) {
        this.adapterActivity = adapterActivity;

        discoveryAgentEventListener = new DiscoveryAgentEventListener() {
            @Override
            public void handleRobotsAvailable(List<Robot> robots) {
                adapterActivity.log(2,"Found "+robots.size()+" robots");
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
        adapterActivity.log(3, "CALIBRAZIONE: porta il  joystick in alto per confermare");
        robot.calibrating(true);
    }
    private void stopCalibrating(){
        calibrating = false;
        adapterActivity.log(3, "CALIBRAZIONE TERMINATA");
        robot.calibrating(false);
    }

    //Movimento

    public void moveForward(double rotation, double speed){

        adapterActivity.log(3, heading+" "+robot.getLastHeading());
        double realRotation = (rotation + heading + 360)%360;

        if(!calibrating) robot.drive((float) realRotation, (float) speed);
        else if(calibrating && (realRotation>350 || realRotation < 10) && speed>=0.9) stopCalibrating();


    }
    public void moveBackward(double rotation, double speed) {robot.drive((float) rotation, (float)speed, true);}

    public void stop(){
        if(!calibrating) {
            robot.drive(heading, 0);
            robot.stop();
        }
    }

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
    public void turnR(double rotation) {
        double realRotation = (rotation+heading+360)%360;
        if(!calibrating){
            robot.drive((float)(realRotation), 0);
            heading = robot.getLastHeading();
            adapterActivity.log(3, heading+" "+robot.getLastHeading());
        }
        else robot.drive((robot.getLastHeading()+CALIBRATING_DEG_UNITY+360)%360, 0);
    }

    //Led
    public void setLedRed() {robot.setLed(1,0,0);}
    public void setLedBlue() {robot.setLed(0,0,1);}
    public void setLedGreen() {robot.setLed(0,1,0);}
    public void setLedYellow() {robot.setLed(0,1,1);}
    public void setLedWhite() {robot.setLed(1, 1, 1);}
    public void setLedOff() {robot.setLed(0,0,0);}

    @Override
    public void handleRobotChangedState(Robot r, RobotChangedStateNotificationType robotChangedStateNotificationType) {
            switch (robotChangedStateNotificationType) {
                case Online:
                    robot = new Sphero(r);
                    setLedGreen();
                    adapterActivity.onRobotConnected(true);
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
                    break;

                case FailedConnect:
                    break;
            }
    }

    public void disconnect() {
        if(robot != null) {
            robot.disconnect();
            adapterActivity.onRobotConnected(false);
        }

    }

    public void startSearching() {
        try {
            discoveryAgent.startDiscovery(adapterActivity.getActivity().getApplicationContext());
        } catch (DiscoveryException e) {
            Log.e("Sphero", "Discovery Error: " + e);
            e.printStackTrace();
        }
    }

    public void stopSearching() {
        discoveryAgent.stopDiscovery();
    }
}
