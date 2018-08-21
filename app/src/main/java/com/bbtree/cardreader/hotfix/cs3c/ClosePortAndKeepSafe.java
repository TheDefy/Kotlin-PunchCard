package com.bbtree.cardreader.hotfix.cs3c;

import android.content.Context;

import com.bbtree.baselib.utils.PackageUtils;
import com.bbtree.cardreader.utils.ShellUtils;
import com.orhanobut.logger.Logger;

/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2016/01/06
 * Time: 下午2:01
 * Function:关闭网络调试端口，并卸载流氓软件CIBN
 */
public class ClosePortAndKeepSafe {
    private static final String TAG = ClosePortAndKeepSafe.class.getSimpleName();

    public static void fix(Context context) {
        Logger.d(">>>CS3C net work debug will be close");
        ShellUtils.execCommand("stop adbd", true);
        ShellUtils.execCommand("setprop service.adb.tcp.port 0", true);
        ShellUtils.execCommand("start adbd ", true);

        //卸载流氓软件
        PackageUtils.uninstall(context, "com.cibn.tv");
        PackageUtils.uninstall(context, "com.assistant.tv.server");
        PackageUtils.uninstall(context, "com.qiyi.tvassistant");


    }
}
