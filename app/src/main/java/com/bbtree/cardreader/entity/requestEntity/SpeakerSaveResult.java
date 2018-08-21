package com.bbtree.cardreader.entity.requestEntity;

import com.bbtree.cardreader.entity.BaseEntity;

/**
 * 考勤机保存音箱信息接口请求返回结果
 */
public class SpeakerSaveResult extends BaseEntity {
    private int speakerId;

    public int getSpeakerId() {
        return speakerId;
    }

    public void setSpeakerId(int speakerId) {
        this.speakerId = speakerId;
    }
}
