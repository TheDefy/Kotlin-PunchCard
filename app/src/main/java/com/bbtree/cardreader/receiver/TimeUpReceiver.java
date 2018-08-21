package com.bbtree.cardreader.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.bbtree.cardreader.utils.ShellUtils;
import com.bbtree.cardreader.utils.statistics.RecordEvent;

/**
 * Function:
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2016/03/18
 * Time: 下午4:32
 */
public class TimeUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        final String action = intent.getAction();
        if (TextUtils.equals(action, "com.bbtree.cardreader.action.TIMEUP")) {
            RecordEvent.record("Reboot", context);
            ShellUtils.execCommand("reboot", true);
        }
    }
}
