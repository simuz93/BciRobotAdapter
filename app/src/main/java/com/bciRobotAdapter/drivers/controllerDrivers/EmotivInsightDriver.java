package com.bciRobotAdapter.drivers.controllerDrivers;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bciRobotAdapter.AdapterActivity;
import com.bciRobotAdapter.drivers.abstractDrivers.AbstractController;
import com.emotiv.bluetooth.Emotiv;
import com.emotiv.sdk.IEE_DataChannel_t;
import com.emotiv.sdk.IEE_Event_t;
import com.emotiv.sdk.IEE_MentalCommandAction_t;
import com.emotiv.sdk.IEE_MentalCommandEvent_t;
import com.emotiv.sdk.IEE_MentalCommandTrainingControl_t;
import com.emotiv.sdk.SWIGTYPE_p_double;
import com.emotiv.sdk.SWIGTYPE_p_unsigned_int;
import com.emotiv.sdk.SWIGTYPE_p_unsigned_long;
import com.emotiv.sdk.SWIGTYPE_p_void;
import com.emotiv.sdk.edkJava;
import com.emotiv.sdk.edkJavaConstants;
import com.emotiv.sdk.edkJavaJNI;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class EmotivInsightDriver extends AbstractController{

    IEE_DataChannel_t[] Channel_list;
    String[] Name_Channel = {"AF3","T7","Pz","T8","AF4"};

    //public static Context context;
    //public static EmotivInsightDriver engineConnectInstance;
    private Timer timer;
    private TimerTask timerTask;

    public boolean isConnected = false;
    public boolean bleInUse = false;
    private int state;
    private int userId=-1;

    private SWIGTYPE_p_void handleEvent;
    private SWIGTYPE_p_void emoState;

    private boolean b = false;

    /* ============================================ */
    protected static final int TYPE_USER_ADD = 16;
    protected static final int TYPE_USER_REMOVE = 32;
    protected static final int TYPE_EMOSTATE_UPDATE = 64;
    protected static final int TYPE_METACOMMAND_EVENT = 256;
    /* ============================================ */
    protected static final int HANDLER_TRAIN_STARTED = 1;
    protected static final int HANDLER_TRAIN_SUCCEED = 2;
    protected static final int HANDLER_TRAIN_FAILED = 3;
    protected static final int HANDLER_TRAIN_COMPLETED = 4;
    protected static final int HANDLER_TRAIN_ERASED = 5;
    protected static final int HANDLER_TRAIN_REJECTED = 6;
    protected static final int HANDLER_ACTION_CURRENT = 7;
    protected static final int HANDLER_USER_ADD = 8;
    protected static final int HANDLER_USER_REMOVE = 9;
    protected static final int HANDLER_TRAINED_RESET = 10;

    public EngineInterface delegate;

    /*public static void setContext(Context context) {
        EmotivInsightDriver.context = context;
    }*/

    public EmotivInsightDriver(AdapterActivity adapterActivity) {
        super(adapterActivity);
        connectEngine();
    }


    private void connectEngine(){
        Emotiv.IEE_EmoInitDevice(getContext());
        edkJava.IEE_EngineConnect("Emotiv Systems-5");
        handleEvent = edkJava.IEE_EmoEngineEventCreate();
        emoState = edkJava.IEE_EmoStateCreate();
        IEE_DataChannel_t[] ChannelListTmp = {IEE_DataChannel_t.IED_AF3, IEE_DataChannel_t.IED_T7,IEE_DataChannel_t.IED_Pz,
                IEE_DataChannel_t.IED_T8,IEE_DataChannel_t.IED_AF4};

        Channel_list = ChannelListTmp;

        timer = new Timer();
        intTimerTask();
        timer.schedule(timerTask , 0, 10);
    }

    /*
    public void enableMentalcommandActions(IEE_MentalCommandAction_t _MetalcommandAction) {
        //long MetaCommandActions;
        SWIGTYPE_p_unsigned_long uActiveAction = edkJava.new_ulong_p();
        int result = edkJava.IEE_MentalCommandGetActiveActions(userId, uActiveAction);
        if (result == edkJava.EDK_OK) {
            long _currentActiveActions = edkJava.ulong_p_value(uActiveAction);
            long y = _currentActiveActions & _MetalcommandAction.swigValue();
            if (y == 0) {
                long MetaCommandActions;
                MetaCommandActions = _currentActiveActions | _MetalcommandAction.swigValue();
                edkJava.IEE_MentalCommandSetActiveActions(userId, MetaCommandActions);
            }
        }
        edkJava.delete_ulong_p(uActiveAction);
    }
    */

    /*
    public boolean checkTrained(IEE_MentalCommandAction_t action) {
        boolean res = false;
        SWIGTYPE_p_unsigned_long uSignAction = edkJava.new_ulong_p();
        int result = edkJava.IEE_MentalCommandGetTrainedSignatureActions(userId, uSignAction);
        if (result == edkJava.EDK_OK) {
            long _currentActiveActions = edkJava.ulong_p_value(uSignAction);
            long y = _currentActiveActions & action.swigValue();
            return (y == action.swigValue());
        }
        return res;
    }
*/
    /*
    public void trainningClear(IEE_MentalCommandAction_t _MetalcommandAction) {
        edkJava.IEE_MentalCommandSetTrainingAction(userId, _MetalcommandAction);
        if (edkJava.IEE_MentalCommandSetTrainingControl(userId,
                IEE_MentalCommandTrainingControl_t.MC_ERASE) == edkJava.EDK_OK) {
        }
    }
*/
    /*
    public boolean startTrainingMetalcommand(Boolean isTrain, IEE_MentalCommandAction_t MetaCommandAction) {
        if (!isTrain) {
            if (edkJava.IEE_MentalCommandSetTrainingAction(userId, MetaCommandAction) == edkJava.EDK_OK) {
                if (edkJava.IEE_MentalCommandSetTrainingControl(userId,
                        IEE_MentalCommandTrainingControl_t.MC_START) == edkJava.EDK_OK) {
                    return true;
                }
            }
        } else {
            if (edkJava.IEE_MentalCommandSetTrainingControl(userId, IEE_MentalCommandTrainingControl_t.MC_RESET) == edkJava.EDK_OK) {
                return false;
            }
        }
        return false;
    }*/
/*
    public void setTrainControl(IEE_MentalCommandTrainingControl_t type) {
        if (edkJava.IEE_MentalCommandSetTrainingControl(userId, type) == edkJava.EDK_OK) {
        }

    }*/

    public int getEventEngineId(SWIGTYPE_p_void hEvent)
    {
        SWIGTYPE_p_unsigned_int pEngineId = edkJava.new_uint_p();
        int result = edkJava.IEE_EmoEngineEventGetUserId(handleEvent, pEngineId);
        int tmpUserId = (int)edkJava.uint_p_value(pEngineId);
        edkJava.delete_uint_p(pEngineId);
        return tmpUserId;
    }

    public void intTimerTask() {
        if (timerTask != null)
            return;
        timerTask = new TimerTask() {

            @Override
            public void run() {
                int numberDevice=Emotiv.IEE_GetInsightDeviceCount();
                if (numberDevice != 0){
                    if (!bleInUse) {
                        bleInUse = true;
                        Emotiv.IEE_ConnectInsightDevice(0);
                        Log.d("EMOTIV", "CONNESSO");
                        b = true;
                    }
                }
                else {
                    numberDevice = Emotiv.IEE_GetEpocPlusDeviceCount();
                    if (numberDevice != 0){
                        if (!bleInUse) {
                            bleInUse = true;
                            Emotiv.IEE_ConnectEpocPlusDevice(0, false);
                        }
                    } else {
                        bleInUse = false;
                    }
                }
                state = edkJava.IEE_EngineGetNextEvent(handleEvent);
                if (state == edkJava.EDK_OK) {

                    IEE_Event_t eventType = edkJava.IEE_EmoEngineEventGetType(handleEvent);
                    int tmpUserId = getEventEngineId(handleEvent);
                    switch (eventType) {
                        case IEE_UserAdded:
                            Log.e("connect", "User Added");
                            isConnected = true;
                            bleInUse = true;
                            userId = tmpUserId;
                            Log.d("ID", ""+userId);
                            hander.sendEmptyMessage(HANDLER_USER_ADD);
                            break;
                        case IEE_UserRemoved:
                            Log.e("disconnect", "User Removed");
                            isConnected = false;
                            bleInUse = false;
                            userId=-1;
                            hander.sendEmptyMessage(HANDLER_USER_REMOVE);
                            break;
                        case IEE_EmoStateUpdated:
                            if (!isConnected)
                                break;
                            edkJava.IEE_EmoEngineEventGetEmoState(handleEvent, emoState);
                            Log.e("MentalCommand", "EmoStateUpdated");
                            hander.sendMessage(hander.obtainMessage(HANDLER_ACTION_CURRENT));

                            for(int i=0; i < Channel_list.length; i++) {
                                SWIGTYPE_p_double ptheta = edkJava.new_double_p();
                                SWIGTYPE_p_double palpha = edkJava.new_double_p();
                                SWIGTYPE_p_double plow_beta = edkJava.new_double_p();
                                SWIGTYPE_p_double phigh_beta = edkJava.new_double_p();
                                SWIGTYPE_p_double pgamma = edkJava.new_double_p();
                                int result = -1;
                                result = edkJava.IEE_GetAverageBandPowers(userId, Channel_list[i], ptheta, palpha, plow_beta, phigh_beta, pgamma);


                                System.out.println(edkJavaJNI.EDK_UNKNOWN_ERROR_get());
                                System.out.println(edkJavaJNI.EDK_INVALID_DEV_ID_ERROR_get());
                                System.out.println(edkJavaJNI.EDK_INVALID_PROFILE_ARCHIVE_get());
                                System.out.println(edkJavaJNI.EDK_NO_USER_FOR_BASEPROFILE_get());
                                System.out.println(edkJavaJNI.EDK_CANNOT_ACQUIRE_DATA_get());
                                System.out.println(edkJavaJNI.EDK_BUFFER_TOO_SMALL_get());
                                System.out.println(edkJavaJNI.EDK_OUT_OF_RANGE_get());
                                System.out.println(edkJavaJNI.EDK_INVALID_PARAMETER_get());
                                System.out.println(edkJavaJNI.EDK_PARAMETER_LOCKED_get());
                                System.out.println(edkJavaJNI.EDK_MC_INVALID_TRAINING_ACTION_get());
                                System.out.println(edkJavaJNI.EDK_MC_INVALID_TRAINING_CONTROL_get());
                                System.out.println(edkJavaJNI.EDK_MC_INVALID_ACTIVE_ACTION_get());
                                System.out.println(edkJavaJNI.EDK_MC_EXCESS_MAX_ACTIONS_get());
                                System.out.println(edkJavaJNI.EDK_FE_NO_SIG_AVAILABLE_get());
                                System.out.println(edkJavaJNI.EDK_FILESYSTEM_ERROR_get());
                                System.out.println(edkJavaJNI.EDK_INVALID_USER_ID_get());
                                System.out.println(edkJavaJNI.EDK_EMOENGINE_UNINITIALIZED_get());
                                System.out.println(edkJavaJNI.EDK_EMOENGINE_DISCONNECTED_get());
                                System.out.println(edkJavaJNI.EDK_EMOENGINE_PROXY_ERROR_get());
                                System.out.println(edkJavaJNI.EDK_STREAM_UNINITIALIZED_get());
                                System.out.println(edkJavaJNI.EDK_FILESTREAM_ERROR_get());
                                System.out.println(edkJavaJNI.EDK_STREAM_NOT_SUPPORTED_get());
                                System.out.println(edkJavaJNI.EDK_FILE_ERROR_get());
                                System.out.println(edkJavaJNI.EDK_NO_EVENT_get());
                                System.out.println(edkJavaJNI.EDK_GYRO_NOT_CALIBRATED_get());
                                System.out.println(edkJavaJNI.EDK_OPTIMIZATION_IS_ON_get());
                                System.out.println(edkJavaJNI.EDK_RESERVED1_get());
                                System.out.println(edkJavaJNI.EDK_COULDNT_RESOLVE_PROXY_get());
                                System.out.println(edkJavaJNI.EDK_COULDNT_RESOLVE_HOST_get());
                                System.out.println(edkJavaJNI.EDK_COULDNT_CONNECT_get());
                                System.out.println(edkJavaJNI.EDK_OPERATION_TIMEDOUT_get());
                                System.out.println(edkJavaJNI.EDK_CLOUD_PROFILE_EXISTS_get());
                                System.out.println(edkJavaJNI.EDK_UPLOAD_FAILED_get());
                                System.out.println(edkJavaJNI.EDK_INVALID_CLOUD_USER_ID_get());
                                System.out.println(edkJavaJNI.EDK_INVALID_ENGINE_USER_ID_get());
                                System.out.println(edkJavaJNI.EDK_CLOUD_USER_ID_DONT_LOGIN_get());
                                System.out.println(edkJavaJNI.EDK_EMOTIVCLOUD_UNINITIALIZED_get());
                                System.out.println(edkJavaJNI.EDK_FILE_EXISTS_get());
                                System.out.println(edkJavaJNI.EDK_HEADSET_NOT_AVAILABLE_get());
                                System.out.println(edkJavaJNI.EDK_HEADSET_IS_OFF_get());
                                System.out.println(edkJavaJNI.EDK_SAVING_IS_RUNNING_get());
                                System.out.println(edkJavaJNI.EDK_DEVICE_CODE_ERROR_get());
                                System.out.println(edkJavaJNI.EDK_LICENSE_ERROR_get());
                                System.out.println(edkJavaJNI.EDK_LICENSE_EXPIRED_get());
                                System.out.println(edkJavaJNI.EDK_LICENSE_NOT_FOUND_get());
                                System.out.println(edkJavaJNI.EDK_OVER_QUOTA_get());
                                System.out.println(edkJavaJNI.EDK_INVALID_DEBIT_ERROR_get());
                                System.out.println(edkJavaJNI.EDK_OVER_DEVICE_LIST_get());
                                System.out.println(edkJavaJNI.EDK_APP_QUOTA_EXCEEDED_get());
                                System.out.println(edkJavaJNI.EDK_APP_INVALID_DATE_get());
                                System.out.println(edkJavaJNI.EDK_LICENSE_DEVICE_LIMITED_get());
                                System.out.println(edkJavaJNI.EDK_LICENSE_REGISTERED_get());
                                System.out.println(edkJavaJNI.EDK_NO_ACTIVE_LICENSE_get());
                                System.out.println(edkJavaJNI.EDK_LICENSE_NO_EEG_get());
                                System.out.println(edkJavaJNI.EDK_UPDATE_LICENSE_get());
                                System.out.println(edkJavaJNI.EDK_INVALID_DEBIT_NUMBER_get());
                                System.out.println(edkJavaJNI.EDK_DAILY_DEBIT_LIMITED_get());
                                System.out.println(edkJavaJNI.EDK_FILE_NOT_FOUND_get());
                                System.out.println(edkJavaJNI.EDK_ACCESS_DENIED_get());
                                System.out.println(edkJavaJNI.EDK_NO_INTERNET_CONNECTION_get());
                                System.out.println(edkJavaJNI.EDK_AUTHENTICATION_ERROR_get());
                                System.out.println(edkJavaJNI.EDK_LOGIN_ERROR_get());
                                System.out.println(edkJavaJNI.MAX_NUM_OF_BACKUP_PROFILE_VERSION_get());

                                if (result == edkJava.EDK_OK) {
                                    Log.e("FFT", "GetAverageBandPowers");
                                    System.out.println(edkJava.double_p_value(palpha));
                                    System.out.println(edkJava.double_p_value(plow_beta));
                                    System.out.println(edkJava.double_p_value(phigh_beta));
                                    System.out.println(edkJava.double_p_value(ptheta));
                                    System.out.println(edkJava.double_p_value(pgamma));
                                }
                                edkJava.delete_double_p(ptheta);
                                edkJava.delete_double_p(palpha);
                                edkJava.delete_double_p(plow_beta);
                                edkJava.delete_double_p(phigh_beta);
                                edkJava.delete_double_p(pgamma);
                            }

                            break;
                        /*case IEE_MentalCommandEvent:
                            IEE_MentalCommandEvent_t type = edkJava.IEE_MentalCommandEventGetType(handleEvent);
                            if (type == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingStarted) {
                                Log.e("MentalCommand", "training started");
                                hander.sendEmptyMessage(HANDLER_TRAIN_STARTED);
                            } else if (type == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingSucceeded) {
                                Log.e("MentalCommand", "training Succeeded");
                                hander.sendEmptyMessage(HANDLER_TRAIN_SUCCEED);
                            } else if (type == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingCompleted) {
                                Log.e("MentalCommand", "training Completed");
                                hander.sendEmptyMessage(HANDLER_TRAIN_COMPLETED);
                            } else if (type == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingDataErased) {
                                Log.e("MentalCommand", "training erased");
                                hander.sendEmptyMessage(HANDLER_TRAIN_ERASED);

                            } else if (type == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingFailed) {
                                Log.e("MentalCommand", "training failed");
                                hander.sendEmptyMessage(HANDLER_TRAIN_FAILED);

                            } else if (type == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingRejected) {
                                Log.e("MentalCommand", "training rejected");
                                hander.sendEmptyMessage(HANDLER_TRAIN_REJECTED);
                            } else if (type == IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingReset) {
                                Log.e("MentalCommand", "training Reset");
                                hander.sendEmptyMessage(HANDLER_TRAINED_RESET);
                            }
                            //	IEE_MentalCommandAutoSamplingNeutralCompleted,
                            //	IEE_MentalCommandSignatureUpdated;
                            break;
                        case IEE_FacialExpressionEvent:
                            break;*/
                        default:
                            break;
                    }
                }
            }
        };
    }

    Handler hander = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_USER_ADD:
                    if (delegate != null)
                        delegate.userAdd(userId);
                    break;
                case HANDLER_USER_REMOVE:
                    if (delegate != null)
                        delegate.userRemoved();
                    break;
                case HANDLER_ACTION_CURRENT:
                    if (delegate != null)
                        delegate.currentAction(edkJava.IS_MentalCommandGetCurrentAction(emoState), edkJava.IS_MentalCommandGetCurrentActionPower(emoState));
                    break;
                case HANDLER_TRAIN_STARTED:
                    if (delegate != null)
                        delegate.trainStarted();
                    break;
                case HANDLER_TRAIN_SUCCEED:
                    if (delegate != null)
                        delegate.trainSucceed();
                    break;
                case HANDLER_TRAIN_FAILED:
                    if(delegate != null)
                        delegate.trainFailed();
                    break;
                case HANDLER_TRAIN_COMPLETED:
                    if (delegate != null)
                        delegate.trainCompleted();
                    break;
                case HANDLER_TRAIN_ERASED:
                    if (delegate != null)
                        delegate.trainErased();
                    break;
                case HANDLER_TRAIN_REJECTED:
                    if (delegate != null)
                        delegate.trainRejected();
                    break;
                case HANDLER_TRAINED_RESET:
                    if (delegate != null)
                        delegate.trainReset();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void disconnect() {

    }

    @Override
    public void searchAndConnect() {

    }

    @Override
    public void stopSearching() {

    }
}

interface EngineInterface {
    //train
    public void trainStarted();
    public void trainSucceed();
    public void trainFailed();
    public void trainCompleted();
    public void trainRejected();
    public void trainReset();
    public void trainErased();
    public void userAdd(int userId);
    public void userRemoved();

    //action
    public void currentAction(IEE_MentalCommandAction_t typeAction,float power);

}
