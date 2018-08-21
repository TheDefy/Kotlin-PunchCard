package com.bbtree.cardreader.entity.requestEntity;

import android.os.Build;

import java.io.Serializable;


/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/06/17
 * Time: 下午2:47
 */
public class UpdateCheckRequest implements Serializable {

    private int hostVersionCode;
    private String hostVersionName;
    private String hostPackageName;

    private int targetVersionCode;
    private String targetVersionName;
    private String targetPackageName;

    private String deviceId;//设备ID
    private String machineModel;
    /**
     *类型ID,1-智能考勤机 2-NFC手机考勤 4-车载考勤机 5-智能门闸机 -1-看门狗
     */
    private int appType;
    private int appVersion;
    private String packageName;

    private int SDK_INT = Build.VERSION.SDK_INT;//系统的API级别 数字表示

    public int getHostVersionCode() {
        return hostVersionCode;
    }

    public void setHostVersionCode(int hostVersionCode) {
        this.hostVersionCode = hostVersionCode;
    }

    public String getHostVersionName() {
        return hostVersionName;
    }

    public void setHostVersionName(String hostVersionName) {
        this.hostVersionName = hostVersionName;
    }

    public String getHostPackageName() {
        return hostPackageName;
    }

    public void setHostPackageName(String hostPackageName) {
        this.hostPackageName = hostPackageName;
    }

    public int getTargetVersionCode() {
        return targetVersionCode;
    }

    public void setTargetVersionCode(int targetVersionCode) {
        this.targetVersionCode = targetVersionCode;
    }

    public String getTargetVersionName() {
        return targetVersionName;
    }

    public void setTargetVersionName(String targetVersionName) {
        this.targetVersionName = targetVersionName;
    }

    public String getTargetPackageName() {
        return targetPackageName;
    }

    public void setTargetPackageName(String targetPackageName) {
        this.targetPackageName = targetPackageName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getMachineModel() {
        return machineModel;
    }

    public void setMachineModel(String machineModel) {
        this.machineModel = machineModel;
    }

    public int getAppType() {
        return appType;
    }

    public void setAppType(int appType) {
        this.appType = appType;
    }

    public int getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(int appVersion) {
        this.appVersion = appVersion;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getSDK_INT() {
        return SDK_INT;
    }

    public void setSDK_INT(int SDK_INT) {
        this.SDK_INT = SDK_INT;
    }
}
