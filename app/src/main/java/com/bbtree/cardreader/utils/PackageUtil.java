package com.bbtree.cardreader.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.text.TextUtils;

import com.bbtree.baselib.utils.FileUtils;
import com.bbtree.baselib.utils.PackageUtils;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.childservice.utils.MD5;
import com.orhanobut.logger.Logger;

import java.io.File;

/**
 * Created by qiujj on 2017/4/21.
 */

public class PackageUtil {

    /**
     * 文件对比
     *
     * @param filePath
     * @param md5
     * @return
     */
    public static boolean compareFile(String filePath, String md5) {
        try {
            File downloadFile = new File(filePath);
            if (downloadFile.exists() && downloadFile.isFile()) {
                String md5ExistsFile = MD5.calculateMD5(downloadFile);
                Logger.d("the file success download justnow or exist MD5sum is :" + md5ExistsFile + " and the server tell me MD5sum is:" + md5);
                return !TextUtils.isEmpty(md5ExistsFile) && !TextUtils.isEmpty(md5) && md5ExistsFile.equalsIgnoreCase(md5);
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 安装APK
     *
     * @param absPath
     */
    public static boolean installAPK(Context ctx,String absPath) {
        if (TextUtils.isEmpty(absPath)) {
            return false;
        }

        if(ShellUtils.checkRootPermission()){
            PackageInfo watchDogInfo = PackageUtils.getPackageInfo(ctx, Constant.PackageNameInfo.WatchDog);
            if(watchDogInfo == null){//没有安装watchdog，复制到system app
                return DeployWatchDog.deploy(absPath);
            }
        }

        PackageInfo watchDogInfo = PackageUtils.getPackageInfo(ctx, Constant.PackageNameInfo.WatchDog);
        if (watchDogInfo == null && (Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V5)
                || Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V4))) {
            return DeployWatchDog.deploy(absPath);
        } else if (watchDogInfo == null && Build.MODEL.equals(Constant.PlatformAdapter.Cobabys_Z3T)) {
            return DeployWatchDog.deploy(absPath);
        } else if (watchDogInfo == null && (Build.MODEL.equals(Constant.PlatformAdapter.Cobabys_Z2)
                || Build.MODEL.equals(Constant.PlatformAdapter.Cobabys_M2))) {
            return DeployWatchDog.deploy(absPath);
        }
        else if( Build.MODEL.equals(Constant.PlatformAdapter.SOFTWINER_EVB) )
        {
            return DeployWatchDog.deploy(absPath);
        }



        try {
            final File file = new File(absPath);
            String mimeType = FileUtils.getMimeType(file);
            Logger.d("the file of type is: " + mimeType);
            if (mimeType.contains(FileUtils.MIME_TYPE_APP)) {
                long startTime = System.currentTimeMillis();
                int result = PackageUtils.install(ctx, absPath);
                if (result == PackageUtils.INSTALL_SUCCEEDED) {
                    Logger.d("the file of " + absPath + " has install success");
                    Logger.d("Install APK coast:" + (System.currentTimeMillis() - startTime));
                    return true;

                } else if (result == PackageUtils.INSTALL_FAILED_INVALID_URI) {
                    Logger.d("the file of " + absPath + " has install fail of invalid uri");
                    return false;
                } else {
                    Logger.d("the file of " + absPath + " has unknown error");
                    return false;
                }
            } else {
                Logger.d("the file of type is: " + mimeType + " but we want " + FileUtils.MIME_TYPE_APP);
                return false;
            }
        } catch (Exception e) {
//            CrashReport.postCatchedException(e);
            e.printStackTrace();
            return false;
        }

    }
}
