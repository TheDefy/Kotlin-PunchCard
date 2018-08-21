package com.bbtree.cardreader.report;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.bbtree.baselib.base.BaseApp;
import com.bbtree.baselib.crypto.AESUtil;
import com.bbtree.baselib.utils.Logger;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.entity.requestEntity.Reporter;

import java.util.List;


/**
 * Function:记录崩溃时的情况
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2016/01/14
 * Time: 下午8:04
 */
public class CrashSaver {


    public static void record(int crashType, String errorType, String errorMessage, String errorStack) {
        StringBuilder builder = new StringBuilder();

        String alias = BBTreeApp.getApp().getMachineAlias();
        builder.append("Alias:")
                .append(alias)
                .append("\r\n");
        builder.append("CrashType:")
                .append(crashType)
                .append("\r\n")
                .append("ErrorType:")
                .append(errorType)
                .append("\r\n")
                .append("ErrorMessage:")
                .append(errorMessage)
                .append("\r\n")
                .append("errorStack:")
                .append(errorStack);

        PackageManager packageManager = BaseApp.getMContext().getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

        builder.append("\r\n")
                .append(">>>>>>>>>>>>>>>>>>>>>>InstalledPackages>>>>>>>>>>>>>>>>>>>>>>");
        builder.append("\r\n");
        if (packageInfos != null && packageInfos.size() > 0) {
            for (PackageInfo info : packageInfos) {
                builder.append(info.packageName);
                builder.append(" --> ");
                builder.append(packageManager.getApplicationLabel(info.applicationInfo).toString());
                builder.append(" --> ");
                builder.append(info.versionName);
                builder.append("\r\n");
            }
        }
        builder.append("\r\n")
                .append(">>>>>>>>>>>>>>>>>>>>>>NowInfo>>>>>>>>>>>>>>>>>>>>>>");
        builder.append("\r\n");


        ReportData reportData = Reporter.getInstance().build(BBTreeApp.getApp());
        //String info = reportData.toJsonString();
        //builder.append(info);
        builder.append("\r\n");

        Logger.getInstance().log(buildCryptoBody(builder.toString(), "0c6313ab4d5d8f4d9aae848d046fff4d"));
    }

    /**
     * 加密日志信息
     *
     * @param body
     * @param secret
     * @return
     */
    private static String buildCryptoBody(String body, String secret) {
        try {
            return AESUtil.encrypt(body, secret.substring(0, secret.length() - 16),
                    secret.substring(secret.length() - 16, secret.length()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
