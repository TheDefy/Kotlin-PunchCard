package com.bbtree.baselib.net;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.bbtree.baselib.base.BaseApp;
import com.bbtree.baselib.crypto.AESUtil;
import com.bbtree.baselib.utils.FileUtils;
import com.bbtree.baselib.utils.ShellUtils;
import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhouyl on 16/03/2017.
 */

public class BaseParam {

    private static final String COBABYS_Z2 = "Z2";//幼乐宝Z2
    private static final String COBABYS_M2 = "M2";//幼乐宝Z2的板子，一块板子两个机型

    private static final String TU_XING_GATE = "A2318";// 土星门闸机

    private static java.lang.String PATTON_STRING = "[0-9]|[0-9][0-9]";

    private static BaseParam param = new BaseParam();

    public static BaseParam getInstance() {
        return param;
    }

    public static String beginDeviceid="";

    public static  Context context;

    static String deviceId = null;

    static boolean first = true;

    public static String getDeviceId() {

        // TODO test
        if (BaseApp.getInstance().isDebug()) return "123456";

        synchronized (beginDeviceid) {
            if (first == true) {
                first = false;
                beginDeviceid = readDeviceIdFromDisk();
                Log.e("beginDeviceidFirst", beginDeviceid);
            }
        }

        if (BaseApp.getInstance().isFactory()) {
            return "999999999999999";
        }
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = readDeviceIdFromDisk();
            /*
            if( TextUtils.isEmpty(deviceId) == false ) {
                beginDeviceid = new String(deviceId);
            }
            */
            if (TextUtils.isEmpty(deviceId) || deviceId.equals("empty")) {
                if (TU_XING_GATE.equals(Build.MODEL)) {// 土星门闸机
                    deviceId = "999999999999999_" + getMacAddress();
                } else {
                    deviceId = getImei() + "_" + getMacAddress();
                }
                //beginDeviceid = "";
                //writeDeviceId2Disk(deviceId);
            }
            else
            {
                //beginDeviceid = new String(deviceId);
                //writeDeviceId2Disk(deviceId);
            }
        }
        else
        {
            //beginDeviceid = new String(deviceId);
        }

