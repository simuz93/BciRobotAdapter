/**
 * Example of using libmuse library on android.
 * Interaxon, Inc. 2016
 */

package com.choosemuse.example.multiDevCompApp;

import com.choosemuse.example.multiDevCompApp.drivers.Controller;
import com.choosemuse.example.multiDevCompApp.drivers.MuseHeadsetDriver;

import android.app.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;

public class MainActivity extends Activity implements OnClickListener{

    private ControllerType controllerName = null;
    private RobotType robotName = null;

    private ArrayAdapter<String> spinnerAdapterCtrl;
    private ArrayAdapter<String> spinnerAdapterRobot;
    private Controller controller;

    //--------------------------------------
    // Lifecycle / Connection code


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setController(ControllerType.MUSE_HEADBAND); //static

        initUI();
    }

    protected void onPause() {
        super.onPause();
        controller.stopListening();
    }

    public boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.refreshCtrl) {
            writeScreenLog("Refreshing Controllers");
            controller.stopListening();
            controller.startListening();

            if(controller.getSpinnerCtrlList().size()>0) {
                Spinner spinnerCtrl = (Spinner) findViewById(R.id.spinnerCtrl);
                spinnerAdapterRobot.addAll(controller.getSpinnerCtrlList());
                spinnerCtrl.setAdapter(spinnerAdapterCtrl);
            }

        } else if (v.getId() == R.id.connectCtrl) {
            writeScreenLog("Connecting");
            controller.stopListening();
            Spinner spinnerCtrl = (Spinner) findViewById(R.id.spinnerCtrl);
            if(spinnerCtrl.getSelectedItemPosition()>=0) controller.connect(spinnerCtrl.getSelectedItemPosition());
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

    // UI Specific methods

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

    public void setController(ControllerType name) {
        this.controllerName = name;

        switch(name) {
            case MUSE_HEADBAND:
                controller = new MuseHeadsetDriver(this);
                break;

            case MINDWAVE_HEADBAND:
            case MYO_ARMBAND:
            case EPOC_INSIGHT_HEADBAND:

            default:
                controller = new MuseHeadsetDriver(this); //static
                break;
        }
    }

    public void setRobot(RobotType name) {this.robotName = name;}

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

}
