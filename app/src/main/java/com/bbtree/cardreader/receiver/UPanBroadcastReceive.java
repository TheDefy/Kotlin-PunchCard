package com.bbtree.cardreader.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.widget.Toast;

import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.entity.eventbus.PauseMusicEvent;
import com.bbtree.cardreader.entity.eventbus.ScreenSaverUPanEvent;
import com.bbtree.cardreader.service.ScanUPanService;
import com.bbtree.cardreader.utils.SPUtils;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by chenglei on 2017/8/10.
 */

public class UPanBroadcastReceive extends BroadcastReceiver {

    private static final String TAG = UPanBroadcastReceive.class.getSimpleName();

    private static final String USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    private static final String MEDIA_UNMOUNTED = "android.intent.action.MEDIA_MOUNTED";

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (USB_DEVICE_ATTACHED.equals(action)) {
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device.getDeviceClass() != 0) return;
            Logger.t(TAG).i("u盘插入");
            SPUtils.setFirstInsetUPan(true);
//            ScreenSaverUPanEvent screenSaverUPan = new ScreenSaverUPanEvent();
//            screenSaverUPan.setDelete(false);
////            screenSaverUPan.setToScreenDelay(Integer.MAX_VALUE);
//            screenSaverUPan.setToScreenDelay(0);
//            EventBus.getDefault().post(screenSaverUPan);

        } else if (USB_DEVICE_DETACHED.equals(action)) {
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device.getDeviceClass() != 0) return;
            SPUtils.setFirstInsetUPan(false);

            //u盘移除 停止music service
            PauseMusicEvent pauseMusicEvent = new PauseMusicEvent();
            pauseMusicEvent.type = PauseMusicEvent.PauseMusicType.stopSelf;
            pauseMusicEvent.setStopSelf(true);
            EventBus.getDefault().post(pauseMusicEvent);

            Observable.create(new ObservableOnSubscribe<Object>() {
                @Override
                public void subscribe(@NonNull ObservableEmitter<Object> e) throws Exception {
                    SPUtils.removeStrList(Constant.UPanConstant.VIDEO_LIST_STR_KEY);
                    e.onNext("");
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Object>() {
                        @Override
                        public void accept(@NonNull Object o) throws Exception {
                            //u盘移除 停止视频
                            ScreenSaverUPanEvent screenSaverUPan = new ScreenSaverUPanEvent();
                            screenSaverUPan.setDelete(true);
                            screenSaverUPan.setToScreenDelay(0);
                            EventBus.getDefault().post(screenSaverUPan);
                            Logger.t(TAG).i("u盘已移除");
                        }
                    });

        } else if (MEDIA_UNMOUNTED.equals(action)) {
            if (SPUtils.isFirstInsetUPan()) {// 等待系统MediaScannerReceiver 挂载完usb之后拉取数据
                Toast.makeText(context, "正在检测u盘内容，请稍等...", Toast.LENGTH_LONG).show();
                ScanUPanService.startScanUPanService(context);
            }
        }

    }

    //-------------------------write log start ---------------------

    private static void writeLog2SDCard(String context, String name) {

        File sdDir = null;
        if (Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED))
            sdDir = Environment.getExternalStorageDirectory();

        File file = new File(sdDir + File.separator + name + ".txt");
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            bufferedWriter.write(context);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //-------------------------write log end ---------------------
}
