package com.bbtree.cardreader.entity.requestEntity;

import com.bbtree.cardreader.entity.BaseEntity;

import java.util.List;

/**
 * 考勤机获取音箱配置接口返回结果
 */
public class SpeakerConfigResult extends BaseEntity {

    private List<SpeakerBean> speakers;
    private ConfigBean config;

    public List<SpeakerBean> getSpeakers() {
        return speakers;
    }

    public void setSpeakers(List<SpeakerBean> speakers) {
        this.speakers = speakers;
    }

    public ConfigBean getConfig() {
        return config;
    }

    public void setConfig(ConfigBean config) {
        this.config = config;
    }
}
