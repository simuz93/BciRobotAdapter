package com.bciRobotAdapter.drivers.controllerDrivers;

import android.annotation.SuppressLint;

import com.choosemuse.libmuse.Accelerometer;
import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseConnectionListener;
import com.choosemuse.libmuse.MuseConnectionPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;
import com.choosemuse.libmuse.MuseVersion;
import com.bciRobotAdapter.drivers.abstractDrivers.AbstractController;
import com.bciRobotAdapter.AdapterActivity;

import java.lang.ref.WeakReference;
import java.util.List;


public class MuseHeadsetDriver extends AbstractController {

    private MuseManagerAndroid manager;
    private Muse muse;
    private MuseVersion museVersion;

    private ConnectionListener connectionListener;
    private DataListener dataListener;

    public MuseHeadsetDriver(AdapterActivity adapterActivity, boolean isAuxiliar) {
        super(adapterActivity, isAuxiliar);
        manager = MuseManagerAndroid.getInstance();
        manager.setContext(getContext());

        WeakReference<MuseHeadsetDriver> weakDriver;
        weakDriver = new WeakReference<>(this);

        connectionListener = new ConnectionListener(weakDriver);
        dataListener = new DataListener(weakDriver);
        manager.setMuseListener(new MuseL(weakDriver));

    }

    //Connect to the first available muse found
    public void connect() {
        manager.stopListening();
        List<Muse> availableMuses = manager.getMuses();

        if (availableMuses.size() > 0) {
            //Get the first free muse
            for (int i = 0; i<availableMuses.size(); i++){
                if(!availableMuses.get(i).isPaired()) muse = availableMuses.get(i);
            }
            if (muse == null) {
                setControllerLog("No free MUSES found");
                notifyControllerConnected(false);
                return;
            }
            museVersion = muse.getMuseVersion();

            //Set listeners
            muse.unregisterAllListeners();
            muse.registerConnectionListener(connectionListener);
            muse.registerDataListener(dataListener, MuseDataPacketType.EEG);
            muse.registerDataListener(dataListener, MuseDataPacketType.ALPHA_RELATIVE);
            muse.registerDataListener(dataListener, MuseDataPacketType.BETA_RELATIVE);
            muse.registerDataListener(dataListener, MuseDataPacketType.GAMMA_RELATIVE);
            muse.registerDataListener(dataListener, MuseDataPacketType.DELTA_RELATIVE);
            muse.registerDataListener(dataListener, MuseDataPacketType.THETA_RELATIVE);
            muse.registerDataListener(dataListener, MuseDataPacketType.ACCELEROMETER);
            muse.registerDataListener(dataListener, MuseDataPacketType.BATTERY);
            muse.registerDataListener(dataListener, MuseDataPacketType.DRL_REF);
            muse.registerDataListener(dataListener, MuseDataPacketType.QUANTIZATION);
            muse.registerDataListener(dataListener, MuseDataPacketType.ARTIFACTS);

            // Initiate a connection to the headband and stream the data asynchronously.
            muse.runAsynchronously();
            notifyControllerConnected(true);
        }
    }

    @Override
    public void disconnect() {
        muse.disconnect();
        notifyControllerConnected(false);
    }

    @Override
    public void searchAndConnect() {
        manager.startListening();
    }

    @Override
    public void stopSearching() {
        manager.stopListening();
    }

    // Listeners
    /**
     * You will receive a callback to this method each time there is a change to the
     * connection state of one of the headbands.
     *
     * @param p    A packet containing the current and prior connection states
     * @param muse The headband whose state changed.
     */
    void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {

        final ConnectionState current = p.getCurrentConnectionState();
        if (current == ConnectionState.DISCONNECTED) {
            this.muse = null;
        }
    }

    //Variables to store the data received from the muse
    private double eeg_1 = 0;
    private double eeg_2 = 0 ;
    private double eeg_3 = 0;
    private double eeg_4 = 0;

    private double x = 0;
    private double y = 0;
    private double z = 0;

    private double alpha_1 = 0;
    private double alpha_2 = 0;
    private double alpha_3 = 0;
    private double alpha_4 = 0;

    private double beta_1 = 0;
    private double beta_2 = 0;
    private double beta_3 = 0;
    private double beta_4 = 0;

