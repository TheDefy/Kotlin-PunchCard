package com.bbtree.cardreader.entity.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by qiujj on 2017/3/28.
 */
@Entity
public class SpeakerConfig {
    @Id
    private Long id;
    private Integer schoolId;
    private Integer speed;
    private Integer namePlayNum;
    private Integer playNum;
    private Integer playClass;
    @Generated(hash = 672976776)
    public SpeakerConfig(Long id, Integer schoolId, Integer speed,
            Integer namePlayNum, Integer playNum, Integer playClass) {
        this.id = id;
        this.schoolId = schoolId;
        this.speed = speed;
        this.namePlayNum = namePlayNum;
        this.playNum = playNum;
        this.playClass = playClass;
    }
    @Generated(hash = 1204143338)
    public SpeakerConfig() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Integer getSchoolId() {
        return this.schoolId;
    }
    public void setSchoolId(Integer schoolId) {
        this.schoolId = schoolId;
    }
    public Integer getSpeed() {
        return this.speed;
    }
    public void setSpeed(Integer speed) {
        this.speed = speed;
    }
    public Integer getNamePlayNum() {
        return this.namePlayNum;
    }
    public void setNamePlayNum(Integer namePlayNum) {
        this.namePlayNum = namePlayNum;
    }
    public Integer getPlayNum() {
        return this.playNum;
    }
    public void setPlayNum(Integer playNum) {
        this.playNum = playNum;
    }
    public Integer getPlayClass() {
        return this.playClass;
    }
    public void setPlayClass(Integer playClass) {
        this.playClass = playClass;
    }
}
