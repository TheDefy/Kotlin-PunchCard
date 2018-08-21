package com.bbtree.lib.bluetooth.temperature.beans;

import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Function:
 * Created by BBTree Team
 * Author: LiuJihui
 * Create Date: 2015/10/28
 * Create Time: 18:22
 */
public class GattServices {
    private List<BluetoothGattService> services;

    public GattServices(List<BluetoothGattService> services) {
        this.services = services;
    }

    public List<BluetoothGattService> getServices() {
        return services;
    }
}
