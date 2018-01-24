package com.multiDevCompApp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


/**
 * Created by sserr on 23/01/2018.
 */

public class SelectDevicesActivity extends Activity {

    private ArrayAdapter<String> controllerSpinnerAdapter;
    private ArrayAdapter<String> robotSpinnerAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_devices_activity);

        controllerSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        for(ControllerType ct : ControllerType.values()) {
            controllerSpinnerAdapter.add(ct.toString());
        }

        robotSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        for(RobotType rt : RobotType.values()) {
            robotSpinnerAdapter.add(rt.toString());
        }

        Spinner spinnerController = (Spinner)findViewById(R.id.ControllerName);
        Spinner robotController = (Spinner)findViewById(R.id.RobotName);

        spinnerController.setAdapter(controllerSpinnerAdapter);
        robotController.setAdapter(robotSpinnerAdapter);

    }
}
