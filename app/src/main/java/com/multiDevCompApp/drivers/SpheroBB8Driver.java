package com.multiDevCompApp.drivers;

import android.util.Log;

import com.multiDevCompApp.drivers.interfaces.AdapterActivity;
import com.orbotix.ConvenienceRobot;
import com.orbotix.Sphero;
import com.orbotix.common.DiscoveryAgentEventListener;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.le.RobotRadioDescriptor;

import java.util.List;

public class SpheroBB8Driver implements com.multiDevCompApp.drivers.interfaces.Robot, RobotChangedStateListener{

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
        adapterActivity.log(3, "CALIBRAZIONE: porta il  joystick in basso per confermare");
        robot.calibrating(true);
    }
    private void stopCalibrating(){
        calibrating = false;
        robot.calibrating(false);
    }

    //Movimento

    public void moveForward(double rotation, double speed){
        robot.drive((float) rotation, (float) speed);
        if(calibrating && rotation>170 && rotation < 190) stopCalibrating();
    }
    public void moveBackward(double rotation, double speed) {robot.drive((float) rotation, (float)speed, true);}

    public void stop(){
        if(!calibrating) robot.drive(0, 0);
        else stopCalibrating();
    }
    public void turnL(double rotation) {
        double realRotation = 360 - rotation;
        if(!calibrating) {
            robot.drive((float) realRotation, 0);
        }
        else {
            robot.drive((float)(robot.getLastHeading()+rotation), 0);
        }
    }
    public void turnR(double rotation) {
        if(!calibrating)robot.drive((float)rotation, 0);
        else robot.drive((float)(robot.getLastHeading()+rotation), 0);
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
