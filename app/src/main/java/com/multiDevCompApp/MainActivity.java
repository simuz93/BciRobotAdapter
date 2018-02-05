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
import android.graphics.Color;
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

        //Name (Enum) of the devices in use
        private ControllerType controllerName = null;
        private RobotType robotName = null;

        //Devices (Instances) in use
        private Controller controller = null;
        private Robot robot = null;

        //Spinners adapters
        private ArrayAdapter<String> spinnerAdapterCtrl;
        private ArrayAdapter<String> spinnerAdapterRobot;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            initUI();

            //Bluetooth and position check
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            this.registerReceiver(mReceiver, filter);
            this.reactivateBluetoothOrLocation();
            this.unregisterReceiver(mReceiver);
        }

        protected void onPause() {
            super.onPause();
            if(controller != null) controller.stopSearching();
            if(robot != null) robot.stopSearching();
        }

        protected void onResume() {
            super.onResume();

            //Check if MainActivity has been called by SelectDevicesActivity and set Controller and Robot variables
            Intent i = this.getIntent();
            if(i.hasExtra("ctrlName") && i.hasExtra("robotName")) {
                controllerName = (ControllerType) (i.getSerializableExtra("ctrlName"));
                robotName = (RobotType) (i.getSerializableExtra("robotName"));
                setController(controllerName);
                setRobot(robotName);
                setAllVisible(View.VISIBLE);
                setAllClickable(true);
            }
            /*if(!checkDevices()) {
                startActivity(new Intent(this, SelectDevicesActivity.class));
            }*/

        }

        // UI Specific methods
        private void initUI() {
            //Set View, Spinners, Buttons, listeners and initial visibility
            setContentView(R.layout.activity_main);
            addButtonListener();
            setSpinners();
            if(!checkDevices()) {
                setAllClickable(false);
                setAllVisible(View.GONE);
            }
            else {
                setAllClickable(true);
                setAllVisible(View.VISIBLE);
            }
        }

        //UI listener add methods
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

            Button selectDevices = (Button)findViewById((R.id.selectDevices));
            selectDevices.setOnClickListener(this);

            Button disconnectCtrlBtn = (Button) findViewById(R.id.disconnectController);
            disconnectCtrlBtn.setOnClickListener(this);
            Button disconnectRobotBtn = (Button) findViewById(R.id.disconnectRobot);
            disconnectRobotBtn.setOnClickListener(this);

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

        //Set the Controller's name, instance and textview
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

            ((TextView)findViewById(R.id.ctrlName)).setText("Looking for CONTROLLER: "+controllerName.name());
            //controller.stopSearching();
            //controller.startSearching();

            //updateControllerSpinner();

        }

        //Set the Robot's name, instance and textview
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

            ((TextView)findViewById(R.id.robotName)).setText("Looking for ROBOT: "+robotName.name());
            //robot.stopSearching();
            //robot.startSearching();

            //updateRobotSpinner();
        }

        //Set every view element (except for SelectDevices button) clickable or not, according to b
        private void setAllClickable(boolean b) {

            (findViewById(R.id.BtnUp)).setClickable(b);
            (findViewById(R.id.BtnL)).setClickable(b);
            (findViewById(R.id.BtnMid)).setClickable(b);
            (findViewById(R.id.BtnR)).setClickable(b);
            (findViewById(R.id.BtnDown)).setClickable(b);

            (findViewById(R.id.connectCtrl)).setClickable(b);
            (findViewById(R.id.refreshCtrl)).setClickable(b);
            (findViewById(R.id.connectRobot)).setClickable(b);
            (findViewById(R.id.refreshRobot)).setClickable(b);

            (findViewById(R.id.spinnerCtrl)).setClickable(b);
            (findViewById(R.id.spinnerRobot)).setClickable(b);
        }

        //Set every view element (except for SelectDevices button) visibile or not, according to v (View.VISIBLE, View.INVISIBILE, etc)
        private void setAllVisible(int v) {
            (findViewById(R.id.BtnUp)).setVisibility(v);
            (findViewById(R.id.BtnL)).setVisibility(v);
            (findViewById(R.id.BtnMid)).setVisibility(v);
            (findViewById(R.id.BtnR)).setVisibility(v);
            (findViewById(R.id.BtnDown)).setVisibility(v);

            (findViewById(R.id.connectCtrl)).setVisibility(v);
            (findViewById(R.id.refreshCtrl)).setVisibility(v);
            (findViewById(R.id.connectRobot)).setVisibility(v);
            (findViewById(R.id.refreshRobot)).setVisibility(v);

            (findViewById(R.id.spinnerCtrl)).setVisibility(v);
            (findViewById(R.id.spinnerRobot)).setVisibility(v);
        }

        //Check if both Controller and Robot are set and eventually modifies the SelectDevices button
        private boolean checkDevices() {
            Button b = (Button)findViewById(R.id.selectDevices);
            if(controllerName == null || robotName == null) {
                b.setText("Seleziona i dispositivi che vuoi usare");
                b.setTextColor(Color.RED);
                return false;
            }
            else {
                b.setText("Modifica i dispositivi che vuoi usare");
                b.setTextColor(Color.BLACK);
                return true;
            }
        }

        @Override
        //onClick methods
        public void onClick(View v) {
            switch (v.getId()) {

                //Look for new Controllers
                case R.id.refreshCtrl:
                    writeScreenLog("Refreshing Controllers");
                    controller.stopSearching();
                    controller.startSearching();
                    updateControllerSpinner();
                    break;

                //Connect to the selected Controller
                case R.id.connectCtrl:
                    writeScreenLog("Connecting to controller");
                    controller.stopSearching();
                    Spinner spinnerCtrl = (Spinner) findViewById(R.id.spinnerCtrl);
                    if (spinnerCtrl.getSelectedItemPosition() >= 0) controller.connect(spinnerCtrl.getSelectedItemPosition());
                    break;

                //Disconnect from Controller
                case R.id.disconnectController:
                    if(controller!=null) {
                        controller.disconnect();
                        controller = null;
                    }
                    break;

                //Look for new Robots
                case R.id.refreshRobot:
                    writeScreenLog("refreshRobot");
                    robot.stopSearching();
                    robot.startSearching();
                    updateRobotSpinner();
                    break;

                //Connect to the selected Robot
                case R.id.connectRobot:
                    writeScreenLog("Connecting to robot");
                    robot.stopSearching();
                    Spinner spinnerRobot = (Spinner) findViewById(R.id.spinnerRobot);
                    if (spinnerRobot.getSelectedItemPosition() >= 0) robot.connect(spinnerRobot.getSelectedItemPosition());
                    break;

                //Disconnect from Robot
                case R.id.disconnectRobot:
                    if(robot!=null) {
                        robot.disconnect();
                        robot = null;
                    }
                    break;

                //Buttons cases
                case R.id.BtnUp:
                    writeScreenLog("Forward Pressed");
                    robot.moveForward(0, 0.2); //static
                    break;

                case R.id.BtnL:
                    writeScreenLog("Left Pressed");
                    robot.turnL();
                    break;

                case R.id.BtnMid:
                    writeScreenLog("Stop Pressed");
                    robot.stop();
                    break;

                case R.id.BtnR:
                    writeScreenLog("Right Pressed");
                    robot.turnR();
                    break;

                case R.id.BtnDown:
                    writeScreenLog("Backward Pressed");
                    robot.moveBackward(0, 0.2); //static
                    break;

                //Launch the SelectDevicesActivity
                case R.id.selectDevices:
                    Intent i = new Intent(this, SelectDevicesActivity.class);
                    startActivity(i);
                    break;

                default:
                    writeScreenLog("default");
                    break;
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

        //Update the list of avaiable Controllers for the spinner
        private void updateControllerSpinner() {
            if (controller != null && controller.getCtrlList().size() > 0) {
                Spinner spinnerCtrl = (Spinner) findViewById(R.id.spinnerCtrl);
                spinnerAdapterCtrl.addAll(controller.getCtrlList());
                spinnerCtrl.setAdapter(spinnerAdapterCtrl);
            }
        }

        //Update the list of avaiable Robots for the spinner
        private void updateRobotSpinner() {
            if (robot != null && robot.getRobotList().size() > 0) {
                Spinner spinnerRobot = (Spinner) findViewById(R.id.spinnerRobot);
                spinnerAdapterRobot.addAll(robot.getRobotList());
                spinnerRobot.setAdapter(spinnerAdapterRobot);
            }

        }

        //Bluetooth methods
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

        //Log and debug methods
        public void writeScreenLog(String toWrite) {
            TextView log = (TextView) findViewById(R.id.log);
            log.setText(toWrite);
            System.out.println("LOG: "+toWrite);
        }

}
