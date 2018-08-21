package com.bbtree.cardreader.entity.eventbus;

import java.io.Serializable;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/11/27
 * Create Time: 上午11:15
 */
public class TTSInfo implements Serializable {
    public String text;

    public TTSInfo() {
    }

    public TTSInfo(String text) {
        this.text = text;
    }
}
