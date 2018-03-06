package com.bciRobotAdapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.bciRobotAdapter.joystickLib.Joystick;
import com.bciRobotAdapter.joystickLib.JoystickListener;

public class JoystickFragment extends Fragment implements View.OnClickListener, JoystickListener {

    MainActivity mainActivity;
    View view;

    private Joystick joystick;

    private Button btnL, btnR, btnPause;
    private Switch jsToTurnSwitch;

    private TextView driveLog, mainControllerConnected, auxControllerConnected, robotConnected;

    public JoystickFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_joystick, container, false);
        getUiElements();
        initJoystick();
        addButtonsListener();
        return view;
    }

    public void initFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private void initJoystick() {
        joystick = (Joystick) view.findViewById(R.id.joystick);
        joystick.setJoystickListener(this);
    }

    private void addButtonsListener() {
        btnL.setOnClickListener(this);
        btnR.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        jsToTurnSwitch.setOnClickListener(this);
    }

    private void getUiElements() {
        btnL = (Button)view.findViewById(R.id.btnL);
        btnR = (Button)view.findViewById(R.id.btnR);
        btnPause = (Button) view.findViewById(R.id.btnPause);
        jsToTurnSwitch = (Switch) view.findViewById(R.id.joystickToTurn);

        driveLog = (TextView) view.findViewById(R.id.driveLog);
        mainControllerConnected = (TextView) view.findViewById(R.id.mainControllerConnected);
        auxControllerConnected = (TextView) view.findViewById(R.id.auxControllerConnected);
        robotConnected = (TextView) view.findViewById(R.id.robotConnected);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //Left button clicked
            case R.id.btnL:
                mainActivity.turnL(90);//Turn the robot 90 degrees left
                break;

            //Right button clicked
            case R.id.btnR:
                mainActivity.turnR(90);//Turn the robot 90 degrees right
                break;

            case R.id.btnPause:
                mainActivity.stop();
                String btnString = (String)btnPause.getText();
                if(btnString.equals(getString(R.string.pauseControllers))) {
                    mainActivity.activateMainController(false);
                    mainActivity.activateAuxController(false);
                    btnPause.setText(R.string.resumeControllers);
                }
                else if (btnString.equals(getString(R.string.resumeControllers))) {
                    mainActivity.activateMainController(true);
                    mainActivity.activateAuxController(true);
                    btnPause.setText(R.string.pauseControllers);
                }
                break;

            case R.id.joystickToTurn:
                mainActivity.setJoystickTurning(jsToTurnSwitch.isChecked());
                if (mainActivity.isJoystickTurning()) {
                    joystick.setMotionConstraint(Joystick.MotionConstraint.HORIZONTAL);
                } else joystick.setMotionConstraint(Joystick.MotionConstraint.NONE);
                break;

            default:
                break;
        }
    }

    public void driveLog(String toWrite) {
        driveLog.setText(toWrite);
    }
    public void setConnectedDevices(String mainController, String auxController, String robot) {
        if(mainController!=null) mainControllerConnected.setText(mainController);
        else mainControllerConnected.setText("Not Connected");

        if(auxController!=null) auxControllerConnected.setText(auxController);
        else auxControllerConnected.setText("Not Connected");

        if(robot!=null) robotConnected.setText(robot);
        else robotConnected.setText("Not Connected");
    }

    @Override
    public void onDown() {
        mainActivity.onJsDown();
    }

    @Override
    public void onDrag(float degrees, float offset) {
        mainActivity.onJsDrag(degrees, offset);
    }

    //When your finger goes back up from the joystick
    @Override
    public void onUp() {
        mainActivity.onJsUp();
    }
}