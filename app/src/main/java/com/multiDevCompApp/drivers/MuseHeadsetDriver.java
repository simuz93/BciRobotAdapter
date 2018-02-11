package com.multiDevCompApp.drivers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

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
import com.multiDevCompApp.R;
import com.multiDevCompApp.drivers.abstractDrivers.AbstractControllerDriver;
import com.multiDevCompApp.drivers.driversInterfaces.AdapterActivity;
import com.multiDevCompApp.drivers.driversInterfaces.Controller;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MuseHeadsetDriver extends AbstractControllerDriver {

    private static final int FREQUENCY = 5;

    private final AdapterActivity adapterActivity;

    private MuseManagerAndroid manager;
    private Muse muse;
    private MuseVersion museVersion;

    private ConnectionListener connectionListener;
    private DataListener dataListener;

    //private boolean active = true;

    public MuseHeadsetDriver(AdapterActivity adapterActivity) {
        this.adapterActivity = adapterActivity;
        manager = MuseManagerAndroid.getInstance();
        manager.setContext(adapterActivity.getActivity().getApplicationContext());

        WeakReference<MuseHeadsetDriver> weakDriver;
        weakDriver = new WeakReference<>(this);

        connectionListener = new ConnectionListener(weakDriver);
        dataListener = new DataListener(weakDriver);
        manager.setMuseListener(new MuseL(weakDriver));

        ensurePermissions();
    }

    public void connect() {
        manager.stopListening();
        List<Muse> availableMuses = manager.getMuses();

        if (availableMuses.size() > 0) {

            // Cache the Muse that the user has selected.
            for (int i = 0; i<availableMuses.size(); i++){
                if(!availableMuses.get(i).isPaired()) muse = availableMuses.get(i);
            }
            if (muse == null) {
                adapterActivity.log(1, "No free MUSES found");
                adapterActivity.onControllerConnected(false);
                return;
            }
            museVersion = muse.getMuseVersion();
            // Unregister all prior listeners and register our data listener to
            // receive the MuseDataPacketTypes we are interested in.  If you do
            // not register a listener for a particular data type, you will not
            // receive data packets of that type.
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
            adapterActivity.onControllerConnected(true);
        }
    }

    @Override
    public void disconnect() {
        muse.disconnect();
        adapterActivity.onControllerConnected(false);
    }

    @Override
    public void startSearching() {
        manager.startListening();
    }

    @Override
    public void stopSearching() {
        manager.stopListening();
    }

    private void ensurePermissions() {

        if (ContextCompat.checkSelfPermission(adapterActivity.getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // We don't have the ACCESS_COARSE_LOCATION permission so create the dialogs asking
            // the user to grant us the permission.

            DialogInterface.OnClickListener buttonListener =
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ActivityCompat.requestPermissions(adapterActivity.getActivity(),
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    0);
                        }
                    };

            // This is the context dialog which explains to the user the reason we are requesting
            // this permission.  When the user presses the positive (I Understand) button, the
            // standard Android permission dialog will be displayed (as defined in the button
            // listener above).
            AlertDialog introDialog = new AlertDialog.Builder(adapterActivity.getActivity())
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage(R.string.permission_dialog_description)
                    .setPositiveButton(R.string.permission_dialog_understand, buttonListener)
                    .create();
            introDialog.show();
        }
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

        // Format a message to show the change of connection state in the UI.
        final String status = p.getPreviousConnectionState() + " -> " + current;

        // Update the UI with the change in connection state.
        /*mainActivity.getHandler().post(new Runnable() {

            @Override
            public void run() {

                museVersion = muse.getMuseVersion();

                // If we haven't yet connected to the headband, the version information
                // will be null.  You have to connect to the headband before either the
                // MuseVersion or MuseConfiguration information is known.
                if (museVersion != null) {
                    final String version = museVersion.getFirmwareType() + " - "
                            + museVersion.getFirmwareVersion() + " - "
                            + museVersion.getProtocolVersion();
                    //museVersionText.setText(version);
                } else {
                    //museVersionText.setText(R.string.undefined);
                }
            }
        });*/

        if (current == ConnectionState.DISCONNECTED) {
            // We have disconnected from the headband, so set our cached copy to null.
            this.muse = null;
        }
    }

    /**
     * You will receive a callback to this method each time the headband sends a MuseDataPacket
     * that you have registered.  You can use different listeners for different packet types or
     * a single listener for all packet types as we have done here.
     *
     * @param p    The data packet containing the data from the headband (eg. EEG data)
     * @param muse The headband that sent the information.
     */

    private int count = 0;

    double eeg_1 = 0;
    double eeg_2 = 0 ;
    double eeg_3 = 0;
    double eeg_4 = 0;

    double x = 0;
    double y = 0;
    double z = 0;

    double alpha_1 = 0;
    double alpha_2 = 0;
    double alpha_3 = 0;
    double alpha_4 = 0;

    double beta_1 = 0;
    double beta_2 = 0;
    double beta_3 = 0;
    double beta_4 = 0;

    @SuppressLint("DefaultLocale")
    void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {

        if(count%FREQUENCY == 0) {
            switch (p.packetType()) {
                case EEG:
                    eeg_1 = p.getEegChannelValue(Eeg.EEG1);
                    eeg_2 = p.getEegChannelValue(Eeg.EEG1);
                    eeg_3 = p.getEegChannelValue(Eeg.EEG1);
                    eeg_4 = p.getEegChannelValue(Eeg.EEG1);
                    //adapterActivity.log(1, "eeg ch1:" + String.format("%.2f", eeg_1) + " ch2:" + String.format("%.2f", eeg_2) + " ch3:" + String.format("%.2f", eeg_3) + " ch4:" + String.format("%.2f", eeg_4));
                    break;

                case ACCELEROMETER:
                    x = p.getAccelerometerValue(Accelerometer.X);
                    y = p.getAccelerometerValue(Accelerometer.Y);
                    z = p.getAccelerometerValue(Accelerometer.Z);
                    adapterActivity.log(2,"acc X:" + String.format("%.3f", x) + " Y:" + String.format("%.3f", y) + " Z:" + String.format("%.3f", z));

                    /*

                        double speed = 0;
                        double rotation = 0;

                        if(x>0.3) {
                            adapterActivity.setLedGreen();
                            speed = x-0.2;
                            if (y > 0.2) rotation = 90;
                            if (y < -0.2) rotation = 270;
                            adapterActivity.moveForward(rotation, speed);
                        }

                        else if(x<0.2 && x>-0.2) {
                            adapterActivity.setLedBlue();
                            adapterActivity.stop();
                        }

                        else if(x<-0.3){
                            adapterActivity.setLedRed();
                            speed = (-x)-0.2;
                            if (y > 0.2) rotation = 90;
                            if (y < -0.2) rotation = 270;
                            adapterActivity.moveBackward(180, speed);
                        }

                    */
                    break;

                case ALPHA_RELATIVE:
                    alpha_1 = p.getEegChannelValue(Eeg.EEG1);
                    alpha_2 = p.getEegChannelValue(Eeg.EEG2);
                    alpha_3 = p.getEegChannelValue(Eeg.EEG3);
                    alpha_4 = p.getEegChannelValue(Eeg.EEG4);
                    double alpha_sum = alpha_1+alpha_2+alpha_3+alpha_4;
                    if(super.active) {
                        //if(sum>0.8&& sum<=1.2) adapterActivity.moveForward(0, 0.2);
                        //if(sum>1.2) adapterActivity.moveForward(0, 0.4);
                        //else adapterActivity.stop();
                    }
                    adapterActivity.log(3, "alpha ch1:" + String.format("%.2f", alpha_1) + " ch2:" + String.format("%.2f", alpha_2) + " ch3:" + String.format("%.2f", alpha_3) + " ch4:" + String.format("%.2f", alpha_4) +" sum:"+ String.format("%.2f", alpha_sum));
                    break;

                case BETA_RELATIVE:
                    beta_1 = p.getEegChannelValue(Eeg.EEG1);
                    beta_2 = p.getEegChannelValue(Eeg.EEG2);
                    beta_3 = p.getEegChannelValue(Eeg.EEG3);
                    beta_4 = p.getEegChannelValue(Eeg.EEG4);
                    List l = new ArrayList();
                    l.clear();
                    l.add(beta_1);
                    l.add(beta_2);
                    l.add(beta_3);
                    l.add(beta_4);
                    double beta_sum = beta_1+beta_2+beta_3+beta_4;
                    double beta_max = (double)Collections.max(l);
                    if(active) {
                        //if(beta_sum>0.4 && beta_sum<=0.8) adapterActivity.moveForward(0, 0.2);
                        //if(beta_sum>0.4) adapterActivity.moveForward(0, 0.4);
                        //else adapterActivity.stop();
                    }
                    adapterActivity.log(1, "beta ch1:" + String.format("%.2f", beta_1) + " ch2:" + String.format("%.2f", beta_2) + " ch3:" + String.format("%.2f", beta_3) + " ch4:" + String.format("%.2f", beta_4)+" sum: "+ String.format("%.2f", beta_sum) +" max: "+ String.format("%.2f", beta_max));
                    break;

                case BATTERY:
                case DRL_REF:
                case QUANTIZATION:
                default:
                    break;


            }
        }
        count++;
        if(count > 1000) count = 0;
    }

    /**
     * You will receive a callback to this method each time an artifact packet is generated if you
     * have registered for the ARTIFACTS data type.  MuseArtifactPackets are generated when
     * eye blinks are detected, the jaw is clenched and when the headband is put on or removed.
     *
     * @param p    The artifact packet with the data from the headband.
     * @param muse The headband that sent the information.
     */
    private int blinkColor = 0;

    void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {

        if(p.getBlink()) {
            switch(blinkColor%3) {
                case 0:
                    adapterActivity.setLedBlue();
                    break;
                case 1:
                    adapterActivity.setLedGreen();
                    break;

                case 2:
                    adapterActivity.setLedRed();
                    break;
            }
            blinkColor++;
        }
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