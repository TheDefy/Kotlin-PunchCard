package com.bbtree.cardreader.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.bbtree.cardreader.view.activity.SplashActivity;
import com.orhanobut.logger.Logger;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/06/29
 * Create Time: 下午12:13
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    private String TAG = BootCompleteReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        Logger.v("-------------" + action);
        if (TextUtils.isEmpty(action)) {
            return;
        }
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            Intent i = new Intent(context, SplashActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
