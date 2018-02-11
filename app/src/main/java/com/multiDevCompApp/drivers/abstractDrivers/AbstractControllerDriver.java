package com.multiDevCompApp.drivers.abstractDrivers;

import com.multiDevCompApp.drivers.driversInterfaces.AdapterActivity;
import com.multiDevCompApp.drivers.driversInterfaces.Controller;

/**
 * Created by sserr on 11/02/2018.
 */

public abstract class AbstractControllerDriver implements Controller{

    protected boolean active = true;
    private AdapterActivity adapterActivity;

    public AbstractControllerDriver(AdapterActivity adapterActivity) {
        this.adapterActivity = adapterActivity;
    }

    @Override
    public abstract void disconnect();

    @Override
    public abstract void startSearching();

    @Override
    public abstract void stopSearching();

    @Override
    public boolean activate(boolean active) {
        this.active = active;
        return false;
    }
}
