package com.bbtree.baselib.base;

import android.app.Application;
import android.content.Context;

import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

/**
 * Created by zzz on 16/01/2017.
 */

public class BaseApp extends Application {

    private static Context mContext;
    private boolean isDebug;
    private boolean isFactory;
    private static BaseApp app ;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger
                .init(Constants.LOGNAEM)                 // default PRETTYLOGGER or use just init()
                .logLevel(isDebug ? LogLevel.FULL : LogLevel.NONE)        // default LogLevel.FULL
                .methodOffset(2);               // default 0
    }

    public static void initialize(Context context) {
        app = new BaseApp();
        mContext = context;
    }


    public static BaseApp getInstance() {

        return app;
    }
    public static Context getMContext() {
        return mContext;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public boolean isFactory() {
        return isFactory;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    public void setFactory(boolean factory) {
        isFactory = factory;
    }
}
