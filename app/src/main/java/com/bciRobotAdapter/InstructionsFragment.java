package com.bciRobotAdapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class InstructionsFragment extends Fragment {
    private View view;
    private MainActivity mainActivity;
    private static final String startHtml = "<![CDATA[";
    private static final String endHtml = "]]>";

    private TextView moveForwardInst, moveBackwardInst, moveLeftInst, moveRightInst, turnLeftInst, turnRightInst, ledInst;
    private TextView mainControllerConnected, auxControllerConnected, robotConnected;

    public InstructionsFragment() {}
    //todo: controllare se mantiene formattazione html
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_instructions, container, false);
        getTextViews();

        if(mainActivity.getFragmentData("mainControllerConnected")!=null) mainControllerConnected.setText(mainActivity.getFragmentData("mainControllerConnected"));
        if(mainActivity.getFragmentData("auxControllerConnected")!=null) auxControllerConnected.setText(mainActivity.getFragmentData("auxControllerConnected"));
        if(mainActivity.getFragmentData("robotConnected")!=null) robotConnected.setText(mainActivity.getFragmentData("robotConnected"));

        if(mainActivity.getFragmentData("moveForwardInst")!=null) setMoveForwardInst(mainActivity.getFragmentData("moveForwardInst"));
        if(mainActivity.getFragmentData("moveBackwardInst")!=null) setMoveBackwardInst(mainActivity.getFragmentData("moveBackwardInst"));
        if(mainActivity.getFragmentData("moveLeftInst")!=null) setMoveLeftInst(mainActivity.getFragmentData("moveLeftInst"));
        if(mainActivity.getFragmentData("moveRightInst")!=null) setMoveRightInst(mainActivity.getFragmentData("moveRightInst"));
        if(mainActivity.getFragmentData("turnLeftInst")!=null) setTurnLeftInst(mainActivity.getFragmentData("turnLeftInst"));
        if(mainActivity.getFragmentData("turnRightInst")!=null) setTurnRightInst(mainActivity.getFragmentData("turnRightInst"));
        if(mainActivity.getFragmentData("ledInst")!=null) setLedInst(mainActivity.getFragmentData("ledInst"));
        return view;
    }

    public void initFragment(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void getTextViews() {
        moveForwardInst = (TextView) view.findViewById(R.id.moveForwardInst);
        moveBackwardInst = (TextView) view.findViewById(R.id.moveBackwardInst);
        moveLeftInst = (TextView) view.findViewById(R.id.moveLeftInst);
        moveRightInst = (TextView) view.findViewById(R.id.moveRightInst);
        turnLeftInst = (TextView) view.findViewById(R.id.turnLeftInst);
        turnRightInst = (TextView) view.findViewById(R.id.turnRightInst);
        ledInst = (TextView) view.findViewById(R.id.ledInst);

        mainControllerConnected = (TextView) view.findViewById(R.id.mainControllerConnected);
        auxControllerConnected = (TextView) view.findViewById(R.id.auxControllerConnected);
        robotConnected = (TextView) view.findViewById(R.id.robotConnected);
    }

    public void setMoveForwardInst(String inst) {
        String s = startHtml + "<b>" + getString(R.string.move_forward_inst) + " </b>" + inst + endHtml;
        moveForwardInst.setText(Html.fromHtml(s));
    }
    public void setMoveBackwardInst(String inst) {
        String s = startHtml + "<b>" + getString(R.string.move_backward_inst) + " </b>" + inst + endHtml;
        moveBackwardInst.setText(Html.fromHtml(s));
    }
    public void setMoveLeftInst(String inst) {
        String s = startHtml + "<b>" + getString(R.string.move_left_inst) + " </b>" + inst + endHtml;
        moveLeftInst.setText(Html.fromHtml(s));
    }
    public void setMoveRightInst(String inst) {
        String s = startHtml + "<b>" + getString(R.string.move_right_inst) + " </b>" + inst + endHtml;
        moveRightInst.setText(Html.fromHtml(s));
    }
    public void setTurnLeftInst(String inst) {
        String s = startHtml + "<b>" + getString(R.string.turn_left_inst) + " </b>" + inst + endHtml;
        turnLeftInst.setText(Html.fromHtml(s));
    }
    public void setTurnRightInst(String inst) {
        String s = startHtml + "<b>" + getString(R.string.turn_right_inst) + " </b>" + inst + endHtml;
        turnRightInst.setText(Html.fromHtml(s));
    }
    public void setLedInst(String inst) {
        String s = startHtml + "<b>" + getString(R.string.led_inst) + " </b>" + inst + endHtml;
        ledInst.setText(Html.fromHtml(s));
    }
}
