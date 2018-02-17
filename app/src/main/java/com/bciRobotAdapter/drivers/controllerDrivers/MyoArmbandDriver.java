package com.bciRobotAdapter.drivers.controllerDrivers;

import com.bciRobotAdapter.AdapterActivity;
import com.bciRobotAdapter.drivers.abstractDrivers.AbstractController;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.XDirection;

public class MyoArmbandDriver extends AbstractController {

    private MyoListener myoListener;

    public MyoArmbandDriver(AdapterActivity adapterActivity) {
        super(adapterActivity);
        myoListener = new MyoListener(this);
        Hub.getInstance().init(getContext());
    }


    @Override
    public void disconnect() {
        for(Myo m : Hub.getInstance().getConnectedDevices()) {
            Hub.getInstance().detach(m.getMacAddress());
        }
        Hub.getInstance().removeListener(myoListener);
        setControllerLog("myo disconnected");
        notifyControllerConnected(false);
    }

    @Override
    public void searchAndConnect() {
        Hub.getInstance().attachToAdjacentMyo();
        Hub.getInstance().setLockingPolicy(Hub.LockingPolicy.NONE);
        Hub.getInstance().addListener(myoListener);
    }

    @Override
    public void stopSearching() {

    }
}

//Listener to myo packets incoming
class MyoListener extends AbstractDeviceListener {

    MyoArmbandDriver myoDriver;
    float centerYaw = 0;

    public MyoListener(MyoArmbandDriver myoDriver) {
        this.myoDriver = myoDriver;
    }

    @Override
    public void onAttach(Myo myo, long l) {

    }

    @Override
    public void onDetach(Myo myo, long l) {

    }

    @Override
    public void onConnect(Myo myo, long l) {
        myoDriver.setControllerLog("myo connected"+ myo.isConnected()+" " + myo.isUnlocked());
        myoDriver.notifyControllerConnected(true);
    }

    @Override
    public void onDisconnect(Myo myo, long l) {
        myoDriver.setControllerLog("myo disconnected");
    }

    @Override
    public void onArmSync(Myo myo, long l, Arm arm, XDirection xDirection) {
        myoDriver.setControllerLog("arm sync");
    }

    @Override
    public void onArmUnsync(Myo myo, long l) {
        myoDriver.setControllerLog("arm unsync");
    }

    @Override
    public void onUnlock(Myo myo, long l) {
        //super.onUnlock(myo, l);
        myoDriver.setControllerLog("unlocked");
    }

    @Override
    public void onLock(Myo myo, long l) {
        //super.onLock(myo, l);
        myoDriver.setControllerLog("locked");
    }

    @Override
    public void onPose(Myo myo, long l, Pose pose) {

        if(pose.equals(Pose.DOUBLE_TAP)){
            myoDriver.setControllerLog("double tap");
        }

        if(pose.equals(Pose.FIST)) {
            myoDriver.setControllerLog("fist");
        }

        else if(pose.equals(Pose.REST)) {
            myoDriver.setControllerLog("rest");
        }

        else if(pose.equals(Pose.FINGERS_SPREAD)) {
            myoDriver.setControllerLog("spread");
        }

    }


    //Orientation
    double oriz;
    double orix;
    double oriy;
    double oriw;

    //Accelerometer
    double accx;
    double accy;
    double accz;

    //Gyroscope
    double gyrx;
    double gyry;
    double gyrz;

    @Override
    public void onOrientationData(Myo myo, long l, Quaternion rotation) {
            oriz = rotation.z();
            orix = rotation.x();
            oriy = rotation.y();
            oriw = rotation.w();

            printData();
    }

    @Override
    public void onAccelerometerData(Myo myo, long l, Vector3 vector3) {
            accx = vector3.x();
            accy = vector3.y();
            accz = vector3.z();

            //Speed calculated from the arm position:
            //Down to the floor: speed = 0;
            //Horizontal: speed = 1 (max);
            //Up to the ceiling: the robot moves backward at reduced speed

            double speed = (-accx)+1;
            if(speed>0.3&&speed<1.5)myoDriver.moveRobotForward(0,speed);
            else if(speed>=1.3)myoDriver.moveRobotForward(180, speed-1.3);
            else myoDriver.stopRobot();

            //printData();

    }

    @Override
    public void onGyroscopeData(Myo myo, long l, Vector3 vector3) {
        gyrx = vector3.x();
        gyry = vector3.y();
        gyrz = vector3.z();
        //printData();
    }

    public void printData() {
        myoDriver.multiLog(1, "Ori", "Acc", "Gyr", orix, oriy, oriz, oriw, accx, accy, accz, -255, gyrx, gyry, gyrz, -255);
    }

    @Override
    public void onRssi(Myo myo, long l, int i) {

    }
}