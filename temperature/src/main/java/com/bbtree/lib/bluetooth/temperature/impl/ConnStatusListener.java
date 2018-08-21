package com.bbtree.lib.bluetooth.temperature.impl;


import com.bbtree.lib.bluetooth.temperature.beans.Conn;

/**
 * Function:
 * Created by BBTree Team
 * Author: LiuJihui
 * Create Date: 2015/10/27
 * Create Time: 15:05
 */
public interface ConnStatusListener {
    void getConnStatus(Conn status);
}
