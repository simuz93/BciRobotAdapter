package com.choosemuse.example.multiDevCompApp.drivers;

import android.os.Handler;
import android.widget.ArrayAdapter;

import com.choosemuse.example.multiDevCompApp.MainActivity;
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

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by sserr on 23/01/2018.
 */

public class MuseHeadsetDriver implements Controller{

    private MuseManagerAndroid manager;
    private Muse muse;

    //private MainActivity.ConnectionListener connectionListener;

    //private MainActivity.DataListener dataListener;

    private final Handler handler = new Handler();
    private boolean dataTransmission = true;
    private final AtomicReference<MuseFileWriter> fileWriter = new AtomicReference<>();
    private final AtomicReference<Handler> fileHandler = new AtomicReference<>();



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
}
