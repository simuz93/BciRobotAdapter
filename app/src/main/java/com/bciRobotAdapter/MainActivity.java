package com.bciRobotAdapter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.bciRobotAdapter.devicesTypes.ControllerType;
import com.bciRobotAdapter.devicesTypes.RobotType;
import com.bciRobotAdapter.drivers.controllerDrivers.EmotivInsightDriver;
import com.bciRobotAdapter.drivers.controllerDrivers.MindwaveDriver;
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
 *      -void activate(boolean active) for the controller: this must set a boolean variable, used to activate or pause the controller (e.g.: pausing when using the joystick)
 *      -if needed, log and debug methods for both
 *
 * 2. Implement every not implemented method, according to the device sdk (connection methods, for example).
 *      It's pretty easy to understand what they have to do by reading the name and comments
 *
 * 3. Be SURE that EVERYTIME the physical controller/robot connects/disconnects to the driver you make also a call to notifyControllerConnected(true/false)/notifyRobotConnected(true/false).
 *      This is needed by the AdapterActivity to know when the device is online or not.
 *
 * 4. ONLY FOR CONTROLLER'S DRIVERS: While sending multiple commands to the robot, be sure that every movement method is "filtered" by the boolean readyToSend() method,
 *      implemented in AbstractController and set by the robot itself. This means that the controller should tell the robot to move only if readyToSend()==true.
 *      This method limits the packets send speed according to the robot capacity, which could not handle as well too many commands incoming.
 *      "Instant" commands (like stop or setLed) shouldn't be filtered by this method because of the risk to discard the packet.
 *
 * 5. ONLY FOR ROBOT'S DRIVERS: Note that, if you need, Robots can use the setFrequency(int frequency_Hz) method to limit the number of packets received.
 *
 * 6. Add the proper value in the ControllerType/RobotType enum to make the system aware of the new driver.
 *
 * 7. Add the proper case in the setController/setRobot method here in the MainActivity.
 *      In the new case you should simply create the new controller or robot class and save its instance in the controller or robot variable.
 *      This should be the only MainActivity's modification you do.
 *
 * While writing a new driver, you should not modify AdapterActivity or other existing drivers to solve any of the new driver's problem.
 * This could compromise the app multi compatibility and make other drivers unusable.
 *
 */

public class MainActivity extends Activity implements OnClickListener, AdapterActivity {

    /*==========Joystick variables==========*/
    private Joystick joystick;
    private JoystickListener joystickListener;
    private float jsDirection = 0;
    private float jsOffset = 0;
    private boolean isJoystickTurning = false;

    /*==========Devices variables==========*/
    //Names
    private ControllerType mainControllerName = null;
    private ControllerType auxControllerName = null;
    private RobotType robotName = null;
    //Instances
    private Controller mainController = null;
    private Controller auxController = null;
    private Robot robot = null;
    //Connection status
    private boolean mainControllerConnected = false;
    private boolean auxControllerConnected = false;
    private boolean robotConnected = false;
    //Misc
    private double auxControllerDirection = 0;
    private int FREQUENCY = 1;

    /*==========View elements==========*/
    Button connectMainCtrlBtn, connectAuxCtrlBtn, connectRobotBtn;
    Spinner spinnerMainCtrl, spinnerAuxCtrl, spinnerRobot;
    Button btnL, btnR;
    RelativeLayout controlView;
    Switch jsToTurnSwitch;
    Button switchViewBtn;
    boolean isConnectView;
    boolean isUseView;
    TextView log, cLog, rLog, cAuxLog;

    /*==========Bluetooth receiver==========*/
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

