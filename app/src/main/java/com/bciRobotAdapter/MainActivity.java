package com.bciRobotAdapter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.support.design.widget.NavigationView;

import com.bciRobotAdapter.devicesTypes.*;
import com.bciRobotAdapter.drivers.controllerDrivers.*;
import com.bciRobotAdapter.drivers.robotDrivers.*;
import com.bciRobotAdapter.drivers.interfaces.*;

import java.util.HashMap;

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

public class MainActivity extends AppCompatActivity implements AdapterActivity, NavigationView.OnNavigationItemSelectedListener {

    /*==========Joystick variables==========*/
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

    private FragmentDataMap fragmentDataMap;

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
        if (savedInstanceState == null) {
            HomeFragment homeFragment = new HomeFragment();
            fragmentDataMap = new FragmentDataMap();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.dinamic_frame, homeFragment, "homeFrag");
            transaction.commit();
        }

        initUI();

        //Bluetooth and position check
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);
        this.reactivateBluetoothOrLocation();
        this.unregisterReceiver(mReceiver);
    }

    public void onConnectMainControllerPressed(ControllerType name) {
        setMainController(name);
        mainController.searchAndConnect();
    }
    public void onStopMainControllerConnectionPressed() {
        if(mainController!=null) mainController.stopSearching();
        if(mainControllerConnected) mainController.disconnect();
    }
    public void onDisconnectMainControllerPressed() {
        mainController.stopSearching();
        mainController.disconnect();
    }

    public void onConnectAuxControllerPressed(ControllerType name) {
        setAuxController(name);
        auxController.searchAndConnect();
    }
    public void onStopAuxControllerConnectionPressed() {
        if(auxController!=null) auxController.stopSearching();
        if(auxControllerConnected) auxController.disconnect();
    }
    public void onDisconnectAuxControllerPressed() {
        auxController.stopSearching();
        auxController.disconnect();
    }

    public void onConnectRobotPressed(RobotType name) {
        setRobot(name);
        robot.searchAndConnect();
    }
    public void onStopRobotConnectionPressed() {
        if(robot!=null)robot.stopSearching();
        if(robotConnected)robot.disconnect();
        robot = null;
    }
    public void onDisconnectRobotPressed() {
        if(robot!=null)robot.stopSearching();
        if(robotConnected)robot.disconnect();
        robot = null;
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
    public Activity getActivity() {
        return this;
    }
    public Context getContext() {
        return this.getApplicationContext();
    }

    public void addFragmentData(String key, String value){
        fragmentDataMap.addData(key,value);
    }
    public String getFragmentData (String key) {
        return fragmentDataMap.getData(key);
    }

    /*===========================UI and View methods===========================*/
    private void initUI() {
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        LinearLayout sideNavLayout = (LinearLayout) header.findViewById(R.id.sideNavLayout);
        //sideNavLayout.setBackgroundResource(R.drawable.ic_bb8_bg);
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
        if(auxController!=null) mainController.setHasAuxiliary(true);
        setMainControllerLog("Looking for: "+mainControllerName.name());
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
        if(mainController!=null) mainController.setHasAuxiliary(true);
        setAuxControllerLog("Looking for: "+auxControllerName.name());
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
        setRobotLog("Looking for: "+robotName.name());
    }

    /*===========================Devices management methods===========================*/
    //Check connection
    private boolean checkMainController() {return mainController!=null && mainControllerConnected;}
    private boolean checkAuxController() {return auxController!=null && auxControllerConnected;}
    private boolean checkRobot() {return robot!=null && robotConnected;}

    //Listener
    public void onMainControllerConnected(boolean connected){
        mainControllerConnected = connected;
        if(!connected&&checkRobot()) robot.stop();

        ConnectionFragment cf = (ConnectionFragment) getSupportFragmentManager().findFragmentByTag("connFrag");
        if(cf!=null)cf.onMainControllerConnected(connected, mainControllerName.name());

        if(connected) addFragmentData("mainControllerConnected", mainControllerName.name());
        else addFragmentData("mainControllerConnected", null);
    }
    public void onRobotConnected(boolean connected){
        robotConnected = connected;

        ConnectionFragment cf = (ConnectionFragment) getSupportFragmentManager().findFragmentByTag("connFrag");
        if(cf!=null)cf.onRobotConnected(connected, robotName.name());

        if(connected) addFragmentData("robotConnected", robotName.name());
        else addFragmentData("robotConnected", null);
    }
    public void onAuxControllerConnected(boolean connected){
        auxControllerConnected = connected;

        ConnectionFragment cf = (ConnectionFragment) getSupportFragmentManager().findFragmentByTag("connFrag");
        if(cf!=null)cf.onAuxControllerConnected(connected, auxControllerName.name());

        if(connected) addFragmentData("auxControllerConnected", auxControllerName.name());
        else addFragmentData("robotConnected", null);

        if(!connected) {
            if(mainController!=null) mainController.setHasAuxiliary(false);
            if(checkRobot())robot.stop();
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
    private void setJsValues(float jsDirection, float jsOffset) {
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
    public void setJoystickTurning(boolean isJoystickTurning) {
        if(isJoystickTurning) setDriveLog("Now you can use the joystick to turn the robot!");
        else setDriveLog("Joystick turning function off");
        this.isJoystickTurning = isJoystickTurning;
    }

    /*===========================Movement and led methods===========================*/
    //Movement
    public void moveForward(double rotation, double speed) {
        boolean isBackward = false;
        if(rotation>-90 && rotation<90) isBackward = true;

        if (checkAuxController() && jsDirection == 0) rotation = auxControllerDirection;
        if (isJoystickTurning) rotation = calculateJsRotation(jsDirection, jsOffset);
        if (checkRobot()) {
            setDriveLog("Moving: rotation: "+String.format("%.2f",rotation)+"\tspeed: "+String.format("%.2f", speed));
            robot.moveForward(rotation, speed);
        }
    }
    public void stop() {
        if(checkRobot()) {
            setDriveLog("Stopped");
            robot.stop();
        }
    }
    public void turnL(double rotation) {
        if(checkRobot()) {
            setDriveLog("Turning Left: "+rotation);
            if(checkRobot())robot.turnL(rotation);
        }
    }
    public void turnR(double rotation) {
        setDriveLog("Turning Right: "+rotation);
        if(checkRobot())robot.turnR(rotation);
    }
    //Led
    public void setLedRed() {
        if(checkRobot()) {
            setDriveLog("Led: red");
            robot.setLedRed();
        }
    }
    public void setLedBlue() {
        if(checkRobot()) {
            setDriveLog("Led: blue");
            robot.setLedBlue();
        }
    }
    public void setLedGreen() {if(checkRobot()) {
        setDriveLog("Led: green");
        robot.setLedGreen();
    }}
    public void setLedYellow() {if(checkRobot()) {
        setDriveLog("Led: yellow");
        robot.setLedYellow();
    }}
    public void setLedWhite() {if(checkRobot()) {
        setDriveLog("Led: white");
        robot.setLedWhite();
    }}
    public void setLedOff() {if(checkRobot()) {
        setDriveLog("Led: off");
        robot.setLedOff();
    }}

    /*===========================Log and debug methods===========================*/

    public void setMainControllerLog(String toWrite) {
        ConnectionFragment cf = (ConnectionFragment) getSupportFragmentManager().findFragmentByTag("connFrag");
        if(cf!=null) cf.setMainControllerLog(toWrite);
        addFragmentData("mainControllerLog", toWrite);
    }
    public void setAuxControllerLog(String toWrite) {
        ConnectionFragment cf = (ConnectionFragment) getSupportFragmentManager().findFragmentByTag("connFrag");
        if(cf!=null) cf.setAuxControllerLog(toWrite);
        addFragmentData("auxControllerLog", toWrite);
    }
    public void setRobotLog(String toWrite) {
        ConnectionFragment cf = (ConnectionFragment) getSupportFragmentManager().findFragmentByTag("connFrag");
        if(cf!=null) cf.setRobotLog(toWrite);
        addFragmentData("robotLog", toWrite);
    }

    public void setMainControllerOutput(String toWrite) {
        ConnectionFragment cf = (ConnectionFragment) getSupportFragmentManager().findFragmentByTag("connFrag");
        if(cf!=null) cf.setMainControllerOutput(toWrite);
        addFragmentData("mainControllerOutput", toWrite);
    }
    public void setAuxControllerOutput(String toWrite) {
        ConnectionFragment cf = (ConnectionFragment) getSupportFragmentManager().findFragmentByTag("connFrag");
        if(cf!=null) cf.setAuxControllerOutput(toWrite);
        addFragmentData("auxControllerOutput", toWrite);
    }
    public void setRobotOutput(String toWrite) {
        ConnectionFragment cf = (ConnectionFragment) getSupportFragmentManager().findFragmentByTag("connFrag");
        if(cf!=null) cf.setRobotOutput(toWrite);
        addFragmentData("robotOutput", toWrite);
    }

    @Override
    public void setMoveForwardInst(String inst) {
        InstructionsFragment instF = (InstructionsFragment) getSupportFragmentManager().findFragmentByTag("iFrag");
        if(instF!=null)instF.setMoveForwardInst(inst);
        addFragmentData("moveForwardInst", inst);
    }
    @Override
    public void setMoveBackwardInst(String inst) {
        InstructionsFragment instF = (InstructionsFragment) getSupportFragmentManager().findFragmentByTag("iFrag");
        if(instF!=null)instF.setMoveBackwardInst(inst);
        addFragmentData("moveBackwardInst", inst);
    }
    @Override
    public void setMoveLeftInst(String inst) {
        InstructionsFragment instF = (InstructionsFragment) getSupportFragmentManager().findFragmentByTag("iFrag");
        if(instF!=null)instF.setMoveLeftInst(inst);
        addFragmentData("moveLeftInst", inst);
    }
    @Override
    public void setMoveRightInst(String inst) {
        InstructionsFragment instF = (InstructionsFragment) getSupportFragmentManager().findFragmentByTag("iFrag");
        if(instF!=null)instF.setMoveRightInst(inst);
        addFragmentData("moveRightInst", inst);
    }
    @Override
    public void setTurnLeftInst(String inst) {
        InstructionsFragment instF = (InstructionsFragment) getSupportFragmentManager().findFragmentByTag("iFrag");
        if(instF!=null)instF.setTurnLeftInst(inst);
        addFragmentData("turnLeftInst", inst);
    }
    @Override
    public void setTurnRightInst(String inst) {
        InstructionsFragment instF = (InstructionsFragment) getSupportFragmentManager().findFragmentByTag("iFrag");
        if(instF!=null)instF.setTurnRightInst(inst);
        addFragmentData("turnRightInst", inst);
    }
    @Override
    public void setLedInst(String inst) {
        InstructionsFragment instF = (InstructionsFragment) getSupportFragmentManager().findFragmentByTag("iFrag");
        if(instF!=null)instF.setLedInst(inst);
        addFragmentData("ledInst", inst);
    }

    //todo: se viene disconnesso auxCtrl, ripristinare le istruzioni per usare mainCtrl
    @Override
    public void resetAllInstructions() {
        addFragmentData("moveForwardInst", null);
        addFragmentData("moveBackwardInst", null);
        addFragmentData("moveLeftInst", null);
        addFragmentData("moveRightInst", null);
        addFragmentData("turnLeftInst", null);
        addFragmentData("turnRightInst", null);
        addFragmentData("ledInst", null);
    }
    @Override
    public void resetTurnInstructions() {
        addFragmentData("moveLeftInst", null);
        addFragmentData("moveRightInst", null);
        addFragmentData("turnLeftInst", null);
        addFragmentData("turnRightInst", null);
    }
    @Override
    public void resetMoveInstructions() {
        addFragmentData("moveForwardInst", null);
        addFragmentData("moveBackwardInst", null);
        addFragmentData("moveLeftInst", null);
        addFragmentData("moveRightInst", null);
    }


    public void setDriveLog(String toWrite) {
        JoystickFragment jsf = (JoystickFragment) getSupportFragmentManager().findFragmentByTag("jsFrag");
        if(jsf!=null) jsf.setDriveLog(toWrite);
        addFragmentData("driveLog", toWrite);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentTransaction transaction;

        switch (item.getItemId()) {

            case R.id.nav_home:
                HomeFragment homeFragment = new HomeFragment();
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.addToBackStack("homeFrag");
                transaction.replace(R.id.dinamic_frame, homeFragment, "homeFrag");
                transaction.commit();
                break;

            case R.id.nav_connection:
                ConnectionFragment connectionFragment = new ConnectionFragment();
                connectionFragment.initFragment(this);
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.addToBackStack("connFrag");
                transaction.replace(R.id.dinamic_frame, connectionFragment, "connFrag");
                transaction.commit();
                break;

            case R.id.nav_drive:
                JoystickFragment joystickFragment = new JoystickFragment();
                joystickFragment.initFragment(this);
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.addToBackStack("jsFrag");
                transaction.replace(R.id.dinamic_frame, joystickFragment, "jsFrag");
                transaction.commit();
                break;

            case R.id.nav_instructions:
                InstructionsFragment instructionsFragment = new InstructionsFragment();
                instructionsFragment.initFragment(this);
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.addToBackStack("iFrag");
                transaction.replace(R.id.dinamic_frame, instructionsFragment, "iFrag");
                transaction.commit();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void activateMainController(boolean active) {
        if(mainController!=null)mainController.activate(active);
    }
    public void activateAuxController(boolean active) {
        if(auxController!=null)auxController.activate(active);
    }

    /*===========================Joystick Listener============================*/
    public void onJsDown() {
        if(!isJoystickTurning()) {
            activateMainController(false);
            activateAuxController(false);
        }
    }

    public void onJsDrag(float degrees, float offset) {
        if(!isJoystickTurning()) {
            activateMainController(false);//Pause the controller while using the joystick
            activateAuxController(false);

            if (offset == 0) stop();
            else moveForward(degrees, offset);
        }
        else setJsValues(degrees, offset);
    }

    public void onJsUp() {
        if(!isJoystickTurning()) {
            activateMainController(true);//Pause the controller while using the joystick
            activateAuxController(true);
            stop();
        }
        setJsValues(0, 0);
    }
}

class FragmentDataMap {
    private HashMap<String, String> dataMap;
    public FragmentDataMap() {
        dataMap = new HashMap<>();
    }
    public void addData(String key, String value) {
        dataMap.put(key, value);
    }
    public String getData(String key) {
        return dataMap.get(key);
    }
    public void clearData() {
        dataMap.clear();
    }
}