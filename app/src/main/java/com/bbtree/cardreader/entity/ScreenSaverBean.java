package com.bbtree.cardreader.entity;


import com.bbtree.cardreader.entity.requestEntity.Ad;
import com.bbtree.cardreader.entity.requestEntity.ScreenSaverResult;

/**
 * Function: 轮播图定时器ScreenSaverPagerAdapter的javaBean
 * Created by BBTree Team
 */
public class ScreenSaverBean {
    private int type;  // 0 图片  1 文字 // 3 video
    private int res;   // 显示默认图
    private ScreenSaverResult.Notices notice;//文字
    private ScreenSaverResult.Pictures picture;//图片
    private ScreenSaverResult.Videos video;//视频
    private Ad ad;// 广告

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRes() {
        return res;
    }

    public void setRes(int res) {
        this.res = res;
    }

    public ScreenSaverResult.Notices getNotice() {
        return notice;
    }

    public void setNotice(ScreenSaverResult.Notices notice) {
        this.notice = notice;
    }

    public ScreenSaverResult.Pictures getPicture() {
        return picture;
    }

    public void setPicture(ScreenSaverResult.Pictures picture) {
        this.picture = picture;
    }

    public ScreenSaverResult.Videos getVideo() {
        return video;
    }

    public void setVideo(ScreenSaverResult.Videos video) {
        this.video = video;
    }

    public Ad getAd() {
        return ad;
    }

    public void setAd(Ad ad) {
        this.ad = ad;
    }

    @Override
    public String toString() {
        return "ScreenSaverBean{" +
                "type=" + type +
                ", res=" + res +
                ", notice=" + notice +
                ", picture=" + picture +
                ", video=" + video +
                ", ad=" + ad +
                '}';
    }
}
