package com.bbtree.cardreader.service;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.text.TextUtils;

import com.bbtree.baselib.net.BaseParam;
import com.bbtree.baselib.rxjava.RxUtils;
import com.bbtree.baselib.utils.PackageUtils;
import com.bbtree.cardreader.base.BaseIntentService;
import com.bbtree.cardreader.common.Code;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.common.Urls;
import com.bbtree.cardreader.entity.requestEntity.UpdateCheckRequest;
import com.bbtree.cardreader.entity.requestEntity.UpdateInfo;
import com.bbtree.cardreader.utils.AppDownloadTask;
import com.bbtree.cardreader.utils.PackageUtil;
import com.bbtree.cardreader.utils.ReadPhoneInfo;
import com.bbtree.cardreader.utils.ShellUtils;
import com.orhanobut.logger.Logger;

import java.io.File;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;


/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/06/17
 * Time: 下午3:02
 * 检测watcgdog 版本更新。  相互检测。  ？？？
 */
public class UpdateCheckService extends BaseIntentService {
    private static final String TAG = UpdateCheckService.class.getSimpleName();
    private static final String ACTION_CHECK_UPDATE = "com.bbtree.cardreader.action.CHECK_UPDATE";
    private static final String ACTION_DOWNLOAD_NEW_VERSION = "com.bbtree.cardreader.action.DOWNLOAD_NEW_VERSION";
    private static final String EXTRA_UPDATE_INFO = "updateInfo";
    private AppDownloadTask mDownloadTask;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * <p/>
     * Used to name the worker thread, important only for debugging.
     */
    public UpdateCheckService() {
        super(TAG);
    }

    public static void startCheckUpdate(Context context) {
        Intent intent = new Intent(context, UpdateCheckService.class);
        intent.setAction(ACTION_CHECK_UPDATE);
        context.startService(intent);
    }

    public static void startDownload(Context context, String updateInfoStr) {
        Intent intent = new Intent(context, UpdateCheckService.class);
        intent.setAction(ACTION_DOWNLOAD_NEW_VERSION);
        intent.putExtra(EXTRA_UPDATE_INFO, updateInfoStr);
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
        if (TextUtils.equals(action, ACTION_CHECK_UPDATE)) {
            getAvailableUpdates();
        }
    }

    /**
     * 检查可用更新、下载安装
     */
    private void getAvailableUpdates() {
        UpdateCheckRequest updateCheck = new UpdateCheckRequest();
        PackageInfo watchDogInfo = PackageUtils.getPackageInfo(mContext, Constant.PackageNameInfo.WatchDog);
        if (watchDogInfo != null) {
            updateCheck.setTargetVersionCode(watchDogInfo.versionCode);
            updateCheck.setTargetVersionName(watchDogInfo.versionName);
            updateCheck.setAppVersion(watchDogInfo.versionCode);
        } else if (Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V5)
                || Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V4)
                || Build.MODEL.equals(Constant.PlatformAdapter.Cobabys_Z3T)) {
            updateCheck.setTargetVersionCode(1);
            updateCheck.setTargetVersionName("1.0");
            updateCheck.setAppVersion(-1);
        }
        updateCheck.setTargetPackageName(Constant.PackageNameInfo.WatchDog);
        updateCheck.setHostPackageName(ReadPhoneInfo.getPackageName(this));
        updateCheck.setHostVersionCode(ReadPhoneInfo.getAppVersionCode(this));
        updateCheck.setHostVersionName(ReadPhoneInfo.getAppVersionName(this));

        updateCheck.setDeviceId(BaseParam.getDeviceId());
        updateCheck.setMachineModel(Build.MODEL);
        updateCheck.setAppType(-1);
        updateCheck.setPackageName(Constant.PackageNameInfo.WatchDog);
        RxUtils.postEntity(Urls.CHECKUPDATE, updateCheck)
                .map(RxUtils.getObject(UpdateInfo.class))
                .filter(new Predicate<UpdateInfo>() {
                    @Override
                    public boolean test(UpdateInfo updateInfoResult) {
                        return null != updateInfoResult && (updateInfoResult.getCode() == Code.SUCCESS || updateInfoResult.getCode() == 0);
                    }
                })
                .subscribe(new Observer<UpdateInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(UpdateInfo value) {
                        if(value != null && value.data != null){
                            initDownload(value.data);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /**
     * 先判断此路径是否已经有此文件；
     * 如果有此文件判断MD5指纹信息；符合则不下载立即安装；
     * 不符合，再下载安装。
     */
    private void initDownload(UpdateInfo.Update value) {
        if(!value.isFoundNewVersion()){
            return;
        }
        if (PackageUtil.compareFile(value.getDownloadUrl(), value.getMd5Sum())) {
            Logger.d("the file has downloaded just install it!");
            boolean b = PackageUtil.installAPK(mContext, value.getDownloadUrl());
            if (b) {
                Logger.d(TAG, "安装成功！！！");
                ShellUtils.execCommand("reboot", true);
            } else {
                Logger.e(TAG, "安装失败！！！");
            }
            return;
        } else {
            new File(value.getDownloadUrl()).delete();
        }
        if (value != null) {
            if (mDownloadTask == null) {
                mDownloadTask = new AppDownloadTask(mContext, value);
                mDownloadTask.execute();
            } else {
                if (!mDownloadTask.isDown()) {
                    mDownloadTask.execute();
                }
            }
        } else {
            Logger.e("result UpdateInfoResult is null");
        }
    }
}
