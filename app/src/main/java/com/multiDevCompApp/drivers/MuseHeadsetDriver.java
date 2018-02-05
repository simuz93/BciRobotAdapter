package com.multiDevCompApp.drivers;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.multiDevCompApp.MainActivity;
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
import com.multiDevCompApp.drivers.interfaces.Controller;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sserr on 23/01/2018.
 */

public class MuseHeadsetDriver implements Controller {

    private final MainActivity mainActivity;

    private MuseManagerAndroid manager;
    private Muse muse;
    private MuseVersion museVersion;

    private ConnectionListener connectionListener;
    private DataListener dataListener;

    ArrayList<String> spinnerCtrlList;

    //private boolean dataTransmission = true;
    //private final AtomicReference<MuseFileWriter> fileWriter = new AtomicReference<>();
    //private final AtomicReference<Handler> fileHandler = new AtomicReference<>();

    public MuseHeadsetDriver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        manager = MuseManagerAndroid.getInstance();
        manager.setContext(mainActivity);

        WeakReference<MuseHeadsetDriver> weakDriver;
        weakDriver = new WeakReference<>(this);

        connectionListener = new ConnectionListener(weakDriver);
        dataListener = new DataListener(weakDriver);
        manager.setMuseListener(new MuseL(weakDriver));

        spinnerCtrlList = new ArrayList<>();
        ensurePermissions();
    }

    @Override
    public void forward() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void turnL() {

    }

    @Override
    public void turnR() {

    }

    @Override
    public boolean connect(int index) {
        manager.stopListening();
        List<Muse> availableMuses = manager.getMuses();

        if (availableMuses.size() < 1 || spinnerCtrlList.size() < 1) {

        } else {

            // Cache the Muse that the user has selected.
            muse = availableMuses.get(index);
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

            // Initiate a connection to the headband and stream the data asynchronously.
            muse.runAsynchronously();

        }
        return true;
    }

    @Override
    public boolean disconnect() {
        return true;
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

        if (ContextCompat.checkSelfPermission(mainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // We don't have the ACCESS_COARSE_LOCATION permission so create the dialogs asking
            // the user to grant us the permission.

            DialogInterface.OnClickListener buttonListener =
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ActivityCompat.requestPermissions(mainActivity,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    0);
                        }
                    };

            // This is the context dialog which explains to the user the reason we are requesting
            // this permission.  When the user presses the positive (I Understand) button, the
            // standard Android permission dialog will be displayed (as defined in the button
            // listener above).
            AlertDialog introDialog = new AlertDialog.Builder(mainActivity)
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage(R.string.permission_dialog_description)
                    .setPositiveButton(R.string.permission_dialog_understand, buttonListener)
                    .create();
            introDialog.show();
        }
    }

    public ArrayList<String> getCtrlList() {return this.spinnerCtrlList;}

    // Listeners

    public void deviceListChanged() {
        final List<Muse> list = manager.getMuses();
        spinnerCtrlList.clear();
        for (Muse m : list) {
            spinnerCtrlList.add(m.getName() + " - " + m.getMacAddress());
        }
    }

    /**
     * You will receive a callback to this method each time there is a change to the
     * connection state of one of the headbands.
     *
     * @param p    A packet containing the current and prior connection states
     * @param muse The headband whose state changed.
     */
    public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {

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
    public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {

        // valuesSize returns the number of data values contained in the packet.
        final long n = p.valuesSize();
        switch (p.packetType()) {
            case EEG:
                //mainActivity.debug(2, "eeg ch1:" + p.getEegChannelValue(Eeg.EEG1) + " ch2:" + p.getEegChannelValue(Eeg.EEG2) + " ch3:" + p.getEegChannelValue(Eeg.EEG3) + " ch4:" + p.getEegChannelValue(Eeg.EEG4));

                //assert(eegBuffer.length >= n);
                //getEegChannelValues(eegBuffer,p);
                //eegStale = true;
                break;
            case ACCELEROMETER:
                //mainActivity.debug(3, "acc X:" + p.getAccelerometerValue(Accelerometer.X) + " Y:" + p.getAccelerometerValue(Accelerometer.Y) + " Z:" + p.getAccelerometerValue(Accelerometer.Z));
                //assert(accelBuffer.length >= n);
                //getAccelValues(p);
                //accelStale = true;
                break;
            case ALPHA_RELATIVE:
                //assert(alphaBuffer.length >= n);
                mainActivity.writeScreenLog("alpha ch1:" + p.getEegChannelValue(Eeg.EEG1) + " ch2:" + p.getEegChannelValue(Eeg.EEG2) + " ch3:" + p.getEegChannelValue(Eeg.EEG3) + " ch4:" + p.getEegChannelValue(Eeg.EEG4));
                //getEegChannelValues(alphaBuffer,p);
                //alphaStale = true;
                break;
            case BATTERY:
            case DRL_REF:
            case QUANTIZATION:
            default:
                break;
        }
    }

    /**
     * You will receive a callback to this method each time an artifact packet is generated if you
     * have registered for the ARTIFACTS data type.  MuseArtifactPackets are generated when
     * eye blinks are detected, the jaw is clenched and when the headband is put on or removed.
     *
     * @param p    The artifact packet with the data from the headband.
     * @param muse The headband that sent the information.
     */
    public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
    }

}
// Listener translators
//
// Each of these classes extend from the appropriate listener and contain a weak reference
// to the activity.  Each class simply forwards the messages it receives back to the Activity.
class MuseL extends MuseListener {
    final WeakReference<MuseHeadsetDriver> MuseDriverRef;

    MuseL(final WeakReference<MuseHeadsetDriver> activityRef) {
        this.MuseDriverRef = activityRef;
    }

    @Override
    public void museListChanged() {
        MuseDriverRef.get().deviceListChanged();
    }
}

class ConnectionListener extends MuseConnectionListener {
    final WeakReference<MuseHeadsetDriver> MuseDriverRef;

    ConnectionListener(final WeakReference<MuseHeadsetDriver> activityRef) {
        this.MuseDriverRef = activityRef;
    }

    @Override
    public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {
        MuseDriverRef.get().receiveMuseConnectionPacket(p, muse);
    }
}

class DataListener extends MuseDataListener {
    final WeakReference<MuseHeadsetDriver> MuseDriverRef;

    DataListener(final WeakReference<MuseHeadsetDriver> activityRef) {
        this.MuseDriverRef = activityRef;
    }

    @Override
    public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {
        if(MuseDriverRef!=null) MuseDriverRef.get().receiveMuseDataPacket(p, muse);
    }

    @Override
    public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
        MuseDriverRef.get().receiveMuseArtifactPacket(p, muse);
    }
}