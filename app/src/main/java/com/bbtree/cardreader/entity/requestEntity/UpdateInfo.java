package com.bbtree.cardreader.entity.requestEntity;


import com.bbtree.cardreader.entity.BaseEntity;

public class UpdateInfo extends BaseEntity {

    public Update data;

    /**
     * buildDate : 1490803200000
     * changeLog : 看门狗正式-1.6更新说明
     * delayInstall : false
     * downloadUrl : http://dldir.bbtree.com/mobile/watchdog_release_7.apk
     * fileName : dldir.bbtree.com/mobile/watchdog_release_7.apk
     * foundNewVersion : true
     * md5Sum : 0ddb7d8e124203ebec5366eebf9e36cd
     * silentInstall : false
     * uiName : 看门狗正式-1.6提示
     */

    public class Update {

        private String buildDate;
        private String changeLog;
        private Integer delayInstall;
        private String downloadUrl;
        private String fileName;
        private boolean foundNewVersion;
        private String md5Sum;
        private Integer silentInstall;
        private String uiName;
        private String localPath;

        public String getLocalPath() {
            return localPath;
        }

        public void setLocalPath(String localPath) {
            this.localPath = localPath;
        }

        public String getBuildDate() {
            return buildDate;
        }

        public void setBuildDate(String buildDate) {
            this.buildDate = buildDate;
        }

        public String getChangeLog() {
            return changeLog;
        }

        public void setChangeLog(String changeLog) {
            this.changeLog = changeLog;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public boolean isFoundNewVersion() {
            return foundNewVersion;
        }

        public void setFoundNewVersion(boolean foundNewVersion) {
            this.foundNewVersion = foundNewVersion;
        }

        public String getMd5Sum() {
            return md5Sum;
        }

        public void setMd5Sum(String md5Sum) {
            this.md5Sum = md5Sum;
        }

        public Integer getSilentInstall() {
            return silentInstall;
        }

        public void setSilentInstall(Integer silentInstall) {
            this.silentInstall = silentInstall;
        }

        public Integer getDelayInstall() {

            return delayInstall;
        }

        public void setDelayInstall(Integer delayInstall) {
            this.delayInstall = delayInstall;
        }

        public String getUiName() {
            return uiName;
        }

        public void setUiName(String uiName) {
            this.uiName = uiName;
        }


    }
}