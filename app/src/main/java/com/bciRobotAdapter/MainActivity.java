package com.bciRobotAdapter;

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

import com.bciRobotAdapter.devicesTypes.ControllerType;
import com.bciRobotAdapter.devicesTypes.RobotType;
import com.bciRobotAdapter.drivers.controllerDrivers.MuseHeadsetDriver;
import com.bciRobotAdapter.drivers.controllerDrivers.MyoArmbandDriver;
import com.bciRobotAdapter.drivers.controllerDrivers.PhoneAccelerometerDriver;
import com.bciRobotAdapter.drivers.robotDrivers.SpheroBB8Driver;
import com.bciRobotAdapter.drivers.interfaces.Controller;
import com.bciRobotAdapter.drivers.interfaces.Robot;

import com.bciRobotAdapter.joystickLib.Joystick;
import com.bciRobotAdapter.joystickLib.JoystickListener;

/**===============How to use bciRobotAdapter===============**/

/**MainActivity should not be modified unless you want to add some features or change the view (add buttons or something else).
 * In this case, the new MainActivity must implements AdapterActivity to works with existent drivers.
 *
 *
 * -----HOW TO WRITE AND ADD A NEW DRIVER-----
 *
 * The procedure to add a new driver is the same for new controllers and new robots.
 *
 * 1. Create the new driver class. It should extends AbstractController/AbstractRobot (which provides some useful methods) to work properly with any AdapterActivity.
 *      If you need to extends something else, you still could bypass the abstract driver and implements directly the interface (Controller or Robot),
 *      but you will have to rewrite every method needed. In particular:
 *
 *      -void notifyControllerConnected(boolean connected) for the controller: this must call AdapterActivity.onControllerConnected(boolean connected) (see point 3 of this gguide)
 *      -void notifyRobotConnected(boolean connected) for the robot: this must call AdapterActivity.onControllerConnected(boolean connected) (see point 3 of this guide)
 *      -void activate(boolean active) for the controller: this must set a boolean variable (see point 4 of this guide)
 *      -if needed, log and debug methods for both
 *
 * 2. Implement every not implemented method, according to the device sdk (connection methods, for example).
 *      It's pretty easy to understand what they have to do by reading the name and comments
 *
 * 3. Be SURE that EVERYTIME the physical controller/robot connects/disconnects to the driver you make also a call to notifyControllerConnected(true/false)/notifyRobotConnected(true/false).
 *      This is needed by the AdapterActivity to know when the device is online or not.
 *
 * 4. ONLY FOR CONTROLLER'S DRIVERS: Be sure that every command method is "filtered" by the active variable (managed by the active(boolean active) method).
 *      If it's false, no command should be sent to the AdapterActivity, which is probably trying to control the robot only using the joystick on screen bypassing the controller.
 *
 * 5. Add the proper value in the ControllerType/RobotType enum to make the system aware of the new driver.
 *
 *
 * While writing a new driver, you should not modify AdapterActivity or other existing drivers to solve any of the new driver's problem.
 * This could compromise the app multi compatibility and make other drivers unusable.
 *
 */

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

    //Bluetooth receiver
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

    //Get methods
    public Activity getActivity() {
        return this;
    }
    public Context getContext() {
        return this.getApplicationContext();
    }

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

    @Override
    protected void onPause() {
        super.onPause();
        //if(controller != null) controller.stopSearching();
        //if(robot != null) robot.stopSearching();
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

        //No robot or controller connected yet
        onControllerConnected(false);
        onRobotConnected(false);
    }

    //Add listener to buttons
    private void addButtonListener() {

        Button connectCtrl = (Button) findViewById(R.id.connectCtrl);
        connectCtrl.setOnClickListener(this);
        Button connectRobot = (Button) findViewById(R.id.connectRobot);
        connectRobot.setOnClickListener(this);

        Button btnL = (Button)findViewById(R.id.btnL);
        btnL.setOnClickListener(this);
        Button btnR = (Button)findViewById(R.id.btnR);
        btnR.setOnClickListener(this);
    }

    //Set up spinners
    private void setSpinners() {

        //Spinners values are from the ControllerType and RobotType enum
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

    //Set the Controller's name and instance
    private void setController(ControllerType name) {
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
            //case MINDWAVE_HEADBAND:

            //case EMOTIV_INSIGHT_HEADBAND:

            default:
                break;
        }
        setControllerLog("Looking for CONTROLLER: "+controllerName.name());
    }

    //Set the Robot's name and instance
    private void setRobot(RobotType name) {
        this.robotName = name;

        switch (name) {
            case SPHERO_BB8:
                robot = new SpheroBB8Driver(this);
                break;

            //case SANBOT:
            //case TELEPATTY:

            default:
                break;
        }
    }

    @Override
    //onClick methods
    public void onClick(View v) {
        switch (v.getId()) {

            //The connect buttons have multiple functions according to the context: Connect, Stop or Disconnect.
            //Connect: starts searching for the selected device and connects to the first available
            //Stop: interrupts the connect's task and stops searching
            //Disconnect: disconnects from the connected device

            case R.id.connectCtrl:
                Button connectCtrlBtn = (Button)findViewById(R.id.connectCtrl);
                String textCtrl = (String)connectCtrlBtn.getText();

                if(textCtrl.equals("Connect")) {
                    connectCtrlBtn.setText("Stop");
                    Spinner ctrlSpinner = (Spinner) findViewById(R.id.spinnerCtrl);
                    setController(ControllerType.valueOf((String)ctrlSpinner.getSelectedItem()));
                    setControllerLog("Looking for controller "+ctrlSpinner.getSelectedItem()+", press stop to abort");
                    controller.stopSearching();
                    controller.searchAndConnect();
                }

                else if(textCtrl.equals("Stop")) {
                    setControllerLog("Search stopped");
                    if(controller!=null) controller.stopSearching();
                    if(controllerConnected) controller.disconnect();
                    connectCtrlBtn.setText(R.string.connect);
                }

                else if(textCtrl.equals("Disconnect")) {
                    connectCtrlBtn.setText(R.string.connect);
                    controller.stopSearching();
                    controller.disconnect();
                }
                break;

            case R.id.connectRobot:
                Button connectRobotBtn =(Button)findViewById(R.id.connectRobot);
                String textRobot = (String)connectRobotBtn.getText();

                if(textRobot.equals("Connect")) {
                    connectRobotBtn.setText("Stop");
                    Spinner robotSpinner = (Spinner) findViewById((R.id.spinnerRobot));
                    setRobot(RobotType.valueOf((String)robotSpinner.getSelectedItem()));
                    setRobotLog("Looking for robot "+robotSpinner.getSelectedItem()+", press stop to abort");
                    robot.stopSearching();
                    robot.searchAndConnect();
                }

                else if(textRobot.equals("Stop")) {
                    setRobotLog("Search stopped");
                    if(robot!=null)robot.stopSearching();
                    if(robotConnected)robot.disconnect();
                    robot = null;
                    connectRobotBtn.setText(R.string.connect);
                }

                else if(textRobot.equals("Disconnect")) {
                    connectRobotBtn.setText(R.string.connect);
                    if(robot!=null)robot.stopSearching();
                    if(robotConnected)robot.disconnect();
                    robot = null;
                }
                break;

            //Left button clicked
            case R.id.btnL:
                turnL(90);//Turn the robot 90 degrees left
                break;

            //Right button clicked
            case R.id.btnR:
                turnR(90);//Turn the robot 90 degrees right
                break;

            default:
                break;
        }

    }

    //Movement methods
    public void moveForward(double rotation, double speed) {
        if (checkRobot()) robot.moveRobotForward(rotation, speed);
    }
    public void stop() {
        if(checkRobot()) robot.stopRobot();
    }
    public void turnL(double rotation) {
        if(checkRobot()) robot.turnRobotL(rotation);
    }
    public void turnR(double rotation) {
        if(checkRobot())robot.turnRobotR(rotation);
    }

    //Led
    public void setLedRed() {if(checkRobot()) robot.setRobotLedRed();}
    public void setLedBlue() {if(checkRobot()) robot.setRobotLedBlue();}
    public void setLedGreen() {if(checkRobot()) robot.setRobotLedGreen();}
    public void setLedYellow() {if(checkRobot()) robot.setRobotLedYellow();}
    public void setLedWhite() {if(checkRobot()) robot.setRobotLedWhite();}
    public void setLedOff() {if(checkRobot()) robot.setRobotLedOff();}

    //Callback to these methods when a controller or a robot connect to his driver
    public void onControllerConnected(boolean connected){
        controllerConnected = connected;
        Button connectCtrlBtn =(Button)findViewById(R.id.connectCtrl);

        if(connected) {
            setControllerLog("Connected to CONTROLLER: " + controllerName.name());
            connectCtrlBtn.setText(R.string.disconnect);
        }
        else {
            setControllerLog("CONTROLLER disconnected");
            connectCtrlBtn.setText(R.string.connect);
        }
    }
    public void onRobotConnected(boolean connected){
        robotConnected = connected;
        Button connectRobotBtn =(Button)findViewById(R.id.connectRobot);

        if(connected) {
            setRobotLog("Connected to ROBOT: " + robotName.name());
            connectRobotBtn.setText(R.string.disconnect);
        }
        else {
            setRobotLog("ROBOT disconnected");
            connectRobotBtn.setText(R.string.connect);
        }
    }

    //Check controller/robot connection and instance
    private boolean checkController() {return controller!=null && controllerConnected;}
    private boolean checkRobot() {return robot!=null && robotConnected;}

    //Bluetooth methods
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
    public void setGeneralLog(String toWrite) {
        TextView log = (TextView)findViewById(R.id.log);
        log.setText(toWrite);
    }

    public void setControllerLog(String toWrite) {
        TextView cLog = (TextView)findViewById(R.id.controllerLog);
        cLog.setText(toWrite);
    }

    public void setRobotLog(String toWrite) {
        TextView rLog = (TextView)findViewById(R.id.robotLog);
        rLog.setText(toWrite);
    }

}

//Joystick listener
class JSListener implements com.bciRobotAdapter.joystickLib.JoystickListener {

    private AdapterActivity mainActivity;
    private Controller controller;

    JSListener(AdapterActivity mainActivity, Controller controller) {

        this.mainActivity = mainActivity;
        this.controller = controller;
    }

    //When your finger goes down on the joystick
    @Override
    public void onDown() {
        if(controller!=null)controller.activate(false);
    }//Pause the controller while using the joystick

    @Override
    public void onDrag(float degrees, float offset) {
        if(controller!=null)controller.activate(false);//Pause the controller while using the joystick
        if(offset == 0) mainActivity.stop();
        else mainActivity.moveForward(degrees, offset);
    }

    //When your finger goes back up from the joystick
    @Override
    public void onUp() {
        if(controller!=null)controller.activate(true);//Pause the controller while using the joystick
        mainActivity.stop();
    }
}
