package com.bciRobotAdapter.drivers.controllerDrivers;

import com.bciRobotAdapter.AdapterActivity;
import com.bciRobotAdapter.drivers.abstractDrivers.AbstractController;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import com.neurosky.AlgoSdk.NskAlgoDataType;
import com.neurosky.AlgoSdk.NskAlgoSdk;
import com.neurosky.AlgoSdk.NskAlgoSignalQuality;
import com.neurosky.AlgoSdk.NskAlgoType;
import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

public class MindwaveDriver extends AbstractController {

    private int algoTypes = NskAlgoType.NSK_ALGO_TYPE_ATT.value;
    private NskAlgoSdk nskAlgoSdk;
    public TgStreamReader tgStreamReader;
    private boolean bInited = false;
    private TgStreamHandler mindwaveListener;


    public MindwaveDriver(AdapterActivity adapterActivity) {
        super(adapterActivity);
        nskAlgoSdk = new NskAlgoSdk();
    }

    @Override
    public void searchAndConnect(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mindwaveListener = new MindwaveListener(this);
        tgStreamReader = new TgStreamReader(mBluetoothAdapter,mindwaveListener);

        if(tgStreamReader.isBTConnected()) {
            tgStreamReader.stop();
            tgStreamReader.close();
        }

        tgStreamReader.connect();

        nskAlgoSdk.setOnSignalQualityListener(new NskAlgoSdk.OnSignalQualityListener() {
            @Override
            public void onSignalQuality(int level) {
                setControllerLog("Signal quality: "+level);
            }
        });

        nskAlgoSdk.setOnAttAlgoIndexListener(new NskAlgoSdk.OnAttAlgoIndexListener() {
            @Override
            public void onAttAlgoIndex(int value) {
                setGeneralLog("value: "+value);

                final int midValue = 65;
                final int highValue = 90;

                if(value<midValue){
                }
                else if(value>=midValue && value<highValue){
                }
                else if(value >=highValue){
                }
            }
        });

        //mindwaveStart();
    }

    public void setAlgos() {
        algoTypes += NskAlgoType.NSK_ALGO_TYPE_ATT.value;
        if (bInited) {
            NskAlgoSdk.NskAlgoUninit();
            bInited = false;
        }
        int ret = NskAlgoSdk.NskAlgoInit(algoTypes, getAdapterActivity().getFilesDir().getAbsolutePath());
        if (ret == 0) {
            bInited = true;
        }
    }

    void mindwaveStart(){
        nskAlgoSdk.setOnSignalQualityListener(new NskAlgoSdk.OnSignalQualityListener() {
            @Override
            public void onSignalQuality(int level) {
                if(NskAlgoSignalQuality.values()[level].toString().equals("POOR") || NskAlgoSignalQuality.values()[level].toString().equals("NOT DETECTED")){
                    setControllerLog("Segnale perso");
                }
            }
        });
        NskAlgoSdk.NskAlgoStart(false);
    }


    public void getDataTimeOut(){
        if (tgStreamReader != null && tgStreamReader.isBTConnected()) {
            tgStreamReader.stop();
            tgStreamReader.close();
        }
    }

    @Override
    public void disconnect() {
        //tgStreamReader.stop();
        //tgStreamReader.close();
    }

    @Override
    public void stopSearching() {
        if(tgStreamReader!=null)tgStreamReader.stop();
    }
}

class MindwaveListener implements TgStreamHandler {

    MindwaveDriver mindwaveDriver;

    public MindwaveListener(MindwaveDriver mindwaveDriver) {
        this.mindwaveDriver = mindwaveDriver;
    }

    @Override
    public void onDataReceived(int datatype, int data, Object obj) {
        switch (datatype) {
            case MindDataType.CODE_ATTENTION:
                short attValue[] = {(short)data};
                NskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_ATT.value, attValue, 1);
                break;
            case MindDataType.CODE_POOR_SIGNAL:
                short pqValue[] = {(short)data};
                NskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_PQ.value, pqValue, 1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onStatesChanged(int connectionStates) {
        Log.d("Connection", "connectionStates change to: " + connectionStates);
        switch (connectionStates) {
            case ConnectionStates.STATE_CONNECTING:
                System.out.println("MindWave: Connecting...");
                break;

            case ConnectionStates.STATE_CONNECTED:
                System.out.println("MindWave: Connected!");
                mindwaveDriver.notifyControllerConnected(true);
                //mindwaveDriver.tgStreamReader.start();
                //mindwaveDriver.mindwaveStart();
                break;

            case ConnectionStates.STATE_WORKING:
                mindwaveDriver.setAlgos();
                break;

            case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                mindwaveDriver.getDataTimeOut();
                break;

            case ConnectionStates.STATE_STOPPED:
                break;

            case ConnectionStates.STATE_DISCONNECTED:
                NskAlgoSdk.NskAlgoUninit();
                mindwaveDriver.notifyControllerConnected(false);
                break;

            case ConnectionStates.STATE_ERROR:
                mindwaveDriver.notifyControllerConnected(false);
                break;

            case ConnectionStates.STATE_FAILED: //Quando è spento
                mindwaveDriver.notifyControllerConnected(false);
                break;
        }
    }

    @Override
    public void onChecksumFail(byte[] bytes, int i, int i1) {

    }

    @Override
    public void onRecordFail(int i) {

    }
}
