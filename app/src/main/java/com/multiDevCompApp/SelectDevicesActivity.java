package com.multiDevCompApp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.multiDevCompApp.drivers.interfaces.Controller;


/**
 * Created by sserr on 23/01/2018.
 */

public class SelectDevicesActivity extends Activity implements View.OnClickListener {

    private ArrayAdapter<String> controllerSpinnerAdapter;
    private ArrayAdapter<String> robotSpinnerAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_devices_activity);

        controllerSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for(ControllerType ct : ControllerType.values()) {
            controllerSpinnerAdapter.add(ct.toString());
        }

        robotSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for(RobotType rt : RobotType.values()) {
            robotSpinnerAdapter.add(rt.toString());
        }

        Spinner spinnerController = (Spinner)findViewById(R.id.ControllerName);
        Spinner robotController = (Spinner)findViewById(R.id.RobotName);

        spinnerController.setAdapter(controllerSpinnerAdapter);
        robotController.setAdapter(robotSpinnerAdapter);

        Button confirmBtn = (Button) findViewById((R.id.confirmBtn));
        confirmBtn.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirmBtn:

                Spinner ctrlSpinner = (Spinner) findViewById(R.id.ControllerName);
                Spinner robotSpinner = (Spinner) findViewById((R.id.RobotName));

                Intent i = new Intent(this, MainActivity.class);
                i.putExtra("ctrlName", ControllerType.valueOf((String)ctrlSpinner.getSelectedItem()));
                i.putExtra("robotName", RobotType.valueOf((String)robotSpinner.getSelectedItem()));
                startActivity(i);
                break;
            default:
                break;
        }
    }
}
