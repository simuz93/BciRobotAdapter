/**
 * Example of using libmuse library on android.
 * Interaxon, Inc. 2016
 */

package com.choosemuse.example.multiDevCompApp;

import com.choosemuse.example.multiDevCompApp.drivers.Controller;
import com.choosemuse.example.multiDevCompApp.drivers.MuseHeadsetDriver;


import android.app.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;

/**
 * This example will illustrate how to connect to a Muse headband,
 * register for and receive EEG data and disconnect from the headband.
 * Saving EEG data to a .muse file is also covered.
 *
 * For instructions on how to pair your headband with your Android device
 * please see:
 * http://developer.choosemuse.com/hardware-firmware/bluetooth-connectivity/developer-sdk-bluetooth-connectivity-2
 *
 * Usage instructions:
 * 1. Pair your headband if necessary.
 * 2. Run this project.
 * 3. Turn on the Muse headband.
 * 4. Press "Refresh". It should display all paired Muses in the Spinner drop down at the
 *    top of the screen.  It may take a few seconds for the headband to be detected.
 * 5. Select the headband you want to connect to and press "Connect".
 * 6. You should see EEG and accelerometer data as well as connection status,
 *    version information and relative alpha values appear on the screen.
 * 7. You can pause/resume data transmission with the button at the bottom of the screen.
 * 8. To disconnect from the headband, press "Disconnect"
 */
public class MainActivity extends Activity implements OnClickListener{

    /**
     * Tag used for logging purposes.
     */
    private final String TAG = "TestLibMuseAndroid";


    /**
     * Data comes in from the headband at a very fast rate; 220Hz, 256Hz or 500Hz,
     * depending on the type of headband and the preset configuration.  We buffer the
     * data that is read until we can update the UI.
     *
     * The stale flags indicate whether or not new data has been received and the buffers
     * hold the values of the last data packet received.  We are displaying the EEG, ALPHA_RELATIVE
     * and ACCELEROMETER values in this example.
     *
     * Note: the array lengths of the buffers are taken from the comments in
     * MuseDataPacketType, which specify 3 values for accelerometer and 6
     * values for EEG and EEG-derived packets.
     */
    /*private final double[] eegBuffer = new double[6];
    private boolean eegStale;
    private final double[] alphaBuffer = new double[6];
    private boolean alphaStale;
    private final double[] accelBuffer = new double[3];
    private boolean accelStale;*/

    /**
     * We will be updating the UI using a handler instead of in packet handlers because
     * packets come in at a very high frequency and it only makes sense to update the UI
     * at about 60fps. The update functions do some string allocation, so this reduces our memory
     * footprint and makes GC pauses less frequent/noticeable.
     */
    private final Handler handler = new Handler();

    /**
     * In the UI, the list of Muses you can connect to is displayed in a Spinner object for this example.
     * This spinner adapter contains the MAC addresses of all of the headbands we have discovered.
     */
    private ArrayAdapter<String> spinnerAdapterCtrl;
    private ArrayAdapter<String> spinnerAdapterRobot;
    private Controller controller;

    //--------------------------------------
    // Lifecycle / Connection code


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controller = new MuseHeadsetDriver(this); //static

        initUI();

