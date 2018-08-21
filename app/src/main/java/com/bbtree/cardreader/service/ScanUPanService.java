package com.bbtree.cardreader.service;

import android.content.Context;
import android.content.Intent;
import android.os.storage.StorageManager;
import android.text.TextUtils;

import com.bbtree.baselib.utils.ListUtils;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.base.BaseIntentService;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.entity.eventbus.PauseMusicEvent;
import com.bbtree.cardreader.entity.eventbus.ScreenSaverUPanEvent;
import com.bbtree.cardreader.utils.SPUtils;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by chenglei on 2017/8/16.
 */
public class ScanUPanService extends BaseIntentService {

    private static final String ACTION_SCAN_UPAN = "com.bbtree.cardreader.action.scanPan";

    private String TAG = ScanUPanService.class.getSimpleName();

    private List<File> audioList = new ArrayList<>();

    private List<File> videoList = new ArrayList<>();

    /**
     * Creates an IntentService.
     */
    public ScanUPanService() {
        super(ScanUPanService.class.getName());
    }

    public static void startScanUPanService(Context context) {
        Intent intent = new Intent(context, ScanUPanService.class);
        intent.setAction(ACTION_SCAN_UPAN);
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
        if (TextUtils.equals(ACTION_SCAN_UPAN, action)) {
            getPath(BBTreeApp.getApp());
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.t(TAG).i("onDestroy");
    }

    public void getPath(Context context) {
        long startTime = System.currentTimeMillis();
        audioList.clear();
        videoList.clear();
        String[] result = null;
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Method method = StorageManager.class.getMethod("getVolumePaths");
            method.setAccessible(true);
            try {
                result = (String[]) method.invoke(storageManager);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
//            Thread.sleep(2000);
//            ShellUtils.execCommand("mount -o remount,rw /", true);
            for (int i = 0; i < result.length; i++) {
                Logger.t(TAG).i("path----> " + result[i] + "\n");
                if (!result[i].contains("usb")) continue;
                File file = new File(result[i]);
                getAllFiles(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //视频处理
        if (!ListUtils.isZero(videoList)) {
            Collections.sort(videoList, comparator);
            ArrayList<String> videoUrls = new ArrayList<>();
            for (File tempFile : videoList) {
                videoUrls.add(tempFile.getAbsolutePath());
            }

            SPUtils.putStrListValue(Constant.UPanConstant.VIDEO_LIST_STR_KEY, videoUrls);
        }
        //音频处理
        if (!ListUtils.isZero(audioList)) {
            Collections.sort(audioList, comparator);
            ArrayList<String> audioUrls = new ArrayList<>();
            for (File tempFile : audioList) {
                audioUrls.add(tempFile.getAbsolutePath());
            }
            MusicPlayService.startMusicPlayService(BBTreeApp.getApp(), audioUrls);
            if (!ListUtils.isZero(videoList)) {
                // 播放视频时 暂停音乐
                PauseMusicEvent pauseMusicEvent = new PauseMusicEvent();
                pauseMusicEvent.type = PauseMusicEvent.PauseMusicType.pause;
                pauseMusicEvent.setPause(true);
                pauseMusicEvent.setDelayTime(Integer.MAX_VALUE);
                EventBus.getDefault().post(pauseMusicEvent);
            }
        }

        //扫描完成立即进入轮播
        ScreenSaverUPanEvent screenSaverUPan = new ScreenSaverUPanEvent();
        screenSaverUPan.setDelete(false);
        screenSaverUPan.setToScreenDelay(0);
        EventBus.getDefault().post(screenSaverUPan);
        Logger.t(TAG).i("scan time: " + (System.currentTimeMillis() - startTime));
    }

    public void getAllFiles(File path) {
        File files[] = path.listFiles();
        if (files != null) {
            for (File tempUsbFile : files) {
                if (tempUsbFile.isDirectory()) {
//                    ShellUtils.execCommand("mount -o remount,rw " + tempUsbFile, true);
                    getAllFiles(tempUsbFile);
                } else {
                    if (!tempUsbFile.getName().startsWith(".") && tempUsbFile.getName().endsWith(".mp4")) {
                        Logger.t(TAG).i("File path----> " + tempUsbFile + "\n");
                        videoList.add(tempUsbFile);
                    } else if (!tempUsbFile.getName().startsWith(".") && tempUsbFile.getName().endsWith(".mp3")) {
                        Logger.t(TAG).i("File path----> " + tempUsbFile + "\n");
                        audioList.add(tempUsbFile);
                    }
                }
            }
        }
    }

    private Comparator<File> comparator = new Comparator<File>() {

        @Override
        public int compare(File lhs, File rhs) {

            if (lhs.isDirectory() && !rhs.isDirectory()) {
                return -1;
            }

            if (rhs.isDirectory() && !lhs.isDirectory()) {
                return 1;
            }

            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    };
}
