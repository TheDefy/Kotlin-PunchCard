package com.bbtree.cardreader.entity.eventbus;

/**
 * Created by zzz on 2017/1/4.
 */

public class CameraDegressEvent {
    private int degress;
    public CameraDegressEvent(int degress) {
        this.degress = degress;
    }

    public int getDegress() {
        return degress;
    }
}
