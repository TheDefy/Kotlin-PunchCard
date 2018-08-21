package com.bbtree.cardreader.entity.requestEntity;

import java.io.Serializable;

/**
 * 考勤机删除音箱表信息接口
 */
public class SpeakerDeleteRequest implements Serializable {
    /**
     * 音响id
     */
    private int id;
    /**
     * 学校id
     */
    private int schoolId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(int schoolId) {
        this.schoolId = schoolId;
    }
}
