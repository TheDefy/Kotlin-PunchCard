package com.bbtree.cardreader.entity.eventbus;

/**
 * VerticalMarqueeTextView滚动到底端事件
 */
public class ScreenSaverCircleEvent {

    private long circleDelayTime;

    public long getCircleDelayTime() {
        return circleDelayTime;
    }

    public void setCircleDelayTime(long circleDelayTime) {
        this.circleDelayTime = circleDelayTime;
    }
}