    /*===========================Lifecycle and Activity's methods===========================*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUI();

        //No robot or controller connected yet
        onMainControllerConnected(false);
        onRobotConnected(false);
        onAuxControllerConnected(false);

        //Bluetooth and position check
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);
        this.reactivateBluetoothOrLocation();
        this.unregisterReceiver(mReceiver);
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            //The connect buttons have multiple functions according to the context: Connect, Stop or Disconnect.
            //Connect: starts searching for the selected device and connects to the first available
            //Stop: interrupts the connect's task and stops searching
            //Disconnect: disconnects from the connected device

            case R.id.connectMainCtrl:
                String textCtrl = (String)connectMainCtrlBtn.getText();

                if(textCtrl.equals(getString(R.string.connect))) {
                    connectMainCtrlBtn.setText(R.string.stop);
                    setMainController(ControllerType.valueOf((String)spinnerMainCtrl.getSelectedItem()));
                    setControllerLog("Looking for controller "+spinnerMainCtrl.getSelectedItem()+", press stop to abort");
                    mainController.searchAndConnect();
                }

                else if(textCtrl.equals(getString(R.string.stop))) {
                    setControllerLog("Search stopped");
                    if(mainController!=null) mainController.stopSearching();
                    if(mainControllerConnected) mainController.disconnect();
                    connectMainCtrlBtn.setText(R.string.connect);
                }

                else if(textCtrl.equals(getString(R.string.disconnect))) {
                    connectMainCtrlBtn.setText(R.string.connect);
                    mainController.stopSearching();
                    mainController.disconnect();
                }
                break;

            case R.id.connectRobot:
                String textRobot = (String)connectRobotBtn.getText();

                if(textRobot.equals(getString(R.string.connect))) {
                    connectRobotBtn.setText(R.string.stop);
                    setRobot(RobotType.valueOf((String)spinnerRobot.getSelectedItem()));
                    setRobotLog("Looking for robot "+spinnerRobot.getSelectedItem()+", press stop to abort");
                    robot.searchAndConnect();
                }

                else if(textRobot.equals(getString(R.string.stop))) {
                    setRobotLog("Search stopped");
                    if(robot!=null)robot.stopSearching();
                    if(robotConnected)robot.disconnect();
                    robot = null;
                    connectRobotBtn.setText(R.string.connect);
                }

                else if(textRobot.equals(getString(R.string.disconnect))) {
                    connectRobotBtn.setText(R.string.connect);
                    if(robot!=null)robot.stopSearching();
                    if(robotConnected)robot.disconnect();
                    robot = null;
                }
                break;

            case R.id.connectAuxCtrl:
                String textAuxCtrl = (String)connectAuxCtrlBtn.getText();

                if(textAuxCtrl.equals(getString(R.string.connect))) {
                    connectAuxCtrlBtn.setText(R.string.stop);
                    setAuxController(ControllerType.valueOf((String)spinnerAuxCtrl.getSelectedItem()));
                    setAuxControllerLog("Looking for controller "+spinnerAuxCtrl.getSelectedItem()+", press stop to abort");
                    auxController.searchAndConnect();
                }

                else if(textAuxCtrl.equals(getString(R.string.stop))) {
                    setAuxControllerLog("Search stopped");
                    if(auxController!=null) auxController.stopSearching();
                    if(auxControllerConnected) auxController.disconnect();
                    connectAuxCtrlBtn.setText(R.string.connect);
                }

                else if(textAuxCtrl.equals(getString(R.string.disconnect))) {
                    connectAuxCtrlBtn.setText(R.string.connect);
                    auxController.stopSearching();
                    auxController.disconnect();
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

            case R.id.joystickToTurn:
                isJoystickTurning = jsToTurnSwitch.isChecked();
                if(isJoystickTurning) {
                    joystick.setMotionConstraint(Joystick.MotionConstraint.HORIZONTAL);
                }
                else joystick.setMotionConstraint(Joystick.MotionConstraint.NONE);
                break;

            case R.id.switchViewBtn:
                switchView();
                break;
            default:
                break;
        }
    }

    /*===========================Bluetooth methods===========================*/
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

    /*===========================Get methods===========================*/
    /**
     * {@inheritDoc}
     */
    public Activity getActivity() {
        return this;
    }
    /**
     * {@inheritDoc}
     */
    public Context getContext() {
        return this.getApplicationContext();
    }

