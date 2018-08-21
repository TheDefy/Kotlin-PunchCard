package com.bbtree.cardreader.config;

import com.bbtree.baselib.utils.ListUtils;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.tts.Speaker;

import java.util.ArrayList;
import java.util.List;

/**
 * *
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/06/16
 * Time: 下午6:51
 * 设备默认配置,
 */
public class DeviceConfig{
    private static List<VoiceConfig> defaultVoice;

    static {
        defaultVoice = new ArrayList<>();

        VoiceConfig configMorning = new VoiceConfig();
        configMorning.setStart("00:00");
        configMorning.setEnd("08:59");
        configMorning.setTeacher(Constant.VoiceFormat.FamilyName + "老师早上好");
        configMorning.setStudent("早上好" + Constant.VoiceFormat.FullName);
        defaultVoice.add(configMorning);

        VoiceConfig configAM = new VoiceConfig();
        configAM.setStart("09:00");
        configAM.setEnd("10:59");
        configAM.setTeacher(Constant.VoiceFormat.FamilyName + "老师上午好");
        configAM.setStudent("上午好" + Constant.VoiceFormat.FullName);
        defaultVoice.add(configAM);

        VoiceConfig configNoon = new VoiceConfig();
        configNoon.setStart("11:00");
        configNoon.setEnd("12:59");
        configNoon.setTeacher(Constant.VoiceFormat.FamilyName + "老师中午好");
        configNoon.setStudent("中午好" + Constant.VoiceFormat.FullName);
        defaultVoice.add(configNoon);

        VoiceConfig configPM = new VoiceConfig();
        configPM.setStart("13:00");
        configPM.setEnd("17:59");
        configPM.setTeacher(Constant.VoiceFormat.FamilyName + "老师下午好");
        configPM.setStudent("下午好" + Constant.VoiceFormat.FullName);
        defaultVoice.add(configPM);

        VoiceConfig configNight = new VoiceConfig();
        configNight.setStart("18:00");
        configNight.setEnd("23:59");
        configNight.setTeacher(Constant.VoiceFormat.FamilyName + "老师晚上好");
        configNight.setStudent("晚上好" + Constant.VoiceFormat.FullName);
        defaultVoice.add(configNight);
    }

    public long configVersion;
    private String welcome;
    private long heartbeatInterval;
    private List<VoiceConfig> cardVoice;
    private boolean debug;
    private boolean flowerVisible;
    private boolean levelVisible;
    private int speaker;
    private Integer cameraPreviewAngle;//摄像头实时预览角度,90,180
    private Integer cameraSnapshotAngle;//摄像头拍照角度,90,180
    private Long screenSaverDelay;//进入屏保时间
    private Long screenSaverInterval;//屏保切换时间
    private int ttsSpeed;//语音速度
    private int messageFont;    //0-正常，1-小，2-大，3-超大

    public static List<VoiceConfig> getDefaultVoice() {
        return defaultVoice;
    }

    public long getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(long configVersion) {
        this.configVersion = configVersion;
    }

    public String getWelcome() {
        return welcome;
    }

    public void setWelcome(String welcome) {
        this.welcome = welcome;
    }

    public long getHeartbeatInterval() {
        if (heartbeatInterval == 0) {
            return 30 * 1000;
        }
        return heartbeatInterval * 1000;
    }

    public void setHeartbeatInterval(long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public List<VoiceConfig> getCardVoice() {
        if (cardVoice == null || ListUtils.isZero(cardVoice)) {
            return defaultVoice;
        }
        return cardVoice;
    }

    public void setCardVoice(List<VoiceConfig> cardVoice) {
        this.cardVoice = cardVoice;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isFlowerVisible() {
        return flowerVisible;
    }

    public void setFlowerVisible(boolean flowerVisible) {
        this.flowerVisible = flowerVisible;
    }

    public boolean isLevelVisible() {
        return levelVisible;
    }

    public void setLevelVisible(boolean levelVisible) {
        this.levelVisible = levelVisible;
    }

    public int getSpeaker() {
        if (speaker == 0) {
            speaker = Speaker.XunfeiXiaoyan;
        }
        return speaker;
    }

    public void setSpeaker(int speaker) {
        this.speaker = speaker;
    }

    public Integer getCameraPreviewAngle() {
        return cameraPreviewAngle;
    }

    public void setCameraPreviewAngle(Integer cameraPreviewAngle) {
        this.cameraPreviewAngle = cameraPreviewAngle;
    }

    public Integer getCameraSnapshotAngle() {
        return cameraSnapshotAngle;
    }

    public void setCameraSnapshotAngle(Integer cameraSnapshotAngle) {
        this.cameraSnapshotAngle = cameraSnapshotAngle;
    }

    public Long getScreenSaverDelay() {
        if (screenSaverDelay == null) {
            return Constant.ScreenSaver.SCREENSAVER_DELAY;
        }
        return screenSaverDelay;
    }

    public void setScreenSaverDelay(Long screenSaverDelay) {
        this.screenSaverDelay = screenSaverDelay;
    }

    public Long getScreenSaverInterval() {
        if (screenSaverInterval == null) {
            return Constant.ScreenSaver.SCREENSAVER_INTERVAL;
        }
        return screenSaverInterval;
    }

    public void setScreenSaverInterval(Long screenSaverInterval) {
        this.screenSaverInterval = screenSaverInterval;
    }

    public int getMessageFont() {
        return messageFont;
    }

    public void setMessageFont(int messageFont) {
        this.messageFont = messageFont;
    }

    public int getTtsSpeed() {
        return ttsSpeed;
    }

    public void setTtsSpeed(int ttsSpeed) {
        this.ttsSpeed = ttsSpeed;
    }
}