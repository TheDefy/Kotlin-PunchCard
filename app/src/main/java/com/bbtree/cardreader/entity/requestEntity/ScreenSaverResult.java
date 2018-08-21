package com.bbtree.cardreader.entity.requestEntity;


import com.bbtree.cardreader.entity.BaseEntity;

import java.util.List;

/**
 * Created by Administrator on 2015/8/15.
 */
public class ScreenSaverResult extends BaseEntity {
    private List<Notices> notices;
    private List<Pictures> pictures;
    private List<Videos> videos;
    private List<Ad> ads;

    public List<Ad> getAds() {
        return ads;
    }

    public void setAds(List<Ad> ads) {
        this.ads = ads;
    }

    public List<Notices> getNotices() {
        return notices;
    }

    public void setNotices(List<Notices> notices) {
        this.notices = notices;
    }

    public List<Pictures> getPictures() {
        return pictures;
    }

    public void setPictures(List<Pictures> pictures) {
        this.pictures = pictures;
    }

    public List<Videos> getVideos() {
        return videos;
    }

    public void setVideos(List<Videos> videos) {
        this.videos = videos;
    }

    public class Notices {
        private String title; //通知标题
        private String content; //通知内容
        private String signature;
        private String noticeTime;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getNoticeTime() {
            return noticeTime;
        }

        public void setNoticeTime(String noticeTime) {
            this.noticeTime = noticeTime;
        }
    }

    public class Pictures {
        private String title; //图片标题
        private String url;  //图片url

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Videos {
        private String title; //视频标题
        private String url; //视频地址

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