        // Start our asynchronous updates of the UI.
        //handler.post(tickUi);
    }

    protected void onPause() {
        super.onPause();
        // It is important to call stopListening when the Activity is paused
        // to avoid a resource leak from the LibMuse library.
        controller.stopListening();
    }

    public boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.refreshCtrl) {
            writeScreenLog("Refreshing Controllers");
            // The user has pressed the "Refresh" button.
            // Start listening for nearby or paired Muse headbands. We call stopListening
            // first to make sure startListening will clear the list of headbands and start fresh.
            controller.stopListening();
            controller.startListening();

            Spinner spinnerCtrl = (Spinner) findViewById(R.id.spinnerCtrl);
            spinnerAdapterRobot.addAll(controller.getSpinnerCtrlList());
            spinnerCtrl.setAdapter(spinnerAdapterCtrl);

        } else if (v.getId() == R.id.connectCtrl) {
            writeScreenLog("Connecting");
            controller.stopListening();
            Spinner spinnerCtrl = (Spinner) findViewById(R.id.spinnerCtrl);
            controller.connect(spinnerCtrl.getSelectedItemPosition());
        }

        else if (v.getId() == R.id.refreshRobot) {
            writeScreenLog("refreshRobot");
        }

        else if (v.getId() == R.id.BtnUp) writeScreenLog("Forward Pressed");
        else if (v.getId() == R.id.BtnL) writeScreenLog("Left Pressed");
        else if (v.getId() == R.id.BtnMid) writeScreenLog("Stop Pressed");
        else if (v.getId() == R.id.BtnR) writeScreenLog("Right Pressed");
        else if (v.getId() == R.id.BtnDown) writeScreenLog("Backward Pressed");

        /*else if (v.getId() == R.id.disconnect) {

            // The user has pressed the "Disconnect" button.
            // Disconnect from the selected Muse.
            if (muse != null) {
                muse.disconnect();
            }

        } else if (v.getId() == R.id.pause) {

            // The user has pressed the "Pause/Resume" button to either pause or
            // resume data transmission.  Toggle the state and pause or resume the
            // transmission on the headband.
            if (muse != null) {
                dataTransmission = !dataTransmission;
                muse.enableDataTransmission(dataTransmission);
            }
         else if
        }*/
    }




    /**
     * Helper methods to get different packet values.  These methods simply store the
     * data in the buffers for later display in the UI.
     *
     * getEegChannelValue can be used for any EEG or EEG derived data packet type
     * such as EEG, ALPHA_ABSOLUTE, ALPHA_RELATIVE or HSI_PRECISION.  See the documentation
     * of MuseDataPacketType for all of the available values.
     * Specific packet types like ACCELEROMETER, GYRO, BATTERY and DRL_REF have their own
     * getValue methods.
     *//*
    *//*private void getEegChannelValues(double[] buffer, MuseDataPacket p) {
        buffer[0] = p.getEegChannelValue(Eeg.EEG1);
        buffer[1] = p.getEegChannelValue(Eeg.EEG2);
        buffer[2] = p.getEegChannelValue(Eeg.EEG3);
        buffer[3] = p.getEegChannelValue(Eeg.EEG4);
        buffer[4] = p.getEegChannelValue(Eeg.AUX_LEFT);
        buffer[5] = p.getEegChannelValue(Eeg.AUX_RIGHT);
    }*//*
    *//*
    private void getAccelValues(MuseDataPacket p) {
        accelBuffer[0] = p.getAccelerometerValue(Accelerometer.X);
        accelBuffer[1] = p.getAccelerometerValue(Accelerometer.Y);
        accelBuffer[2] = p.getAccelerometerValue(Accelerometer.Z);

    }*/


    //--------------------------------------
    // UI Specific methods

    /**
     * Initializes the UI of the example application.
     */
    private void initUI() {
        setContentView(R.layout.activity_main);
        addButtonListener();
        addSpinnerListener();
    }

    private void addButtonListener() {
        Button refreshCtrl = (Button) findViewById(R.id.refreshCtrl);
        refreshCtrl.setOnClickListener(this);
        Button connectCtrl = (Button) findViewById(R.id.connectCtrl);
        connectCtrl.setOnClickListener(this);

        Button refreshRobot = (Button) findViewById(R.id.refreshRobot);
        refreshRobot.setOnClickListener(this);
        Button connectRobot = (Button) findViewById(R.id.connectRobot);
        connectRobot.setOnClickListener(this);

        Button btnUp = (Button) findViewById(R.id.BtnUp);
        btnUp.setOnClickListener(this);
        Button btnL = (Button) findViewById(R.id.BtnL);
        btnL.setOnClickListener(this);
        Button btnR = (Button) findViewById(R.id.BtnR);
        btnR.setOnClickListener(this);
        Button btnDown = (Button) findViewById(R.id.BtnDown);
        btnDown.setOnClickListener(this);

        Button btnMid = (Button) findViewById(R.id.BtnMid);
        btnMid.setOnClickListener(this);

        //Button disconnectButton = (Button) findViewById(R.id.disconnect);
        //disconnectButton.setOnClickListener(this);
        //Button pauseButton = (Button) findViewById(R.id.pause);
        //pauseButton.setOnClickListener(this);
    }

    private void addSpinnerListener() {
        spinnerAdapterCtrl = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        Spinner spinnerCtrl = (Spinner) findViewById(R.id.spinnerCtrl);
        spinnerCtrl.setAdapter(spinnerAdapterCtrl);

        spinnerAdapterRobot = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        Spinner spinnerRobot = (Spinner) findViewById(R.id.spinnerRobot);
        spinnerRobot.setAdapter(spinnerAdapterRobot);
    }

    public Handler getHandler() {return this.handler;}

    public void writeScreenLog(String toWrite) {
        TextView log = (TextView) findViewById(R.id.log);
        log.setText(toWrite);
    }

    public void debug(int debugTextNum, String toWrite) {
        TextView debug;
        switch(debugTextNum) {
            case 1:
               debug = (TextView)findViewById(R.id.debugText1);
               break;

            case 2:
                debug = (TextView)findViewById(R.id.debugText2);
                break;

            case 3:
            default:
                debug = (TextView)findViewById(R.id.debugText3);
                break;
        }

        debug.setText(toWrite);
    }



    /**
     * The runnable that is used to update the UI at 60Hz.
     *
     * We update the UI from this Runnable instead of in packet handlers
     * because packets come in at high frequency -- 220Hz or more for raw EEG
     * -- and it only makes sense to update the UI at about 60fps. The update
     * functions do some string allocation, so this reduces our memory
     * footprint and makes GC pauses less frequent/noticeable.
     */
    /*private final Runnable tickUi = new Runnable() {
        @Override
        public void run() {
            if (eegStale) {
                //updateEeg();
            }
            if (accelStale) {
                //updateAccel();
            }
            if (alphaStale) {
                //updateAlpha();
                trySignal();
            }
            handler.postDelayed(tickUi, 1000 / 60);
        }
    };*/

    /**
     * The following methods update the TextViews in the UI with the data
     * from the buffers.
     */
    /*private void updateAccel() {
        TextView acc_x = (TextView)findViewById(R.id.acc_x);
        TextView acc_y = (TextView)findViewById(R.id.acc_y);
        TextView acc_z = (TextView)findViewById(R.id.acc_z);
        acc_x.setText(String.format("%6.2f", accelBuffer[0]));
        acc_y.setText(String.format("%6.2f", accelBuffer[1]));
        acc_z.setText(String.format("%6.2f", accelBuffer[2]));
        TextView accelLabel = (TextView)findViewById(R.id.accelLabel);
        String status="";
        float threesholdValue = (float) 0.2;
        if(accelBuffer[0]>threesholdValue){
            status += "-avanti";
            //VolleyClient.volleyRequest("http://192.168.4.1/message?message=avanti", getApplicationContext());

        }
        else if(accelBuffer[0]<-threesholdValue){
            status += "-indietro";
        }
        if(accelBuffer[1]>threesholdValue){
            status += "-destra";
            //VolleyClient.volleyRequest("http://192.168.4.1/message?message=destra", getApplicationContext());

        }
        else if(accelBuffer[1]<-threesholdValue){
            status += "-sinistra";
            //VolleyClient.volleyRequest("http://192.168.4.1/message?message=sinistra", getApplicationContext());

        }

            accelLabel.setText("acc value "+status);


    }

    private int count=0;
    private double[] previousEegBuffer;

    private double[][] circularBuffer;
    private void updateEeg() {

        if(previousEegBuffer==null){

            previousEegBuffer = new double[]{eegBuffer[0], eegBuffer[1], eegBuffer[2], eegBuffer[3]};
        }
        if(circularBuffer==null){
            circularBuffer = new double[100][4];
        }
        TextView tp9 = (TextView)findViewById(R.id.eeg_tp9);
        TextView fp1 = (TextView)findViewById(R.id.eeg_af7);
        TextView fp2 = (TextView)findViewById(R.id.eeg_af8);
        TextView tp10 = (TextView)findViewById(R.id.eeg_tp10);

        tp9.setText(String.format("%6.2f", eegBuffer[0]));
        fp1.setText(String.format("%6.2f", eegBuffer[1]));
        fp2.setText(String.format("%6.2f", eegBuffer[2]));
        tp10.setText(String.format("%6.2f", eegBuffer[3]));
        Log.d("log", "blink sx detected"+(eegBuffer[1]-previousEegBuffer[1]));

        if(eegBuffer[1]-previousEegBuffer[1]>100){
            fp1.setText("blk sx");
            Log.d("log121", "blink sx detected "+count);
            count+=1;
        }
        if(eegBuffer[2]-previousEegBuffer[2]>100){
            fp2.setText("blk dx");
            Log.d("log", "blink dx detected");
        }
        for (int i=0;i<4;i++){
            previousEegBuffer[i]=eegBuffer[i];
        }

    }

    private void updateAlpha() {
        TextView elem1 = (TextView)findViewById(R.id.elem1);
        elem1.setText(String.format("%6.2f", alphaBuffer[0]));
        TextView elem2 = (TextView)findViewById(R.id.elem2);
        elem2.setText(String.format("%6.2f", alphaBuffer[1]));
        TextView elem3 = (TextView)findViewById(R.id.elem3);
        elem3.setText(String.format("%6.2f", alphaBuffer[2]));
        TextView elem4 = (TextView)findViewById(R.id.elem4);
        elem4.setText(String.format("%6.2f", alphaBuffer[3]));
        TextView EEGLabel = (TextView) findViewById(R.id.alphaLabel);

        if((alphaBuffer[1]+alphaBuffer[2])>60) {
            Log.d("azione", "avanti");
            EEGLabel.setText("avanti");
            VolleyClient.volleyRequest("http://192.168.4.1/message?message=avanti", getApplicationContext());
        }
        if(alphaBuffer[0]>alphaBuffer[3]&&alphaBuffer[0]>30){
            Log.d("azione", "sinistra");
            EEGLabel.setText("sinistra");
            VolleyClient.volleyRequest("http://192.168.4.1/message?message=sinistra", getApplicationContext());
        }
        if(alphaBuffer[3]>alphaBuffer[0] && alphaBuffer[3]>30){
            Log.d("azione", "destra");
            EEGLabel.setText("destra");
            VolleyClient.volleyRequest("http://192.168.4.1/message?message=destra", getApplicationContext());
        }
    }*/

}
