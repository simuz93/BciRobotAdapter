package com.multiDevCompApp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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


import com.multiDevCompApp.drivers.MuseHeadsetDriver;
import com.multiDevCompApp.drivers.MyoArmbandDriver;
import com.multiDevCompApp.drivers.PhoneAccelerometerDriver;
import com.multiDevCompApp.drivers.SpheroBB8Driver;
import com.multiDevCompApp.drivers.driversInterfaces.AdapterActivity;
import com.multiDevCompApp.drivers.driversInterfaces.Controller;
import com.multiDevCompApp.drivers.driversInterfaces.Robot;
import com.multiDevCompApp.joystickLib.Joystick;
import com.multiDevCompApp.joystickLib.JoystickListener;


public class MainActivity extends Activity implements OnClickListener, AdapterActivity {

        //Joystick variables
        private Joystick joystick;
        private JoystickListener joystickListener;

        //Name (Enum) of the devices in use
        private ControllerType controllerName = null;
        private RobotType robotName = null;

        //Devices (Instances) in use
        private Controller controller = null;
        private Robot robot = null;

        //Boolean to check if the devices are connected
        private boolean controllerConnected = false;
        private boolean robotConnected = false;


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

        // UI Specific methods
        private void initUI() {
            //Set View, Spinners, Buttons, listeners
            setContentView(R.layout.activity_main);
            joystick = (Joystick) findViewById(R.id.joystick);
            joystickListener = new JSListener(this, controller);
            joystick.setJoystickListener(joystickListener);
            addButtonListener();
            setSpinners();
            onControllerConnected(false);
            onRobotConnected(false);
        }

        private void showCtrlConnectBtn() {
            Button connectCtrlBtn = (Button) findViewById(R.id.connectCtrl);
            Button disconnectCtrlBtn = (Button) findViewById(R.id.disconnectCtrl);

            connectCtrlBtn.setVisibility(View.VISIBLE);
            connectCtrlBtn.setClickable(true);
            connectCtrlBtn.setText("Connect");

            disconnectCtrlBtn.setVisibility(View.GONE);
            disconnectCtrlBtn.setClickable(false);
        }

        private void showRobotConnectBtn() {
            Button connectRobotBtn = (Button) findViewById(R.id.connectRobot);
            Button disconnectRobotBtn = (Button) findViewById(R.id.disconnectRobot);

            connectRobotBtn.setVisibility(View.VISIBLE);
            connectRobotBtn.setClickable(true);
            connectRobotBtn.setText("Connect");

            disconnectRobotBtn.setVisibility(View.GONE);
            disconnectRobotBtn.setClickable(false);
        }

        private void showCtrlDisconnectBtn() {
            Button connectCtrlBtn = (Button) findViewById(R.id.connectCtrl);
            Button disconnectCtrlBtn = (Button) findViewById(R.id.disconnectCtrl);

            disconnectCtrlBtn.setVisibility(View.VISIBLE);
            disconnectCtrlBtn.setClickable(true);

            connectCtrlBtn.setVisibility(View.GONE);
            connectCtrlBtn.setClickable(false);
        }

        private void showRobotDisconnectBtn() {
            Button connectRobotBtn = (Button) findViewById(R.id.connectRobot);
            Button disconnectRobotBtn = (Button) findViewById(R.id.disconnectRobot);

            disconnectRobotBtn.setVisibility(View.VISIBLE);
            disconnectRobotBtn.setClickable(true);

            connectRobotBtn.setVisibility(View.GONE);
            connectRobotBtn.setClickable(false);
        }

        //UI listener add methods
        private void addButtonListener() {

            Button connectCtrl = (Button) findViewById(R.id.connectCtrl);
            connectCtrl.setOnClickListener(this);
            Button connectRobot = (Button) findViewById(R.id.connectRobot);
            connectRobot.setOnClickListener(this);

            Button disconnectCtrlBtn = (Button) findViewById(R.id.disconnectCtrl);
            disconnectCtrlBtn.setOnClickListener(this);
            Button disconnectRobotBtn = (Button) findViewById(R.id.disconnectRobot);
            disconnectRobotBtn.setOnClickListener(this);

            Button btnL = (Button)findViewById(R.id.btnL);
            btnL.setOnClickListener(this);
            Button btnR = (Button)findViewById(R.id.btnR);
            btnR.setOnClickListener(this);
        }

        private void setSpinners() {

            ArrayAdapter<String> spinnerAdapterCtrl = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
            Spinner spinnerCtrl = (Spinner) findViewById(R.id.spinnerCtrl);
            for(ControllerType ct : ControllerType.values()) {
                spinnerAdapterCtrl.add(ct.toString());
            }
            spinnerCtrl.setAdapter(spinnerAdapterCtrl);

            ArrayAdapter<String> spinnerAdapterRobot = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
            Spinner spinnerRobot = (Spinner) findViewById(R.id.spinnerRobot);
            for(RobotType rt : RobotType.values()) {
                spinnerAdapterRobot.add(rt.toString());
            }
            spinnerRobot.setAdapter(spinnerAdapterRobot);
        }

        //Set the Controller's name, instance and textview
        public void setController(ControllerType name) {
            this.controllerName = name;

            switch(name) {
                case MUSE_HEADBAND:
                    controller = new MuseHeadsetDriver(this);
                    break;
                case PHONE_ACCELEROMETER:
                    controller = new PhoneAccelerometerDriver(this);
                    break;
                case MYO_ARMBAND:
                    controller = new MyoArmbandDriver(this);
                    break;
                case MINDWAVE_HEADBAND:

                case EPOC_INSIGHT_HEADBAND:

                default:
                    break;
            }

            ((TextView)findViewById(R.id.ctrlName)).setText("Looking for CONTROLLER: "+controllerName.name());
        }

