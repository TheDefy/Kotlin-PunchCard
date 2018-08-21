package com.bbtree.cardreader.entity.eventbus;

/**
 *
 */
public class PauseMusicEvent {

    public PauseMusicType type;
    /**
     * 是否暂停音乐 true：暂停 false：续播
     */
    private boolean isPause;

    /**
     * 续播的延长时间
     */
    private long delayTime;

    /**
     * 是否停止service
     */
    private boolean isStopSelf;

    public enum PauseMusicType {
        pause,
        stopSelf
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public boolean isStopSelf() {
        return isStopSelf;
    }

    public void setStopSelf(boolean stopSelf) {
        isStopSelf = stopSelf;
    }

    public long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(long delayTime) {
        this.delayTime = delayTime;
    }

    @Override
    public String toString() {
        return "PauseMusicEvent{" +
                "isPause=" + isPause +
                ", isStopSelf=" + isStopSelf +
                ", delayTime=" + delayTime +
                '}';
    }
}
