package com.bbtree.cardreader.entity.requestEntity;

import java.io.Serializable;

public class SpeakerBean implements Serializable {
    /**
     * 音响id
     */
    private int id;
    private long schoolId;
    /**
     * 音响号
     */
    private String code;
    /**
     * 音箱名称
     */
    private String name;
    /**
     * 班级
     */
    private String classIds;
    /**
     * 是否全部班级
     */
    private int isAll;
    /**
     * 组-用于向音箱发送指令
     */
    private int groupId;
    /**
     * 号-用于向音箱发送指令
     */
    private int num;
    /**
     * 上次更新时间
     */
    private String lastUpdateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(long schoolId) {
        this.schoolId = schoolId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassIds() {
        return classIds;
    }

    public void setClassIds(String classIds) {
        this.classIds = classIds;
    }

    public int getIsAll() {
        return isAll;
    }

    public void setIsAll(int isAll) {
        this.isAll = isAll;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}