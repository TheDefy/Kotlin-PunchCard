package com.bbtree.cardreader.service;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.bbtree.cardreader.base.BaseIntentService;
import com.bbtree.cardreader.entity.dao.CardRecord;
import com.bbtree.cardreader.model.CardRecordModule;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.List;

/**
 * Function: 磁盘空间释放，删除无用的照片
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/09/14
 * Create Time: 下午3:44
 */
public class FreeUpDiskService extends BaseIntentService {

    private static final String ACTION_FREE_UP_DISK = "com.bbtree.cardreader.action.FreeUpDisk";
    private String TAG = FreeUpDiskService.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public FreeUpDiskService() {
        super(FreeUpDiskService.class.getName());
    }

    public static void startWork(Context context) {
        Intent intent = new Intent(context, FreeUpDiskService.class);
        intent.setAction(ACTION_FREE_UP_DISK);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        if (TextUtils.equals(ACTION_FREE_UP_DISK, action)) {
            freeUpDisk();
        }

    }

    /**
     * 释放磁盘
     */
    private void freeUpDisk() {
        Logger.i("hoho~ I will free up the disk!");

        int i = 0;
//        while (true) {
        List<CardRecord> list = CardRecordModule.getInstance().querySuccessImg();
        if (list == null || list.isEmpty()) {
            return;
        }
        Logger.i("now i am doing the " + i + " time clean");
        i++;

        for (CardRecord record : list) {
            String localPath = record.getCard_holder();
            Logger.i("hoho~ I found \'" + localPath + "\' has upload success! I will delete it~");
            if (TextUtils.isEmpty(localPath)) {
                Logger.w("hoho~ I found \'" + localPath + "\' has upload success!But local SENDERPATH is null???");
                continue;
            }
            try {
                File file = new File(localPath);
                if (file.isFile()) {
                    file.delete();
                }
            } catch (Exception e) {
                Logger.e(e.getMessage());
            }

        }
        stopSelf();
//            Logger.i("hoho~ I will check over number pic,if they over the limit,I will delete is too~~");
//            SnapshotSaver.getInstance().deleteUnnecessaryFiles(mContext);
//            if (list.size() < Constant.CleanDiskPic.MAX_SIZE_PER) {
//                break;
//            }
//        }
    }
}
