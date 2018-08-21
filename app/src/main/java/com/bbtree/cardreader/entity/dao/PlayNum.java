package com.bbtree.cardreader.entity.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 广告播放次数统计
 */
@Entity
public class PlayNum {
    @Id
    private String id;// 格式 adId_date
    private Integer adId;//广告id
    private Integer num;//广播播放次数
    private String date;// 广告时间

    @Generated(hash = 614700967)
    public PlayNum(String id, Integer adId, Integer num, String date) {
        this.id = id;
        this.adId = adId;
        this.num = num;
        this.date = date;
    }

    @Generated(hash = 488191215)
    public PlayNum() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getAdId() {
        return adId;
    }

    public void setAdId(Integer adId) {
        this.adId = adId;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