        return deviceId;
    }


    public static String readDeviceIdFromDisk() {
        String finalPathUniversal = deviceIdFilePath + File.separator + deviceIdUniversal;
        //String finalPathUniversal2 = deviceIdFilePath2 + File.separator + deviceIdUniversal;
        //String finalPathUniversal3 = deviceIdFilePath3 + File.separator + deviceIdUniversal;
        ShellUtils.CommandResult resultUniversal = ShellUtils.execCommand("cat " + finalPathUniversal, true, true);

        String seedUniversal = resultUniversal.successMsg;
        if (TextUtils.isEmpty(seedUniversal))
        {
            /*
            resultUniversal = ShellUtils.execCommand("cat " + finalPathUniversal2, true, true);
            seedUniversal = resultUniversal.successMsg;
            if (TextUtils.isEmpty(seedUniversal))
            {
                resultUniversal = ShellUtils.execCommand("cat " + finalPathUniversal3, true, true);
                seedUniversal = resultUniversal.successMsg;

                if( TextUtils.isEmpty(seedUniversal) == false )
                {
                    validPath = deviceIdFilePath3;
                }
            }
            else
            {
                validPath = deviceIdFilePath2;
            }
            */

        }
        else
        {
            validPath = deviceIdFilePath;
        }

        if( TextUtils.isEmpty(seedUniversal) == false )
        {
            String result = AESUtil.decrypt(seedUniversal, SECRET[0], SECRET[1]);
            return result;
        }
        else
        {
            seedUniversal = FileUtils.readFileSdcardFile(FileUtils.getExternalDir(context) +  File.separator + deviceIdUniversal);
            if( TextUtils.isEmpty(seedUniversal) == false ) {
                validPath = "sdcard";

                String result = AESUtil.decrypt(seedUniversal, SECRET[0], SECRET[1]);
                return result;
            }

            validPath = "no";
            return "empty";
        }

    }

    public void writeDeviceId2Disk(String deviceId) {
        String finalPathUniversal = deviceIdFilePath + File.separator + deviceIdUniversal;
        //String finalPathUniversal2 = deviceIdFilePath2 + File.separator + deviceIdUniversal;
        //String finalPathUniversal3 = deviceIdFilePath3 + File.separator + deviceIdUniversal;


        String aesResult = AESUtil.encrypt(deviceId, SECRET[0], SECRET[1]);
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand("echo " + aesResult + " > " + finalPathUniversal, true);
        if (TextUtils.isEmpty(commandResult.errorMsg)) {
            Logger.d("writeDeviceId2Disk origin:" + deviceId);
            Logger.d("writeDeviceId2Disk encrypt:" + aesResult);
        } else {
            Logger.w("writeDeviceId2Disk fail:" + commandResult.errorMsg);
        }
/*
        commandResult = ShellUtils.execCommand("echo " + aesResult + " > " + finalPathUniversal2, true);
        if (TextUtils.isEmpty(commandResult.errorMsg)) {
            Logger.d("writeDeviceId2Disk2 origin:" + deviceId);
            Logger.d("writeDeviceId2Disk2 encrypt:" + aesResult);
        } else {
            Logger.w("writeDeviceId2Disk2 fail:" + commandResult.errorMsg);
        }

        commandResult = ShellUtils.execCommand("echo " + aesResult + " > " + finalPathUniversal3, true);
        if (TextUtils.isEmpty(commandResult.errorMsg)) {
            Logger.d("writeDeviceId2Disk3 origin:" + deviceId);
            Logger.d("writeDeviceId2Disk3 encrypt:" + aesResult);
        } else {
            Logger.w("writeDeviceId2Disk3 fail:" + commandResult.errorMsg);
        }
*/


        FileUtils.writeFileSdcardFile(FileUtils.getExternalDir(context) +  File.separator + deviceIdUniversal, aesResult);
    }

    /**
     * 设备类型
     *
     * @return
     */
    public static int getMachineType() {
        try {
            String[] split = Build.MODEL.split("_");
            if (split.length == 4) {
                String type = split[split.length - 1];
                Pattern pattern = Pattern.compile(PATTON_STRING);
                Matcher matcher = pattern.matcher(type);
                if (matcher.matches()) {
                    return Integer.valueOf(type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 1;
    }

    /**
     * 设备ID
     */
    public static String getImei() {
        TelephonyManager telephonyManager = (TelephonyManager) BaseApp.getMContext().getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        if (TextUtils.isEmpty(imei)) {
            imei = "000000000000000";
        }
        if (COBABYS_M2.equals(Build.MODEL)
                || COBABYS_Z2.equals(Build.MODEL)// 适配imei值 重启会变的机子(幼乐宝Z2 M2)
                ||Build.DEVICE.equals("sugar-ref001")) { //土星个别机器.
            imei = Build.MODEL;
        }
        return imei;
    }

    public static String getMacAddress() {
        try {
            // Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
            String netAddress = loadFileAsString("/sys/class/net/eth0/address")
                    .toUpperCase().substring(0, 17);
            if (!TextUtils.isEmpty(netAddress)) {
                return netAddress.replaceAll(":", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //logger.error("获取以太网mac地址失败:getMacAddress error");
        }
        return "";
    }

    /**
     * 获取本机mac地址
     *
     * @param context
     * @return
     */
    public static String getWifiMac(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String macAddress = info.getMacAddress();
        if (!TextUtils.isEmpty(macAddress)) {
            return macAddress.replaceAll(":", "");
        }
        return macAddress;
    }


    public static String loadFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }


    private static String DEVICE_UUID;
    private static final String filePath = "/system";
    private static final String fileName = "seed.inf";
    private static final String fileNameUniversal = "seed2.inf";
    private static final String deviceIdFilePath = "/data";//"/dev";//"/dev";
    //private static final String deviceIdFilePath2 = "/data/data";
    //private static final String deviceIdFilePath3 = "/data";

    private static final String deviceIdUniversal = ".devid.inf";
    private static final String SECRET[] = {"9664a62144034eb3", "8dc6693fa76ec135"};


    public static String validPath = "";
    /**
     * 真实的DeviceID
     *
     * @return
     */
    public static String getOldDeviceId() {
        boolean factory;
        String imei = getImei();
        factory = "999999999999999".equals(imei);
        if (factory) {
            return imei;
        }

        if (!ShellUtils.checkRootPermission()) {
            com.orhanobut.logger.Logger.i("no root permission");
            return imei;
        }

        if (!TextUtils.isEmpty(DEVICE_UUID)) {
            return DEVICE_UUID;
        }

        final String finalPath = filePath + File.separator + fileName;
        final String finalPathUniversal = filePath + File.separator + fileNameUniversal;

        ShellUtils.CommandResult result = ShellUtils.execCommand("cat " + finalPath, true, true);
        ShellUtils.CommandResult resultUniversal = ShellUtils.execCommand("cat " + finalPathUniversal, true, true);
        String seed = result.successMsg;
        String seedUniversal = resultUniversal.successMsg;
        if (TextUtils.isEmpty(seed) && TextUtils.isEmpty(seedUniversal)) {
            //全新机器
            ShellUtils.execCommand("mount -o remount,rw " + filePath, true);
            ShellUtils.execCommand("touch " + finalPathUniversal, true);
            DEVICE_UUID = UUID.randomUUID().toString().replaceAll("-", "");
            String aesResult = AESUtil.encrypt(DEVICE_UUID, SECRET[0], SECRET[1]);
            ShellUtils.execCommand("echo " + aesResult + " > " + finalPathUniversal, true);
        } else if (TextUtils.isEmpty(seed) && !TextUtils.isEmpty(seedUniversal)) {
            //第二版机器码规则
            try {
                DEVICE_UUID = AESUtil.decrypt(seedUniversal, SECRET[0], SECRET[1]);
            } catch (Exception e) {
                e.printStackTrace();
                DEVICE_UUID = null;
            }
        } else if (!TextUtils.isEmpty(seed) && TextUtils.isEmpty(seedUniversal)) {
            //第一版机器码规则
            DEVICE_UUID = imei + seed;
        } else {
            //第二版机器码规则
            try {
                DEVICE_UUID = AESUtil.decrypt(seedUniversal, SECRET[0], SECRET[1]);
            } catch (Exception e) {
                e.printStackTrace();
                DEVICE_UUID = null;
            }
        }
        com.orhanobut.logger.Logger.i("device_id:" + DEVICE_UUID);
        return DEVICE_UUID;
    }
}
