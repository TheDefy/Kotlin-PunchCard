package com.bbtree.cardreader.base;

import android.app.IntentService;
import android.content.Context;

/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/06/18
 * Time: 下午2:10
 */
public abstract class BaseIntentService extends IntentService {
    protected Context mContext;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BaseIntentService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
}
