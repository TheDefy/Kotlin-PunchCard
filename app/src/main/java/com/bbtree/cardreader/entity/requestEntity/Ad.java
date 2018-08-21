package com.bbtree.cardreader.entity.requestEntity;

/**
 * Created by chenglei on 2017/6/2.
 */

public class Ad {

    private int id;// 广告的id
    private int adType;//广告类型 1 轮播 2 固定
    private String pic1;//广告图片的url 横屏
    private String pic2;//广告图片的url 竖屏

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAdType() {
        return adType;
    }

    public void setAdType(int adType) {
        this.adType = adType;
    }

    public String getPic1() {
        return pic1;
    }

    public void setPic1(String pic1) {
        this.pic1 = pic1;
    }

    public String getPic2() {
        return pic2;
    }

    public void setPic2(String pic2) {
        this.pic2 = pic2;
    }
}
