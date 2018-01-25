/**
 * Example of using libmuse library on android.
 * Interaxon, Inc. 2016
 */

package com.multiDevCompApp;

import com.multiDevCompApp.drivers.SpheroBB8Driver;
import com.multiDevCompApp.drivers.interfaces.Controller;
import com.multiDevCompApp.drivers.MuseHeadsetDriver;
import com.multiDevCompApp.drivers.interfaces.Robot;

import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
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
    private Robot robot;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        reactivateBluetoothOrLocation();
                        break;
                }
            }
        }
    };

    public void reactivateBluetoothOrLocation(){
        // Check if location is still enabled
        final LocationManager mLocationManager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE );
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivity(new Intent(MainActivity.this, BluetoothConnectionActivity.class));
        }

        // Check if bluetooth is still enabled
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()){
            startActivity(new Intent(MainActivity.this, BluetoothConnectionActivity.class));
        }
    }



    //--------------------------------------
    // Lifecycle / Connection code


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);

        // Check if bluetooth or location are still enabled
        this.reactivateBluetoothOrLocation();
        setController(ControllerType.MUSE_HEADBAND); //static
        setRobot(RobotType.SPHERO_BB8); //static
        this.unregisterReceiver(mReceiver);


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

            if(controller.getCtrlList().size()>0) {
                Spinner spinnerCtrl = (Spinner) findViewById(R.id.spinnerCtrl);
                spinnerAdapterCtrl.addAll(controller.getCtrlList());
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

        else if (v.getId() == R.id.connectRobot) {
            robot.connect(0);
        }


        else if (v.getId() == R.id.BtnUp) {
            writeScreenLog("Forward Pressed");
            robot.ledOn(0,0,1);
            robot.forward();
        }
        else if (v.getId() == R.id.BtnL) {
            writeScreenLog("Left Pressed");
            robot.ledOn(0,0,1);
            robot.turnL();
        }
        else if (v.getId() == R.id.BtnMid) {
            writeScreenLog("Stop Pressed");
            robot.ledOn(1,0,0);
            robot.stop();
        }
        else if (v.getId() == R.id.BtnR) {
            writeScreenLog("Right Pressed");
            robot.ledOn(0,0,1);
            robot.turnR();
        }

        else if (v.getId() == R.id.BtnDown) {
            writeScreenLog("Backward Pressed");
            robot.ledOn(1,0,0);
            robot.backward();
        }

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
        setSpinners();
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

    private void setSpinners() {
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
                break;
        }
    }

    public void setRobot(RobotType name) {
        this.robotName = name;

        switch(name) {
            case SPHERO_BB8:
                robot = new SpheroBB8Driver(this);
                break;

            case SANBOT:
            case TELEPATTY:

            default:
                break;
        }
    }

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
