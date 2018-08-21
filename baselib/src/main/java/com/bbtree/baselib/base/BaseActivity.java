package com.bbtree.baselib.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.crashlytics.android.answers.Answers;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by zzz on 16/01/2017.
 */

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers());
        if (getActionBar() != null) {
            getActionBar().hide();
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Countly.sharedInstance().onStart(this);
    }

    @Override
    protected void onStop() {
//        Countly.sharedInstance().onStop();
        super.onStop();
    }
}

