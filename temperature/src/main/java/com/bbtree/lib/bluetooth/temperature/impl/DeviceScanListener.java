package com.bbtree.lib.bluetooth.temperature.impl;

import android.bluetooth.BluetoothDevice;

/**
 * Function:
 * Created by BBTree Team
 * Author: LiuJihui
 * Create Date: 2015/10/30
 * Create Time: 11:48
 */
public interface DeviceScanListener {

    void found(BluetoothDevice device);

    void finish();


}