    /*===========================UI and View methods===========================*/
    private void initUI() {
        setContentView(R.layout.activity_main);

        getAllUiElements();//Saves an instance of every View element
        initSpinners();
        initJoystick();
        addButtonsListener();

        showConnectView(true);
        showUseView(false);
    }
    private void getAllUiElements() {
        connectMainCtrlBtn = (Button) findViewById(R.id.connectMainCtrl);
        connectRobotBtn = (Button) findViewById(R.id.connectRobot);
        connectAuxCtrlBtn = (Button) findViewById(R.id.connectAuxCtrl);
        btnL = (Button)findViewById(R.id.btnL);
        btnR = (Button)findViewById(R.id.btnR);
        jsToTurnSwitch = (Switch) findViewById(R.id.joystickToTurn);
        spinnerMainCtrl = (Spinner) findViewById(R.id.spinnerCtrl);
        spinnerRobot = (Spinner) findViewById(R.id.spinnerRobot);
        spinnerAuxCtrl = (Spinner) findViewById(R.id.spinnerAuxCtrl);
        switchViewBtn = (Button) findViewById(R.id.switchViewBtn);
        controlView = (RelativeLayout) findViewById(R.id.ControlView);
        log = (TextView)findViewById(R.id.log);
        cLog = (TextView)findViewById(R.id.controllerLog);
        rLog = (TextView)findViewById(R.id.robotLog);
        cAuxLog = (TextView)findViewById(R.id.auxCtrlLog);
    }
    private void addButtonsListener() {
        connectMainCtrlBtn.setOnClickListener(this);
        connectRobotBtn.setOnClickListener(this);
        connectAuxCtrlBtn.setOnClickListener(this);
        btnL.setOnClickListener(this);
        btnR.setOnClickListener(this);
        jsToTurnSwitch.setOnClickListener(this);
        switchViewBtn.setOnClickListener(this);
    }
    private void initSpinners() {
        //Spinners values are from the ControllerType and RobotType enum
        ArrayAdapter<String> spinnerAdapterCtrl = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for(ControllerType ct : ControllerType.values()) {
            spinnerAdapterCtrl.add(ct.toString());
        }
        spinnerMainCtrl.setAdapter(spinnerAdapterCtrl);

        ArrayAdapter<String> spinnerAdapterRobot = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for(RobotType rt : RobotType.values()) {
            spinnerAdapterRobot.add(rt.toString());
        }
        spinnerRobot.setAdapter(spinnerAdapterRobot);

        ArrayAdapter<String> spinnerAdapterAuxCtrl = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for(ControllerType ct : ControllerType.values()) {
            spinnerAdapterAuxCtrl.add(ct.toString());
        }
        spinnerAuxCtrl.setAdapter(spinnerAdapterAuxCtrl);
    }
    private void initJoystick() {
        joystick = (Joystick) findViewById(R.id.joystick);
        joystickListener = new JSListener(this, mainController, auxController);
        joystick.setJoystickListener(joystickListener);
    }
    private void switchView(){
        if(isConnectView && !isUseView) {
            showConnectView(false);
            showUseView(true);
        }
        else if(isUseView && !isConnectView) {
            showUseView(false);
            showConnectView(true);
        }
        else Log.e("SwitchView", "Both connectingView and useView are ON or OFF together. SwitchView request ignored");
    }
    private void showUseView(boolean wantToShow) {
        if(wantToShow) {
            jsToTurnSwitch.setVisibility(View.VISIBLE);
            controlView.setVisibility(View.VISIBLE);
            log.setVisibility(View.VISIBLE);
            isUseView = true;
        }
        else {
            jsToTurnSwitch.setVisibility(View.GONE);
            controlView.setVisibility(View.GONE);
            log.setVisibility(View.GONE);
            isUseView=false;
        }
    }
    private void showConnectView(boolean wantToShow) {
        if(wantToShow) {
            connectMainCtrlBtn.setVisibility(View.VISIBLE);
            spinnerMainCtrl.setVisibility(View.VISIBLE);
            connectRobotBtn.setVisibility(View.VISIBLE);
            spinnerRobot.setVisibility(View.VISIBLE);
            connectAuxCtrlBtn.setVisibility(View.VISIBLE);
            spinnerAuxCtrl.setVisibility(View.VISIBLE);

            cLog.setVisibility(View.VISIBLE);
            rLog.setVisibility(View.VISIBLE);
            cAuxLog.setVisibility(View.VISIBLE);

            TextView explainTextView = (TextView) findViewById(R.id.explainTextView);
            explainTextView.setVisibility(View.VISIBLE);
            isConnectView = true;
        }
        else {
            connectMainCtrlBtn.setVisibility(View.GONE);
            spinnerMainCtrl.setVisibility(View.GONE);
            connectRobotBtn.setVisibility(View.GONE);
            spinnerRobot.setVisibility(View.GONE);
            connectAuxCtrlBtn.setVisibility(View.GONE);
            spinnerAuxCtrl.setVisibility(View.GONE);

            cLog.setVisibility(View.GONE);
            rLog.setVisibility(View.GONE);
            cAuxLog.setVisibility(View.GONE);

            TextView explainTextView = (TextView) findViewById(R.id.explainTextView);
            explainTextView.setVisibility(View.GONE);
            isConnectView = false;
        }
    }

