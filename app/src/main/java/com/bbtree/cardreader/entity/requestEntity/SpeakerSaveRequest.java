package com.bbtree.cardreader.entity.requestEntity;

import java.io.Serializable;

/**
 * 考勤机保存音箱信息接口请求
 */
public class SpeakerSaveRequest implements Serializable {
    /**
     * 音响id,新增时为null
     */
    private String id;
    private long schoolId;
    /**
     * 音响号必传
     */
    private String code;
    /**
     * 音箱名称必传
     */
    private String name;
    /**
     * 班级非必传
     */
    private String classIds;
    /**
     * 是否全部班级，必传
     */
    private int isAll;
    /**
     * 组-用于和音箱通信
     */
    private int groupId;
    /**
     * 号-用于和音箱通信
     */
    private int num;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
}
