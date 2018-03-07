package com.bciRobotAdapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.bciRobotAdapter.devicesTypes.ControllerType;
import com.bciRobotAdapter.devicesTypes.RobotType;
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
        if(mainActivity.getFragmentData("mainControllerConnected")!=null) mainControllerConnected.setText(mainActivity.getFragmentData("mainControllerConnected"));
        if(mainActivity.getFragmentData("auxControllerConnected")!=null) auxControllerConnected.setText(mainActivity.getFragmentData("auxControllerConnected"));
        if(mainActivity.getFragmentData("robotConnected")!=null) robotConnected.setText(mainActivity.getFragmentData("robotConnected"));

        if(mainActivity.getFragmentData("driveLog")!=null) driveLog.setText(mainActivity.getFragmentData("driveLog"));
        if(mainActivity.getFragmentData("jsToTurnSwitch")!=null) jsToTurnSwitch.setChecked(Boolean.valueOf(mainActivity.getFragmentData("jsToTurnSwitch")));
        if(mainActivity.getFragmentData("btnPauseText")!=null) btnPause.setText(mainActivity.getFragmentData("btnPauseText"));

        if (mainActivity.isJoystickTurning()) joystick.setMotionConstraint(Joystick.MotionConstraint.HORIZONTAL);
        else joystick.setMotionConstraint(Joystick.MotionConstraint.NONE);
        return view;
    }

    @Override
    public void onPause() {
        mainActivity.addFragmentData("mainControllerConnected", (String)mainControllerConnected.getText());
        mainActivity.addFragmentData("auxControllerConnected", (String)auxControllerConnected.getText());
        mainActivity.addFragmentData("robotConnected", (String)robotConnected.getText());

        mainActivity.addFragmentData("driveLog", (String)driveLog.getText());
        mainActivity.addFragmentData("jsToTurnSwitch", Boolean.toString(jsToTurnSwitch.isChecked()));
        mainActivity.addFragmentData("btnPauseText", (String)btnPause.getText());
        super.onPause();
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

    public void setDriveLog(String toWrite) {
        driveLog.setText(toWrite);
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