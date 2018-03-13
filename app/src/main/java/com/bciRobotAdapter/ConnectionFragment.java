package com.bciRobotAdapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.bciRobotAdapter.devicesTypes.ControllerType;
import com.bciRobotAdapter.devicesTypes.RobotType;

public class ConnectionFragment extends Fragment implements View.OnClickListener {
    MainActivity mainActivity;
    View view;
    Button connectMainCtrlBtn, connectAuxCtrlBtn, connectRobotBtn;
    Spinner spinnerMainCtrl, spinnerAuxCtrl, spinnerRobot;
    TextView cMainLog, rLog, cAuxLog;
    TextView cMainOutput, rOutput, cAuxOutput;

    public ConnectionFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_connection, container, false);
        getUiElements();
        initSpinners();
        addButtonsListener();
        if(mainActivity.getFragmentData("mainControllerLog")!=null) cMainLog.setText(mainActivity.getFragmentData("mainControllerLog"));
        if(mainActivity.getFragmentData("auxControllerLog")!=null) cAuxLog.setText(mainActivity.getFragmentData("auxControllerLog"));
        if(mainActivity.getFragmentData("robotLog")!=null) rLog.setText(mainActivity.getFragmentData("robotLog"));

        if(mainActivity.getFragmentData("mainControllerOutput")!=null) cMainOutput.setText(mainActivity.getFragmentData("mainControllerOutput"));
        if(mainActivity.getFragmentData("auxControllerOutput")!=null) cAuxOutput.setText(mainActivity.getFragmentData("auxControllerOutput"));
        if(mainActivity.getFragmentData("robotOutput")!=null) rOutput.setText(mainActivity.getFragmentData("robotOutput"));

        if(mainActivity.getFragmentData("connectMainCtrlBtn")!=null) connectMainCtrlBtn.setText(mainActivity.getFragmentData("connectMainCtrlBtn"));
        if(mainActivity.getFragmentData("connectAuxCtrlBtn")!=null) connectAuxCtrlBtn.setText(mainActivity.getFragmentData("connectAuxCtrlBtn"));
        if(mainActivity.getFragmentData("connectRobotBtn")!=null) connectRobotBtn.setText(mainActivity.getFragmentData("connectRobotBtn"));

        return view;
    }

    @Override
    public void onPause() {
        mainActivity.addFragmentData("mainControllerLog", (String)cMainLog.getText());
        mainActivity.addFragmentData("auxControllerLog", (String)cAuxLog.getText());
        mainActivity.addFragmentData("robotLog", (String)rLog.getText());

        mainActivity.addFragmentData("mainControllerOutput", cMainOutput.getText().toString());
        mainActivity.addFragmentData("auxControllerOutput", cAuxOutput.getText().toString());
        mainActivity.addFragmentData("robotOutput", rOutput.getText().toString());

        mainActivity.addFragmentData("connectMainCtrlBtn", (String)connectMainCtrlBtn.getText());
        mainActivity.addFragmentData("connectAuxCtrlBtn", (String)connectAuxCtrlBtn.getText());
        mainActivity.addFragmentData("connectRobotBtn", (String)connectRobotBtn.getText());
        super.onPause();
    }

    public void initFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private void getUiElements() {
        connectMainCtrlBtn = (Button) view.findViewById(R.id.connectMainCtrl);
        connectRobotBtn = (Button) view.findViewById(R.id.connectRobot);
        connectAuxCtrlBtn = (Button) view.findViewById(R.id.connectAuxCtrl);
        spinnerMainCtrl = (Spinner) view.findViewById(R.id.spinnerCtrl);
        spinnerRobot = (Spinner) view.findViewById(R.id.spinnerRobot);
        spinnerAuxCtrl = (Spinner) view.findViewById(R.id.spinnerAuxCtrl);
        cMainLog = (TextView) view.findViewById(R.id.mainControllerLog);
        rLog = (TextView) view.findViewById(R.id.robotLog);
        cAuxLog = (TextView) view.findViewById(R.id.auxControllerLog);
        cMainOutput = (TextView) view.findViewById(R.id.mainControllerOutput);
        cAuxOutput = (TextView) view.findViewById(R.id.auxControllerOutput);
        rOutput = (TextView) view.findViewById(R.id.robotOutput);
    }

    private void addButtonsListener() {
        connectMainCtrlBtn.setOnClickListener(this);
        connectRobotBtn.setOnClickListener(this);
        connectAuxCtrlBtn.setOnClickListener(this);
    }
    private void initSpinners() {
        //Spinners values are from the ControllerType and RobotType enum
        ArrayAdapter<String> spinnerAdapterCtrl = new ArrayAdapter<>(mainActivity.getContext(), R.layout.spinner);
        for(ControllerType ct : ControllerType.values()) {
            spinnerAdapterCtrl.add(ct.toString());
        }
        spinnerMainCtrl.setAdapter(spinnerAdapterCtrl);

        ArrayAdapter<String> spinnerAdapterRobot = new ArrayAdapter<>(mainActivity.getContext(), R.layout.spinner);
        for(RobotType rt : RobotType.values()) {
            spinnerAdapterRobot.add(rt.toString());
        }
        spinnerRobot.setAdapter(spinnerAdapterRobot);

        ArrayAdapter<String> spinnerAdapterAuxCtrl = new ArrayAdapter<>(mainActivity.getContext(), R.layout.spinner);
        for(ControllerType ct : ControllerType.values()) {
            spinnerAdapterAuxCtrl.add(ct.toString());
        }
        spinnerAuxCtrl.setAdapter(spinnerAdapterAuxCtrl);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            //The connect buttons have multiple functions according to the context: Connect, Stop or Disconnect.
            //Connect: starts searching for the selected device and connects to the first available
            //Stop: interrupts the connect's task and stops searching
            //Disconnect: disconnects from the connected device

            case R.id.connectMainCtrl:
                String textCtrl = (String) connectMainCtrlBtn.getText();

                if (textCtrl.equals(getString(R.string.connect))) {
                    mainActivity.onConnectMainControllerPressed(ControllerType.valueOf((String) spinnerMainCtrl.getSelectedItem()));
                    connectMainCtrlBtn.setText(R.string.stop);
                    setMainControllerLog("Looking for " + spinnerMainCtrl.getSelectedItem() + ", press stop to abort");
                }
                else if (textCtrl.equals(getString(R.string.stop))) {
                    mainActivity.onStopMainControllerConnectionPressed();
                    setMainControllerLog("Search stopped");
                    connectMainCtrlBtn.setText(R.string.connect);
                }
                else if (textCtrl.equals(getString(R.string.disconnect))) {
                    mainActivity.onDisconnectMainControllerPressed();
                    connectMainCtrlBtn.setText(R.string.connect);
                    setMainControllerLog(getString(R.string.notConnected));
                }
                break;

            case R.id.connectRobot:
                String textRobot = (String) connectRobotBtn.getText();

                if (textRobot.equals(getString(R.string.connect))) {
                    mainActivity.onConnectRobotPressed(RobotType.valueOf((String) spinnerRobot.getSelectedItem()));
                    connectRobotBtn.setText(R.string.stop);
                    setRobotLog("Looking for " + spinnerRobot.getSelectedItem() + ", press stop to abort");
                }
                else if (textRobot.equals(getString(R.string.stop))) {
                    mainActivity.onStopRobotConnectionPressed();
                    connectRobotBtn.setText(R.string.connect);
                    setRobotLog("Search stopped");
                }
                else if (textRobot.equals(getString(R.string.disconnect))) {
                    mainActivity.onDisconnectRobotPressed();
                    connectRobotBtn.setText(R.string.connect);
                    setRobotLog(getString(R.string.notConnected));
                }
                break;

            case R.id.connectAuxCtrl:
                String textAuxCtrl = (String) connectAuxCtrlBtn.getText();

                if (textAuxCtrl.equals(getString(R.string.connect))) {
                    mainActivity.onConnectAuxControllerPressed(ControllerType.valueOf((String) spinnerAuxCtrl.getSelectedItem()));
                    connectAuxCtrlBtn.setText(R.string.stop);
                    setAuxControllerLog("Looking for " + spinnerAuxCtrl.getSelectedItem() + ", press stop to abort");
                }
                else if (textAuxCtrl.equals(getString(R.string.stop))) {
                    mainActivity.onStopAuxControllerConnectionPressed();
                    setAuxControllerLog("Search stopped");
                    connectAuxCtrlBtn.setText(R.string.connect);
                }
                else if (textAuxCtrl.equals(getString(R.string.disconnect))) {
                    mainActivity.onDisconnectAuxControllerPressed();
                    connectAuxCtrlBtn.setText(R.string.connect);
                    setAuxControllerLog(getString(R.string.notConnected));
                }
                break;
        }
    }

    public void onMainControllerConnected(boolean connected, String name) {
        if(connected) {
            setMainControllerLog("Connected to: " + name);
            connectMainCtrlBtn.setText(R.string.disconnect);
        }
        else {
            setMainControllerLog("Disconnected");
            connectMainCtrlBtn.setText(R.string.connect);
        }
    }
    public void onAuxControllerConnected(boolean connected, String name) {
        if(connected) {
            setAuxControllerLog("Connected to: " + name);
            connectAuxCtrlBtn.setText(R.string.disconnect);
        }
        else {
            setAuxControllerLog("Disconnected");
            connectAuxCtrlBtn.setText(R.string.connect);
        }
    }
    public void onRobotConnected(boolean connected, String name) {
        if(connected) {
            setRobotLog("Connected to: " + name);
            connectRobotBtn.setText(R.string.disconnect);
        }
        else {
            setRobotLog("Disconnected");
            connectRobotBtn.setText(R.string.connect);
        }
    }

    public void setMainControllerLog(String toWrite) {
        cMainLog.setText(toWrite);
    }
    public void setAuxControllerLog(String toWrite) {
        cAuxLog.setText(toWrite);
    }
    public void setRobotLog(String toWrite) {
        rLog.setText(toWrite);
    }

    public void setMainControllerOutput(String toWrite) {
        toWrite = toWrite+"\n";
        cMainOutput.append(toWrite);
    }
    public void setAuxControllerOutput(String toWrite) {
        toWrite = toWrite+"\n";
        cAuxOutput.append(toWrite);
    }
    public void setRobotOutput (String toWrite) {
        toWrite = toWrite+"\n";
        rOutput.append(toWrite);
    }
}
