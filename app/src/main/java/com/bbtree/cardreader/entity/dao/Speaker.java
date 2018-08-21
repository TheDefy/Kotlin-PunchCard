package com.bbtree.cardreader.entity.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by qiujj on 2017/3/28.
 */
@Entity
public class Speaker {
    @Id
    private long speakerId;
    private Long schoolId;
    private String name;
    private String code;
    private String classIds;
    private Boolean isAll;
    private Integer group_name;
    private Integer number;
    @Keep
    public Speaker(long speakerId, Long schoolId, String name, String code,
            String classIds, Boolean isAll, Integer group, Integer number) {
        this.speakerId = speakerId;
        this.schoolId = schoolId;
        this.name = name;
        this.code = code;
        this.classIds = classIds;
        this.isAll = isAll;
        this.group_name = group;
        this.number = number;
    }
    @Keep
    public Speaker() {
    }
    public long getSpeakerId() {
        return this.speakerId;
    }
    public void setSpeakerId(long speakerId) {
        this.speakerId = speakerId;
    }
    public Long getSchoolId() {
        return this.schoolId;
    }
    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCode() {
        return this.code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getClassIds() {
        return this.classIds;
    }
    public void setClassIds(String classIds) {
        this.classIds = classIds;
    }
    public Boolean getIsAll() {
        return this.isAll;
    }
    public void setIsAll(Boolean isAll) {
        this.isAll = isAll;
    }
    public Integer getGroup_name() {
        return this.group_name;
    }
    public void setGroup_name(Integer group_name) {
        this.group_name = group_name;
    }
    public Integer getNumber() {
        return this.number;
    }
    public void setNumber(Integer number) {
        this.number = number;
    }
}
