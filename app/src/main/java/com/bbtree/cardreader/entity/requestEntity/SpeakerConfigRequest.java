package com.bbtree.cardreader.entity.requestEntity;

import java.io.Serializable;

/**
 * 考勤机获取音箱配置接口
 */
public class SpeakerConfigRequest implements Serializable {

    private long schoolId;
    private String deviceId;
    private String sn;

    public long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(long schoolId) {
        this.schoolId = schoolId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }
}
