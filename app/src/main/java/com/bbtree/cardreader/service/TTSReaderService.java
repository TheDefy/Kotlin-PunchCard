package com.bbtree.cardreader.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bbtree.cardreader.BuildConfig;
import com.bbtree.cardreader.TTSWorker;
import com.bbtree.cardreader.tts.IflytekTTSBuilder;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.orhanobut.logger.Logger;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/09/23
 * Create Time: 上午10:43
 */
public class TTSReaderService extends Service {

    private static final String ACTION_READ = "com.bbtree.tts.action.read";
    private String TAG = TTSReaderService.class.getSimpleName();
    private static LinkedBlockingQueue<ReadInfo> queue = new LinkedBlockingQueue<>();
    private TTSWorker.Stub stub = new TTSWorker.Stub() {
        @Override
        public void readText(int speaker, String text) throws RemoteException {
            queue.add(new ReadInfo(speaker, text));
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Logger.i("onBind");
        if (TextUtils.equals(intent.getAction(), ACTION_READ)) {
            return stub;
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initSpeech();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        ReadInfo info = queue.take();
                        if (info == null) {
                            continue;
                        }
                        int speaker = info.speaker;
                        String text = info.text;
                        Logger.i(">>TTSWorker > readText:" + speaker + "  " + text);

                        if (TextUtils.isEmpty(text)) {
                            return;
                        }

                        if (text.startsWith("您好，请上报设备机器号")) {
//                            BaiduTTSBuilder.getInstance().read(text);
                            Logger.t("TTSWorker").i("text:" + text);
                        } else  {
//                            FileUtils.writeFileSdcardFile(FileUtils.getExternalDir(BBTreeApp.getApp()) + "/TTSReaderService1", ">>IflytekTTSBuilder > readText:" + speaker + "  " + text);
//                            if (speaker >= Speaker.BaiduInterval[0] && speaker <= Speaker.BaiduInterval[1]) {
//                                BaiduTTSBuilder.getInstance().read(text);
//                            } else if (speaker >= Speaker.XunfeiInterval[0] && speaker <= Speaker.XunfeiInterval[1]) {
//                                IflytekTTSBuilder.getInstance().read(text);
//                            } else {
//                                IflytekTTSBuilder.getInstance().read(text);
//                            }
                            IflytekTTSBuilder.getInstance().read(text);
//                        } else {
////                            FileUtils.writeFileSdcardFile(FileUtils.getExternalDir(BBTreeApp.getApp()) + "/TTSReaderService2", ">>BaiduTTSBuilder > readText:" + speaker + "  " + text);
//                            BaiduTTSBuilder.getInstance().read(text);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void initSpeech() {
        if (authorized()) {
            Setting.setShowLog(BuildConfig.isDebug);
            StringBuffer param = new StringBuffer();
            param.append("appid=55642b59");
            param.append(",");
            param.append("force_login=true");
            param.append(",");
            // 设置使用v5+
            param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
            SpeechUtility.createUtility(this, param.toString());
            IflytekTTSBuilder.getInstance().build(this);
            //        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=55642b59");
        }
//        BaiduTTSBuilder.getInstance().build(this);
    }

    private boolean authorized() {
//        final String model = ReadPhoneInfo.getPhoneModel();
//        return model.startsWith(Constant.PlatformAdapter.CS3)
//                || model.equals(Constant.PlatformAdapter.Scallops)
//                || model.equals(Constant.PlatformAdapter.Squid)
//                || model.equals(Constant.PlatformAdapter.ZBOX_V5)
//                || Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V4)
//                || model.equals(Constant.PlatformAdapter.Snobs)
//                || Build.MODEL.equals(Constant.PlatformAdapter.Peas)
//                || Build.MODEL.equals(Constant.PlatformAdapter.Donkey);
        return true;
    }

    private class ReadInfo {
        protected int speaker;
        protected String text;

        protected ReadInfo(int speaker, String text) {
            this.speaker = speaker;
            this.text = text;
        }
    }

}