        //Set the Robot's name, instance and textview
        public void setRobot(RobotType name) {
            this.robotName = name;

            switch (name) {
                case SPHERO_BB8:
                    robot = new SpheroBB8Driver(this);
                    break;

                case SANBOT:
                case TELEPATTY:

                default:
                    break;
            }

            ((TextView) findViewById(R.id.robotName)).setText("Looking for ROBOT: " + robotName.name());
        }

        @Override
        //onClick methods
        public void onClick(View v) {
            switch (v.getId()) {

                //Connect to the selected Controller
                case R.id.connectCtrl:
                    ((Button)findViewById(R.id.connectCtrl)).setText("Wait");
                    Spinner ctrlSpinner = (Spinner) findViewById(R.id.spinnerCtrl);
                    setController(ControllerType.valueOf((String)ctrlSpinner.getSelectedItem()));
                    controller.stopSearching();
                    controller.startSearching();
                    break;

                //Disconnect from Controller
                case R.id.disconnectCtrl:
                    if(checkController()) {
                        controller.disconnect();
                        controller = null;
                    }
                    break;

                //Connect to the selected Robot
                case R.id.connectRobot:
                    ((Button)findViewById(R.id.connectRobot)).setText("Wait");
                    Spinner robotSpinner = (Spinner) findViewById((R.id.spinnerRobot));
                    setRobot(RobotType.valueOf((String)robotSpinner.getSelectedItem()));
                    robot.stopSearching();
                    robot.startSearching();
                    break;

                //Disconnect from Robot
                case R.id.disconnectRobot:
                    if(checkRobot()) {
                        robot.disconnect();
                        robot = null;
                    }
                    break;

                case R.id.btnL:
                    turnL(90);
                    break;

                case R.id.btnR:
                    turnR(90);
                    break;

                default:
                    break;
            }

        }

        public void moveForward(double rotation, double speed) {
            if (checkRobot()) robot.moveForward(rotation, speed);
        }
        public void moveBackward(double rotation, double speed){
            //if(checkRobot()) robot.moveBackward(rotation, speed);
        }
        public void stop() {
            if(checkRobot()) robot.stop();
        }
        public void turnL(double rotation) {
            if(checkRobot()) robot.turnL(rotation);
        }
        public void turnR(double rotation) {
            if(checkRobot())robot.turnR(rotation);
        }

        //Led
        public void setLedRed() {if(checkRobot()) robot.setLedRed();}
        public void setLedBlue() {if(checkRobot()) robot.setLedBlue();}
        public void setLedGreen() {if(checkRobot()) robot.setLedGreen();}
        public void setLedYellow() {if(checkRobot()) robot.setLedYellow();}
        public void setLedWhite() {if(checkRobot()) robot.setLedWhite();}
        public void setLedOff() {if(checkRobot()) robot.setLedOff();}

        public Activity getActivity() {
        return this;
    }

        public void onControllerConnected(boolean connected){
            controllerConnected = connected;
            if(connected) {
                showCtrlDisconnectBtn();
                ((TextView) findViewById(R.id.ctrlName)).setText("Connected to CONTROLLER: " + controllerName.name());
            }
            else {
                showCtrlConnectBtn();
                ((TextView) findViewById(R.id.ctrlName)).setText("No CONTROLLER connected. Press Connect to search");
            }
        }

        public void onRobotConnected(boolean connected){
            robotConnected = connected;
            if(connected) {
                showRobotDisconnectBtn();
                ((TextView) findViewById(R.id.robotName)).setText("Connected to ROBOT: " + robotName.name());
            }
            else {
                showRobotConnectBtn();
                ((TextView) findViewById(R.id.robotName)).setText("No ROBOT connected. Press Connect to search");
            }
        }

        private boolean checkController() {return controller!=null && controllerConnected;}

        private boolean checkRobot() {return robot!=null && robotConnected;}

        //Bluetooth methods
        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                assert action != null;
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
            assert mLocationManager != null;
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
        public void log(int n, String toWrite) {

            TextView log;

            switch (n) {
                case 1:
                    log = (TextView) findViewById(R.id.log_1);
                    break;
                case 2:
                    log = (TextView) findViewById(R.id.log_2);
                    break;
                case 3:
                    log = (TextView) findViewById(R.id.log_3);
                    break;
                default:
                    log = (TextView) findViewById(R.id.log_1);
                    break;
            }

            log.setText(toWrite);
            System.out.println("LOG "+n+": "+toWrite);
        }

}

class JSListener implements com.multiDevCompApp.joystickLib.JoystickListener {

    private AdapterActivity mainActivity;
    private Controller controller;

    JSListener(AdapterActivity mainActivity, Controller controller) {

        this.mainActivity = mainActivity;
        this.controller = controller;
    }

    @Override
    public void onDown() {
        if(controller != null) controller.activate(false);
    }

    @Override
    public void onDrag(float degrees, float offset) {
        if(offset == 0) mainActivity.stop();
        else mainActivity.moveForward(degrees, offset);
    }

    @Override
    public void onUp() {
        if(controller != null) controller.activate(true);
        mainActivity.stop();
    }
}
