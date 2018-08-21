package com.bbtree.cardreader.entity.requestEntity;

import java.io.Serializable;

public class ConfigBean implements Serializable {
    private long schoolId;
    /**
     * 语速
     */
    private int speed;
    /**
     * 姓名播放次数
     */
    private int namePlayNum;
    /**
     * 播放次数
     */
    private int playNum;
    /**
     * 是否播报班级
     */
    private int playClass;
    /**
     * 上次更新时间
     */
    private String lastUpdateTime;

    public long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(long schoolId) {
        this.schoolId = schoolId;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getNamePlayNum() {
        return namePlayNum;
    }

    public void setNamePlayNum(int namePlayNum) {
        this.namePlayNum = namePlayNum;
    }

    public int getPlayNum() {
        return playNum;
    }

    public void setPlayNum(int playNum) {
        this.playNum = playNum;
    }

    public int getPlayClass() {
        return playClass;
    }

    public void setPlayClass(int playClass) {
        this.playClass = playClass;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}