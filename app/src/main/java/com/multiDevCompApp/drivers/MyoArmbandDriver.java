package com.multiDevCompApp.drivers;

import com.multiDevCompApp.drivers.driversInterfaces.AdapterActivity;
import com.multiDevCompApp.drivers.driversInterfaces.Controller;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.XDirection;


public class MyoArmbandDriver implements Controller{

    private final AdapterActivity adapterActivity;
    private boolean active = true;

    private MyoListener myoListener;

    public MyoArmbandDriver(AdapterActivity adapterActivity) {
        this.adapterActivity = adapterActivity;
        myoListener = new MyoListener(adapterActivity);
        Hub.getInstance().init(adapterActivity.getActivity().getApplicationContext());
    }


    @Override
    public void disconnect() {
        for(Myo m : Hub.getInstance().getConnectedDevices()) {
            Hub.getInstance().detach(m.getMacAddress());
        }
        Hub.getInstance().removeListener(myoListener);
        adapterActivity.log(1, "myo disconnected");
        adapterActivity.onControllerConnected(false);
    }

    @Override
    public void startSearching() {
        Hub.getInstance().attachToAdjacentMyo();
        Hub.getInstance().setLockingPolicy(Hub.LockingPolicy.STANDARD);
        Hub.getInstance().addListener(myoListener);
    }

    @Override
    public void stopSearching() {

    }

    @Override
    public boolean activate(boolean active) {
        this.active = active;
        return this.active;
    }
}

class MyoListener extends AbstractDeviceListener {

    AdapterActivity adapterActivity;

    public MyoListener(AdapterActivity adapterActivity) {
        this.adapterActivity = adapterActivity;
    }

    @Override
    public void onAttach(Myo myo, long l) {

    }

    @Override
    public void onDetach(Myo myo, long l) {

    }

    @Override
    public void onConnect(Myo myo, long l) {
        adapterActivity.log(1, "myo connected"+ myo.isConnected()+" " + myo.isUnlocked());
        adapterActivity.onControllerConnected(true);
    }

    @Override
    public void onDisconnect(Myo myo, long l) {
        adapterActivity.log(1, "myo disconnected");

    }

    @Override
    public void onArmSync(Myo myo, long l, Arm arm, XDirection xDirection) {
        adapterActivity.log(1, "arm sync");
    }

    @Override
    public void onArmUnsync(Myo myo, long l) {
        adapterActivity.log(1, "arm unsync");
    }

    @Override
    public void onUnlock(Myo myo, long l) {
        super.onUnlock(myo, l);
        adapterActivity.log(1, "unlocked");
    }

    @Override
    public void onLock(Myo myo, long l) {
        super.onLock(myo, l);
    }

    @Override
    public void onPose(Myo myo, long l, Pose pose) {

        if(pose.equals(Pose.FIST)) {
            myo.vibrate(Myo.VibrationType.SHORT);
            adapterActivity.log(1, "fist");
        }

        else if(pose.equals(Pose.REST)) {
            myo.vibrate(Myo.VibrationType.SHORT);
            adapterActivity.log(1, "rest");
        }

        else if(pose.equals(Pose.FINGERS_SPREAD)) {
            myo.vibrate(Myo.VibrationType.SHORT);
            adapterActivity.log(1, "finger");
        }

    }

    @Override
    public void onOrientationData(Myo myo, long l, Quaternion quaternion) {

    }

    @Override
    public void onAccelerometerData(Myo myo, long l, Vector3 vector3) {

    }

    @Override
    public void onGyroscopeData(Myo myo, long l, Vector3 vector3) {

    }

    @Override
    public void onRssi(Myo myo, long l, int i) {

    }
}