    /*===========================Devices set methods===========================*/
    private void setMainController(ControllerType name) {
        this.mainControllerName = name;

        switch(name) {
            case MUSE_HEADBAND:
                mainController = new MuseHeadsetDriver(this, false);
                break;
            case PHONE_ACCELEROMETER:
                mainController = new PhoneAccelerometerDriver(this, false);
                break;
            case MYO_ARMBAND:
                mainController = new MyoArmbandDriver(this, false);
                break;
            case MINDWAVE_HEADBAND:
                mainController =  new MindwaveDriver(this, false);
                break;
            case EMOTIV_INSIGHT_HEADBAND:
                mainController = new EmotivInsightDriver(this, false);
                break;

            default:
                Log.e("setMainController", "Controller's name unknown. No Controller has been set");
                return;
        }
        mainController.setFrequency(FREQUENCY);
        if(auxController!=null) mainController.setHasAuxiliar(true);
        setControllerLog("Looking for CONTROLLER: "+mainControllerName.name());
    }
    private void setAuxController(ControllerType name) {
        this.auxControllerName = name;

        switch(name) {
            case MUSE_HEADBAND:
                auxController = new MuseHeadsetDriver(this, true);
                break;
            case PHONE_ACCELEROMETER:
                auxController = new PhoneAccelerometerDriver(this, true);
                break;
            case MYO_ARMBAND:
                auxController = new MyoArmbandDriver(this, true);
                break;
            case MINDWAVE_HEADBAND:
                auxController =  new MindwaveDriver(this, true);
                break;
            case EMOTIV_INSIGHT_HEADBAND:
                auxController = new EmotivInsightDriver(this, true);
                break;

            default:
                Log.e("setAuxController", "Controller's name unknown. No Controller has been set");
                return;
        }
        auxController.setFrequency(FREQUENCY);
        if(mainController!=null) mainController.setHasAuxiliar(true);
        setAuxControllerLog("Looking for CONTROLLER: "+auxControllerName.name());
    }
    private void setRobot(RobotType name) {
        this.robotName = name;

        switch (name) {
            case SPHERO_BB8:
                robot = new SpheroBB8Driver(this);
                break;

            //case SANBOT:
            //case TELEPATTY:

            default:
                Log.e("setRobot", "Robot's name unknown. No Robot has been set");
                return;
        }
    }

    /*===========================Devices management methods===========================*/
    //Check connection
    private boolean checkMainController() {return mainController!=null && mainControllerConnected;}
    private boolean checkAuxController() {return auxController!=null && auxControllerConnected;}
    private boolean checkRobot() {return robot!=null && robotConnected;}

