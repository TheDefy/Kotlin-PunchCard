package com.bbtree.lib.bluetooth.temperature.impl;


import com.bbtree.lib.bluetooth.temperature.beans.GattServices;

/**
 * Function:
 * Created by BBTree Team
 * Author: LiuJihui
 * Create Date: 2015/10/30
 * Create Time: 14:01
 */
public interface DiscoverServiceListener {
    void discoverService(GattServices services);
}
