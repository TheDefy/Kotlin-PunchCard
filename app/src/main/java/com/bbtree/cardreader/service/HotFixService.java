package com.bbtree.cardreader.service;

import android.content.Intent;
import android.text.TextUtils;

import com.bbtree.cardreader.base.BaseIntentService;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.hotfix.cs3.BLEFix;
import com.bbtree.cardreader.hotfix.cs3c.ClosePortAndKeepSafe;
import com.bbtree.cardreader.hotfix.cs3plus.MemorySizeFix;
import com.bbtree.cardreader.hotfix.snobs.ResolutionFix;
import com.bbtree.cardreader.hotfix.z2.CobabysZ2ToBBtree;
import com.bbtree.cardreader.hotfix.z3t.Cobabys2BBtree;
import com.bbtree.cardreader.hotfix.zbox.IQEQ2BBtree;
import com.bbtree.cardreader.utils.ReadPhoneInfo;
import com.bbtree.cardreader.utils.ShellUtils;

/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2016/01/05
 * Time: 下午4:29
 */
public class HotFixService extends BaseIntentService {
    public static final String ACTION_HOTFIX = "com.bbtree.cardreader.hotfix";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public HotFixService() {
        super("HotFixService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (TextUtils.equals(ACTION_HOTFIX, action)) {
            boolean needReboot = false;

            switch (ReadPhoneInfo.getPhoneModel()){
                case Constant.PlatformAdapter.CS3Plus:
                    needReboot = fixCS3Plus();
                    break;
                case Constant.PlatformAdapter.CS3:
                    needReboot = fixCS3();
                    break;
                case Constant.PlatformAdapter.CS3C:
                    fixCS3C();
                    break;
                case Constant.PlatformAdapter.ZBOX_V5:
                    needReboot = fixZBOX_V5();
                    break;
                case Constant.PlatformAdapter.Cobabys_Z3T:
                    needReboot = fixCoBabys_z3T();
                    break;
                case Constant.PlatformAdapter.Cobabys_Z2:
                case Constant.PlatformAdapter.Cobabys_M2:
                    needReboot = fixCoBabys_MZ2();
                    break;
                case Constant.PlatformAdapter.Snobs:
                    ResolutionFix.fix();
                    needReboot = false;
                    break;
            }

            if (needReboot) {
                String[] reboot = new String[]{
                        "sleep 15",
                        "reboot"};
                ShellUtils.execCommand(reboot, true);
            }
        }
    }

    private boolean fixCoBabys_MZ2() {
        boolean needReboot = CobabysZ2ToBBtree.fix(mContext);
        UpdateCheckService.startCheckUpdate(mContext);
        return needReboot;
    }

    private boolean fixCoBabys_z3T() {
        boolean needReboot = Cobabys2BBtree.fix(mContext);
        UpdateCheckService.startCheckUpdate(mContext);
        return needReboot;
    }

    private boolean fixZBOX_V5() {
        boolean needReboot = IQEQ2BBtree.fix(mContext);
        UpdateCheckService.startCheckUpdate(mContext);
        return needReboot;
    }

    private void fixCS3C() {
        //CS3C关闭网络调试防止病毒软件
        ClosePortAndKeepSafe.fix(mContext);
        //摄像头前后置修改
//                needReboot = CameraFix.fix();
    }

    private boolean fixCS3() {
        //CS3关闭网络调试防止病毒软件
        ClosePortAndKeepSafe.fix(mContext);
        //CS3机型蓝牙修复
        return BLEFix.fix(mContext);
    }

    private boolean fixCS3Plus() {
        //CS3Plus机型内存修复工作
        return MemorySizeFix.fix();
    }
}
