package com.bbtree.cardreader.utils.statistics;

import android.content.Context;

import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.utils.SPUtils;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;


/**
 * Function:
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2016/02/26
 * Time: 下午2:51
 */
public class RecordEvent {
    public static void record(String key, Context context) {
//        Countly.sharedInstance().recordEvent(key, segmentation, 1);
        Answers.getInstance().logLogin(new LoginEvent()
                .putMethod(key)
                .putSuccess(true)
                .putCustomAttribute("school_name", SPUtils.getSchoolName(""))
                .putCustomAttribute("device_alias", BBTreeApp.getApp().getMachineAlias()));
    }

    public static void record(String key) {
//        Countly.sharedInstance().recordEvent(key, 1);
    }
}
