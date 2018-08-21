package com.bbtree.lib.bluetooth.temperature.impl;


import com.bbtree.lib.bluetooth.temperature.beans.Temp;

/**
 * Function:
 * Created by BBTree Team
 * Author: LiuJihui
 * Create Date: 2015/10/27
 * Create Time: 15:06
 */
public interface TempListener {
    void temperature(Temp temp);
}
