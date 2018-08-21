package com.bbtree.lib.bluetooth.temperature.beans;

/**
 * Function:
 * Created by BBTree Team
 * Author: LiuJihui
 * Create Date: 2015/10/27
 * Create Time: 22:27
 */
public class DiscoveryFinish {
    private boolean discoverFinish;

    public DiscoveryFinish(boolean discoverFinish) {
        this.discoverFinish = discoverFinish;
    }

    public boolean isDiscoverFinish() {
        return discoverFinish;
    }
}
