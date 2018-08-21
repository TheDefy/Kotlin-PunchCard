package com.bbtree.cardreader.entity.requestEntity;

import android.content.Context;
import android.os.Build;

import com.bbtree.baselib.net.BaseParam;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.report.ReportData;
import com.bbtree.cardreader.utils.CameraInfoUtils;
import com.bbtree.cardreader.utils.DeviceConfigUtils;
import com.bbtree.cardreader.utils.ReadPhoneInfo;
import com.bbtree.cardreader.utils.SPUtils;
import com.bbtree.cardreader.utils.ScreenUtils;
import com.bbtree.cardreader.utils.ShellUtils;

/**
 * Created by zzz on 22/01/2017.
 */

public class Reporter {
    private static Reporter ourInstance = new Reporter();
    private ReportData reportData;

    private Reporter() {
        reportData = new ReportData();
    }

    public static Reporter getInstance() {
        return ourInstance;
    }

    private volatile boolean hasInit;

    /**
     * 初始化一些自软件启动之后不会有变动的固定信息
     *
     * @param context
     */
    private void init(Context context) {
        if (!hasInit) {
            reportData.DEVICE_ID = BaseParam.getDeviceId();


            reportData.DEVICE_OLD_ID = BaseParam.getOldDeviceId();


            reportData.UUID = ReadPhoneInfo.buildUUID(context);
            reportData.APP_VERSION_CODE = ReadPhoneInfo.getAppVersionCode(context);
            reportData.APP_VERSION_NAME = ReadPhoneInfo.getAppVersionName(context);
            reportData.PACKAGE_NAME = ReadPhoneInfo.getPackageName(context);
            reportData.FILE_PATH = ReadPhoneInfo.getApplicationFilePath(context);
            reportData.TOTAL_MEM_SIZE = ReadPhoneInfo.getTotalMemorySize(context);
            reportData.CPU_NAME = ReadPhoneInfo.getCpuName();
            reportData.MIN_CPU_FREQ = ReadPhoneInfo.getMinCpuFreq();
            reportData.MAX_CPU_FREQ = ReadPhoneInfo.getMaxCpuFreq();
            reportData.CPU_CORE_NUM = ReadPhoneInfo.getCPUCoreNums();
            reportData.TOTAL_SDCARD_SIZE = ReadPhoneInfo.getTotalExternalMemorySize();
            reportData.INTERNAL_STORGE_SIZE = ReadPhoneInfo.getTotalInternalMemorySize();
            reportData.IMEI = BaseParam.getImei();
            reportData.IMSI = ReadPhoneInfo.getIMSI(context);
            reportData.INSTALLER = ReadPhoneInfo.getInstaller(context);

            if (Build.MODEL.equals(Constant.PlatformAdapter.CS3)
                    || Build.MODEL.equals(Constant.PlatformAdapter.CS3Plus)
                    || Build.MODEL.equals(Constant.PlatformAdapter.CS3i)
                    || Build.MODEL.equals(Constant.PlatformAdapter.CS3Pagan)
                    || Build.MODEL.equals(Constant.PlatformAdapter.CS3PaganOSK)
                    || Build.MODEL.equals(Constant.PlatformAdapter.Donkey)
                    || Build.MODEL.equals(Constant.PlatformAdapter.CS3C)
                    || Build.MODEL.equals(Constant.PlatformAdapter.Squid)
                    || Build.MODEL.equals(Constant.PlatformAdapter.Snobs)) {
                reportData.SOURCE_PLATFORM = Constant.PlatformFirms.BBTree;
            } else if (Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V5)
                    || Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V4)) {
                reportData.SOURCE_PLATFORM = Constant.PlatformFirms.IQEQ;
                ShellUtils.CommandResult result = ShellUtils.execCommand("cat /sys/class/rksn/sn", true);
                reportData.SOURCE_DEVICE_ID = result.successMsg;
            } else if (Build.MODEL.equals(Constant.PlatformAdapter.Cobabys_Z3T)
                    || Build.MODEL.equals(Constant.PlatformAdapter.Cobabys_Z2)
                    || Build.MODEL.equals(Constant.PlatformAdapter.Cobabys_M2)) {
                reportData.SOURCE_PLATFORM = Constant.PlatformFirms.Cobabys;
            } else if (Build.MODEL.equals(Constant.PlatformAdapter.Scallops)
                    || Build.MODEL.equals(Constant.PlatformAdapter.Peas)) {
                reportData.SOURCE_PLATFORM = Constant.PlatformFirms.Beiliao;
            } else {
                reportData.SOURCE_PLATFORM = Constant.PlatformFirms.UNKNOWN;
            }

            reportData.SOURCE_PLATFORM = reportData.SOURCE_PLATFORM + "###" + SPUtils.getLoadingConfig("");

            hasInit = true;
        }
    }

    /**
     * 获取当前的实时信息
     *
     * @param context
     * @return
     */
    public ReportData build(Context context) {
        init(context);
        reportData.AVAILABLE_MEM_SIZE = ReadPhoneInfo.getAvailableMemory(context);
        reportData.CURRENT_CPU_FREQ = ReadPhoneInfo.getCurCpuFreq();
        reportData.AVAILABLE_INTERNALSTORGE_SIZE = ReadPhoneInfo.getAvailableInternalMemorySize();
        reportData.AVAILABLE_SDCARD_SIZE = ReadPhoneInfo.getAvailableExternalMemorySize();
        reportData.LANGUAGE = ReadPhoneInfo.localeLanguage();
        reportData.COUNTRY = ReadPhoneInfo.localeCountry();
        reportData.IP_ADDRESS = ReadPhoneInfo.ipAddress(context);
        reportData.MAC_ADDRESS = ReadPhoneInfo.getLocalMacAddress(context);
        reportData.HOST_IP_ADDRESS = ReadPhoneInfo.getHostIp();
        reportData.WIFI_BSSID = ReadPhoneInfo.getWifiInfo(context).getBSSID();
        reportData.WIFI_SSID = ReadPhoneInfo.getWifiInfo(context).getSSID();
        reportData.WIFI_SPEED = ReadPhoneInfo.getWifiInfo(context).getLinkSpeed();
        reportData.WIFI_RSSI = ReadPhoneInfo.getWifiInfo(context).getRssi();
        reportData.HAS_SDCARD = ReadPhoneInfo.externalMemoryAvailable();

        reportData.CONFIG_VERSION = DeviceConfigUtils.getConfig().configVersion;
        reportData.CAMERA_INFO = CameraInfoUtils.getInfo();
        reportData.SCREEN_SIZE = ScreenUtils.getScreenSize(context);
        reportData.ORIENTATION = ScreenUtils.getOrientation(context);
        reportData.ETH_IP_ADDRESS = ReadPhoneInfo.getIPAddress(true);
        reportData.ETH_MAC_ADDRESS = ReadPhoneInfo.getMACAddress("eth0");
        reportData.DNS = ReadPhoneInfo.getDns();
        reportData.CURRENT_TIME_MILLIS = System.currentTimeMillis();
        reportData.NANO_TIME = System.nanoTime();
        reportData.BAUD_RATE = SPUtils.getBaudRate(0);
        reportData.RFID_PROTOCOL = SPUtils.getRFID_ProtocolV2("");
        return reportData;
    }
}
