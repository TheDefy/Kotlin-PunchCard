package com.bbtree.cardreader.entity.eventbus;

import java.io.Serializable;

/**
 *
 */
public class ScreenSaverUPanEvent implements Serializable {
    /**
     * 是否删除视频 true： 是，拔出u盘 删除视频  false
     */
    private boolean isDelete;

    /**
     * 进入轮播图的时间(u盘插拔时)
     */
    private long toScreenDelay;

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public long getToScreenDelay() {
        return toScreenDelay;
    }

    public void setToScreenDelay(long toScreenDelay) {
        this.toScreenDelay = toScreenDelay;
    }

    @Override
    public String toString() {
        return "ScreenSaverUPanEvent{" +
                "isDelete=" + isDelete +
                ", toScreenDelay=" + toScreenDelay +
                '}';
    }
}
