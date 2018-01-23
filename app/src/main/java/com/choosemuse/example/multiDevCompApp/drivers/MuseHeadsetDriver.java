package com.choosemuse.example.multiDevCompApp.drivers;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.choosemuse.example.multiDevCompApp.MainActivity;
import com.choosemuse.example.multiDevCompApp.R;
import com.choosemuse.libmuse.Accelerometer;
import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.LibmuseVersion;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseConnectionListener;
import com.choosemuse.libmuse.MuseConnectionPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;
import com.choosemuse.libmuse.MuseFileWriter;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;
import com.choosemuse.libmuse.MuseVersion;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by sserr on 23/01/2018.
 */

public class MuseHeadsetDriver implements Controller {

    private final Activity mainActivity;
    private static MuseHeadsetDriver singletonInstance;

    private MuseManagerAndroid manager;
    private Muse muse;

    private ConnectionListener connectionListener;
    private DataListener dataListener;

    private boolean dataTransmission = true;
    //private final AtomicReference<MuseFileWriter> fileWriter = new AtomicReference<>();
    //private final AtomicReference<Handler> fileHandler = new AtomicReference<>();

    private MuseHeadsetDriver(Activity mainActivity) {
        this.mainActivity = mainActivity;

        manager = MuseManagerAndroid.getInstance();
        manager.setContext(mainActivity);

        WeakReference<MuseHeadsetDriver> weakDriver;
        weakDriver = new WeakReference<MuseHeadsetDriver>(this);

        connectionListener = new ConnectionListener(weakDriver);
        dataListener = new DataListener(weakDriver);
        manager.setMuseListener(new MuseL(weakDriver));

        ensurePermissions();
    }

    public MuseHeadsetDriver getInstance(Activity mainActivity) {
        if (singletonInstance == null) singletonInstance = new MuseHeadsetDriver(mainActivity);
        return singletonInstance;
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
    public boolean connect() {
        return false;
    }

    @Override
    public boolean disconnect() {
        return false;
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

}

    // Listeners

    /**
     * You will receive a callback to this method each time a headband is discovered.
     * In this example, we update the spinner with the MAC address of the headband.
     */
    public void museListChanged() {
        final List<Muse> list = manager.getMuses();
        spinnerAdapterCtrl.clear();
        for (Muse m : list) {
            spinnerAdapterCtrl.add(m.getName() + " - " + m.getMacAddress());
        }
    }

    /**
     * You will receive a callback to this method each time there is a change to the
     * connection state of one of the headbands.
     * @param p     A packet containing the current and prior connection states
     * @param muse  The headband whose state changed.
     */
    public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {

        final ConnectionState current = p.getCurrentConnectionState();

        // Format a message to show the change of connection state in the UI.
        final String status = p.getPreviousConnectionState() + " -> " + current;
        Log.i(TAG, status);

        // Update the UI with the change in connection state.
        handler.post(new Runnable() {
            @Override
            public void run() {

                //final TextView statusText = (TextView) findViewById(R.id.con_status);
                //statusText.setText(status);

                final MuseVersion museVersion = muse.getMuseVersion();
                //final TextView museVersionText = (TextView) findViewById(R.id.version);
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
        });

        if (current == ConnectionState.DISCONNECTED) {
            Log.i(TAG, "Muse disconnected:" + muse.getName());

            // We have disconnected from the headband, so set our cached copy to null.
            this.muse = null;
        }
    }

    /**
     * You will receive a callback to this method each time the headband sends a MuseDataPacket
     * that you have registered.  You can use different listeners for different packet types or
     * a single listener for all packet types as we have done here.
     * @param p     The data packet containing the data from the headband (eg. EEG data)
     * @param muse  The headband that sent the information.
     */
    public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {

        // valuesSize returns the number of data values contained in the packet.
        final long n = p.valuesSize();
        switch (p.packetType()) {
            case EEG:
                debug(2, "eeg ch1:"+ p.getEegChannelValue(Eeg.EEG1) + " ch2:"+ p.getEegChannelValue(Eeg.EEG2) + " ch3:"+ p.getEegChannelValue(Eeg.EEG3) + " ch4:"+ p.getEegChannelValue(Eeg.EEG4));

                //assert(eegBuffer.length >= n);
                //getEegChannelValues(eegBuffer,p);
                //eegStale = true;
                break;
            case ACCELEROMETER:
                debug(3, "acc X:"+ p.getAccelerometerValue(Accelerometer.X) + " Y:"+ p.getAccelerometerValue(Accelerometer.Y) + " Z:"+ p.getAccelerometerValue(Accelerometer.Z));
                //assert(accelBuffer.length >= n);
                //getAccelValues(p);
                //accelStale = true;
                break;
            case ALPHA_RELATIVE:
                //assert(alphaBuffer.length >= n);
                debug(1, "alpha ch1:"+ p.getEegChannelValue(Eeg.EEG1) + " ch2:"+ p.getEegChannelValue(Eeg.EEG2) + " ch3:"+ p.getEegChannelValue(Eeg.EEG3) + " ch4:"+ p.getEegChannelValue(Eeg.EEG4));
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
     * @param p     The artifact packet with the data from the headband.
     * @param muse  The headband that sent the information.
     */
    public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
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
        MuseDriverRef.get().museListChanged();
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
        MuseDriverRef.get().receiveMuseDataPacket(p, muse);
    }

    @Override
    public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
        MuseDriverRef.get().receiveMuseArtifactPacket(p, muse);
    }
}