    //Callback to this method everytime a packet (not artifact) arrives
    @SuppressLint("DefaultLocale")
    void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {

        switch (p.packetType()) {
            case EEG:
                eeg_1 = p.getEegChannelValue(Eeg.EEG1);
                eeg_2 = p.getEegChannelValue(Eeg.EEG1);
                eeg_3 = p.getEegChannelValue(Eeg.EEG1);
                eeg_4 = p.getEegChannelValue(Eeg.EEG1);
                break;

            case ACCELEROMETER:
                x = p.getAccelerometerValue(Accelerometer.X);
                y = p.getAccelerometerValue(Accelerometer.Y);
                z = p.getAccelerometerValue(Accelerometer.Z);
                break;

            case ALPHA_RELATIVE:
                alpha_1 = p.getEegChannelValue(Eeg.EEG1);
                alpha_2 = p.getEegChannelValue(Eeg.EEG2);
                alpha_3 = p.getEegChannelValue(Eeg.EEG3);
                alpha_4 = p.getEegChannelValue(Eeg.EEG4);
                break;

            case BETA_RELATIVE:
                beta_1 = p.getEegChannelValue(Eeg.EEG1);
                beta_2 = p.getEegChannelValue(Eeg.EEG2);
                beta_3 = p.getEegChannelValue(Eeg.EEG3);
                beta_4 = p.getEegChannelValue(Eeg.EEG4);
                break;

            case BATTERY:
            case DRL_REF:
            case QUANTIZATION:
            default:
                break;

        }
        //Print data

        //0.9 -> 1.6

        /*if (alpha_1 + alpha_4 >= 0.9 && alpha_1 + alpha_4 < 1.1) {
            moveRobotForward(calculateRotation((float)y), 0.3);
            setRobotLedBlue();
        } else if (alpha_1 + alpha_4 >= 1.1) {
            moveRobotForward(calculateRotation((float)y), 0.7);
            setRobotLedRed();
        } else {
            stopRobot();
            setRobotLedWhite();
        }*/

        //Rotating the robot using the accelerometer datas. TESTING
        moveRobotForward(calculateRotation((float)y), 0.3);
        setControllerLog(calculateRotation((float)y)+"");
    }

    //Callback to this method everytime an artifact packet arrives
    void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {

        if(p.getBlink()) {

        }
    }

    //Calculate rotation in degrees from accelerometer Y data. TESTING.
    private float calculateRotation(float y) {
        if(y>-0.3 && y<0.3) return 0;
        else if (y<=-0.3 && y>-0.5) return -90; //left
        else if (y>=0.3 && y<0.5) return 90; //right
        else if(y<=-0.5) return (float)-90; //left max
        else if(y>=0.5) return (float) 90; //right max
        else return 0;
    }

}
// Listener translators
//
// Each of these classes extend from the appropriate listener and contain a weak reference
// to the activity.  Each class simply forwards the messages it receives back to the Activity.
class MuseL extends MuseListener {
    private final WeakReference<MuseHeadsetDriver> MuseDriverRef;

    MuseL(final WeakReference<MuseHeadsetDriver> activityRef) {
        this.MuseDriverRef = activityRef;
    }

    @Override
    public void museListChanged() {
        MuseDriverRef.get().connect();
    }
}

class ConnectionListener extends MuseConnectionListener {
    private final WeakReference<MuseHeadsetDriver> MuseDriverRef;

    ConnectionListener(final WeakReference<MuseHeadsetDriver> activityRef) {
        this.MuseDriverRef = activityRef;
    }

    @Override
    public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {
        MuseDriverRef.get().receiveMuseConnectionPacket(p, muse);
    }
}

class DataListener extends MuseDataListener {
    private final WeakReference<MuseHeadsetDriver> MuseDriverRef;

    DataListener(final WeakReference<MuseHeadsetDriver> activityRef) {
        this.MuseDriverRef = activityRef;
    }

    @Override
    public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {
        MuseDriverRef.get().receiveMuseDataPacket(p, muse);
    }

    @Override
    public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
        MuseDriverRef.get().receiveMuseArtifactPacket(p, muse);
    }
}