package com.bbtree.cardreader.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.bbtree.baselib.utils.ListUtils;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.common.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * SP工具类
 */
public class SPUtils {

    private final static String BB_SP_NAME = "BBTree_Card";

    private SPUtils() {
    }

    /**
     * 设置相机顺时针旋转角度
     */
    public static void setDegrees(int degree) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("carama_degrees", degree).apply();
    }

    /**
     * 获取相机顺时针旋转角度
     */
    public static int getDegrees(int defaultValue) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("carama_degrees", defaultValue);
    }

    /**
     * 存储崩溃信息
     */
    public static void setCrashInfo(String CrashInfo) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("CrashInfo", CrashInfo).apply();
    }

    /**
     * 获取崩溃信息
     */
    public static String getCrashInfo(String defaultValue) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString("CrashInfo", defaultValue);
    }

    /**
     * 保存学校名称
     */
    public static void setSchoolName(String schoolName) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(Constant.XMLStorage.SCHOOL_NAME, schoolName).apply();
    }

    /**
     * 获取学校名称
     */
    public static String getSchoolName(String defaultValue) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constant.XMLStorage.SCHOOL_NAME, defaultValue);
    }

    /**
     * 保存学校id
     */
    public static void setSchoolId(long schoolId) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong(Constant.XMLStorage.SCHOOL_ID, schoolId).apply();
    }

    /**
     * 获取学校id
     */
    public static long getSchoolId(long defaultValue) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(Constant.XMLStorage.SCHOOL_ID, defaultValue);
    }

    /**
     * 保存发射器路径
     */
    public static void setSenderPath(String senderPath) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(Constant.ClassSpeaker.SENDER_KEY, senderPath).apply();
        //        FileUtils.writeFileSdcardFile(FileUtils.getExternalDir(BBTreeApp.getApp()) + "/sender_path", senderPath);
    }

    /**
     * 获取发射器路径
     */
    public static String getSenderPath(String defaultValue) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        String senderPath = sharedPreferences.getString(Constant.ClassSpeaker.SENDER_KEY, defaultValue);
        //        if (TextUtils.isEmpty(senderPath)) {
        //            String s = FileUtils.readFileSdcardFile(FileUtils.getExternalDir(BBTreeApp.getApp()) + "/sender_path");
        //            if (!TextUtils.isEmpty(s)) {
        //                String replace = s.replace("\n", "");
        //                senderPath = replace;
        //            }
        //        }
        return senderPath;
    }

    /**
     * 保存读卡器路径
     */
    public static void setReaderPath(String readerPath) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(Constant.XMLStorage.READER_PATH, readerPath).apply();
        //        FileUtils.writeFileSdcardFile(FileUtils.getExternalDir(BBTreeApp.getApp()) + "/reader_path", readerPath);
    }

    /**
     * 获取读卡器路径
     */
    public static String getReaderPath(String defaultValue) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        String readerPath = sharedPreferences.getString(Constant.XMLStorage.READER_PATH, defaultValue);
        //        if (TextUtils.isEmpty(readerPath)) {
        //            String s = FileUtils.readFileSdcardFile(FileUtils.getExternalDir(BBTreeApp.getApp()) + "/reader_path");
        //            if (!TextUtils.isEmpty(s)) {
        //                readerPath = s.replace("\n", "");
        //            }
        //        }
        return readerPath;
    }

    /**
     * 设置设备别名
     */
    public static void setDeviceAlias(String DeviceAlias) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(Constant.XMLStorage.DEVICE_ALIAS, DeviceAlias).apply();
    }

    /**
     * 获取设备别名
     */
    public static String getDeviceAlias(String defaultValue) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constant.XMLStorage.DEVICE_ALIAS, defaultValue);
    }

    /**
     * 设置协议
     */
    public static void setRFID_ProtocolV2(String RFID_Protocol) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(Constant.XMLStorage.RFID_PROTOCOL_V2, RFID_Protocol).apply();
    }

    /**
     * 获取协议
     */
    public static String getRFID_ProtocolV2(String defaultValue) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constant.XMLStorage.RFID_PROTOCOL_V2, defaultValue);
    }

    public static void setDeviceConfig(String deviceConfig) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(Constant.XMLStorage.DEVICE_CONFIG, deviceConfig).apply();
    }

    public static String getDeviceConfig(String defaultValue) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constant.XMLStorage.DEVICE_CONFIG, defaultValue);
    }

    /**
     * 设置密钥
     */
    public static void setSecretKey(String secretKey) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(Constant.XMLStorage.SECRET_KEY, secretKey).apply();
    }

    /**
     * 获取密钥
     */
    public static String getSecretKey(String defaultValue) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constant.XMLStorage.SECRET_KEY, defaultValue);
    }

    /**
     * 设置波率
     */
    public static void setBaudRate(int BAUD_RATE) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(Constant.XMLStorage.BAUD_RATE, BAUD_RATE).apply();
    }

    /**
     * 获取波率,此处获取的波率不能直接使用,要在SerialPortUtils中获取波率
     */
    public static int getBaudRate(int defaultValue) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Constant.XMLStorage.BAUD_RATE, defaultValue);
    }

    /**
     * 设置音箱通信协议中PIN值
     */
    public static void setSpeakerPin(int speakerPin) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(Constant.ClassSpeaker.PIN_KEY, speakerPin).apply();
    }

    /**
     * 获取音箱通信协议中PIN值
     */
    public static int getSpeakerPin(int defaultValue) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Constant.ClassSpeaker.PIN_KEY, defaultValue);
    }

    /**
     * 设置蓝牙测温枪开启与否
     */
    public static void setTempConfig(boolean isOpen) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(Constant.XMLStorage.TEMP_CONFIG, isOpen).apply();
    }

    /**
     * 获取蓝牙测温枪开启与否
     */
    public static boolean getTempConfig(boolean defaultValue) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Constant.XMLStorage.TEMP_CONFIG, defaultValue);
    }

    /**
     * 设置初始化相关参数
     */
    public static void setLoadingConfig(String loadingConfig) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("loadingConfig", loadingConfig).apply();
    }

    /**
     * 获取初始化相关参数
     */
    public static String getLoadingConfig(String defaultValue) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString("loadingConfig", defaultValue);
    }

    /**
     * ID ：1
     * IC ：2
     */
    public static void setCardReaderType(int type) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("cardReaderType", type).apply();
    }

    public static int getCardReaderType() {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("cardReaderType", 0);
    }

    public static void setFrontCamera(boolean frontCamera) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("frontCamera", frontCamera).apply();
    }

    public static boolean isFrontCamera() {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("frontCamera", false);
    }

    //------------------u盘 video start-----------------------

    /**
     * 存储List<String>
     *
     * @param key     List<String>对应的key
     * @param strList 对应需要存储的List<String>
     */
    public static void putStrListValue(String key, List<String> strList) {
        if (ListUtils.isZero(strList)) {
            return;
        }
        // 保存之前先清理已经存在的数据，保证数据的唯一性
        removeStrList(key);
        int size = strList.size();
        putIntValue(key + "size", size);
        for (int i = 0; i < size; i++) {
            putStringValue(key + i, strList.get(i));
        }
    }

    /**
     * 取出List<String>
     *
     * @param key List<String> 对应的key
     * @return List<String>
     */
    public static List<String> getStrListValue(String key) {
        List<String> strList = new ArrayList<String>();
        int size = getIntValue(key + "size", 0);
        for (int i = 0; i < size; i++) {
            strList.add(getStringValue(key + i, null));
        }
        return strList;
    }

    /**
     * 清空List<String>所有数据
     *
     * @param key     List<String>对应的key
     */
    public static void removeStrList(String key) {
        int size = getIntValue(key + "size", 0);
        if (0 == size) {
            return;
        }
        remove(key + "size");
        for (int i = 0; i < size; i++) {
            remove(key + i);
        }
    }

    /**
     * 存储数据(Int)
     *
     * @param key
     * @param value
     */
    private static void putIntValue(String key, int value) {
        SharedPreferences sp = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putInt(key, value).apply();
    }

    /**
     * 存储数据(String)
     *
     * @param key
     * @param value
     */
    private static void putStringValue(String key, String value) {
        SharedPreferences sp = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    /**
     * 取出数据（int)
     *
     * @param key
     * @param defValue 默认值
     * @return
     */
    private static int getIntValue(String key, int defValue) {
        SharedPreferences sp = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        int value = sp.getInt(key, defValue);
        return value;
    }

    /**
     * 取出数据（String)
     *
     * @param key
     * @param defValue 默认值
     * @return
     */
    private static String getStringValue(String key, String defValue) {
        SharedPreferences sp = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        String value = sp.getString(key, defValue);
        return value;
    }


    /**
     * 清空对应key数据
     *
     * @param key
     */
    public static void remove(String key) {
        SharedPreferences sp = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.remove(key).apply();
    }

    /**
     * 设置第一次插入u盘
     * @param firstInsetUPan
     */
    public static void setFirstInsetUPan(boolean firstInsetUPan) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(Constant.XMLStorage.FIRST_INSET_UPAN, firstInsetUPan).apply();
    }

    /**
     * 获取是否第一次插入u盘
     * @return
     */
    public static boolean isFirstInsetUPan() {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Constant.XMLStorage.FIRST_INSET_UPAN, false);
    }
    //------------------u盘 video end-----------------------

    /**
     * 设置工厂包提示开关
     */
    public static void setFactoryTip(boolean isOpen) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(Constant.XMLStorage.FACTORYTIP, isOpen).apply();
    }

    /**
     * 获取工厂包提示开关
     */
    public static boolean getFactoryTip(boolean defaultValue) {
        SharedPreferences sharedPreferences = BBTreeApp.getApp().getSharedPreferences(BB_SP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Constant.XMLStorage.FACTORYTIP, defaultValue);
    }

}
