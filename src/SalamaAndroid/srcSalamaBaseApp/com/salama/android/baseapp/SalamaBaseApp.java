package com.salama.android.baseapp;

import org.OpenUDID.IOpenUDIDInitCompleted;
import org.OpenUDID.OpenUDID_manager;

import com.salama.android.support.ServiceSupportApplication;
import com.salama.android.util.SSLog;

public class SalamaBaseApp extends ServiceSupportApplication implements IOpenUDIDInitCompleted {
    private final static String LOG_TAG = SalamaBaseApp.class.getSimpleName();
    
    private String _udid = null;
    
    private static SalamaBaseApp _singleton = null;
    public static SalamaBaseApp singleton() {
        return _singleton;
    }

    @Override
    public void onCreate() {
        SSLog.i(LOG_TAG, "onCreate() begin --------");
        super.onCreate();
        
        _singleton = this;

        //init Open udid
        OpenUDID_manager.sync(this, this);
        
        
        SSLog.i(LOG_TAG, "onCreate() done --------");
    }
    
    @Override
    public void openUDIDInitCompleted() {
        _udid = OpenUDID_manager.getOpenUDID();
        SSLog.i(LOG_TAG, "openUDIDInitCompleted() udid:" + _udid);
    }

    @Override
    public void onTerminate() {
        SSLog.i(LOG_TAG, "onTerminate() begin --------");

        super.onTerminate();
        SSLog.i(LOG_TAG, "onTerminate() udid:" + OpenUDID_manager.getOpenUDID());

        //_singleton = this;
        SSLog.i(LOG_TAG, "onTerminate() done --------");
    }

    public String getUDID() {
        return _udid;
    }
}