    //Listener
    public void onMainControllerConnected(boolean connected){
        mainControllerConnected = connected;

        if(connected) {
            setControllerLog("Connected to CONTROLLER: " + mainControllerName.name());
            connectMainCtrlBtn.setText(R.string.disconnect);
        }
        else {
            if(checkRobot())robot.stop();
            setControllerLog("CONTROLLER disconnected");
            connectMainCtrlBtn.setText(R.string.connect);
        }
    }
    public void onRobotConnected(boolean connected){
        robotConnected = connected;

        if(connected) {
            setRobotLog("Connected to ROBOT: " + robotName.name());
            connectRobotBtn.setText(R.string.disconnect);
        }
        else {
            setRobotLog("ROBOT disconnected");
            connectRobotBtn.setText(R.string.connect);
        }
    }
    public void onAuxControllerConnected(boolean connected){
        auxControllerConnected = connected;

        if(connected) {
            setAuxControllerLog("Connected to AUX CONTROLLER: " + auxControllerName.name());
            connectAuxCtrlBtn.setText(R.string.disconnect);
        }
        else {
            if(checkRobot())robot.stop();
            setAuxControllerLog("AUX CONTROLLER disconnected");
            connectAuxCtrlBtn.setText(R.string.connect);
            if(mainController!=null) mainController.setHasAuxiliar(false);
        }
    }

    //Misc
    public void setAuxCtrlDirection(double direction) {
        this.auxControllerDirection = direction;
    }
    public void setFrequency(int frequency_Hz) {
        this.FREQUENCY = frequency_Hz;
        if(mainController!=null) mainController.setFrequency(frequency_Hz);
    }

    /*===========================Joystick management methods===========================*/
    public void setJsValues(float jsDirection, float jsOffset) {
        this.jsDirection = jsDirection;
        this.jsOffset = jsOffset;
    }
    public boolean isJoystickTurning(){return isJoystickTurning;}
    private float calculateJsRotation(float rotation, float offset) {
        if(rotation > 0) rotation = offset*90;
        else if(rotation < 0) rotation = -offset*90 ;
        else rotation = 0;
        return rotation;
    }


    /*===========================Movement and led methods===========================*/
    //Movement
    public void moveForward(double rotation, double speed) {
        if (checkAuxController() && jsDirection == 0) rotation = auxControllerDirection;
        if (isJoystickTurning) rotation = calculateJsRotation(jsDirection, jsOffset);
        if (checkRobot()) robot.moveForward(rotation, speed);
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


    /*===========================Log and debug methods===========================*/
    public void setGeneralLog(String toWrite) {
        log.setText(toWrite);
    }
    public void setControllerLog(String toWrite) {
        cLog.setText(toWrite);
    }
    public void setAuxControllerLog(String toWrite) {
        cAuxLog.setText(toWrite);
    }
    public void setRobotLog(String toWrite) {
        rLog.setText(toWrite);
    }
}

/*===========================Joystick's listener===========================*/
class JSListener implements com.bciRobotAdapter.joystickLib.JoystickListener {

    private MainActivity mainActivity;
    private Controller mainController;
    private Controller auxController;

    JSListener(MainActivity mainActivity, Controller mainController, Controller auxController) {
        this.mainActivity = mainActivity;
        this.mainController = mainController;
        this.auxController = auxController;
    }

    //When your finger goes down on the joystick
    @Override
    public void onDown() {
        if(!mainActivity.isJoystickTurning()) {
            if (mainController != null) mainController.activate(false);
            if (auxController != null) auxController.activate(false);
        }

    }//Pause the controller while using the joystick

    @Override
    public void onDrag(float degrees, float offset) {
        if(!mainActivity.isJoystickTurning()) {
            if (mainController != null) mainController.activate(false);//Pause the controller while using the joystick
            if (auxController != null) auxController.activate(false);

            if (offset == 0) mainActivity.stop();
            else mainActivity.moveForward(degrees, offset);
        }
        else mainActivity.setJsValues(degrees, offset);
    }

    //When your finger goes back up from the joystick
    @Override
    public void onUp() {
        if(!mainActivity.isJoystickTurning()) {
            if (mainController != null)mainController.activate(true);//Pause the controller while using the joystick
            if (auxController!=null) auxController.activate(true);
            mainActivity.stop();
        }
        mainActivity.setJsValues(0, 0);
    }
}
