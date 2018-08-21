package com.bbtree.lib.bluetooth.temperature.classic.broadcastreceiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bbtree.lib.bluetooth.temperature.beans.Conn;
import com.bbtree.lib.bluetooth.temperature.classic.BthMethod;
import com.bbtree.lib.bluetooth.temperature.impl.ConnStatusListener;
import com.bbtree.lib.bluetooth.temperature.impl.DeviceScanListener;
import com.orhanobut.logger.Logger;


/**
 * Function:  接收蓝牙搜索结果、蓝牙搜索结束的广播
 * Created by BBTree Team
 * Author: LiuJihui
 * Create Date: 2015/10/23
 * Create Time: 14:34
 */
public class BluetoothBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = BluetoothBroadcastReceiver.class.getSimpleName();
    private DeviceScanListener mDeviceScanListener = null;
    private ConnStatusListener mConnStatusListener = null;
    private BthMethod mControl = null;

    public void setmConnStatusListener(ConnStatusListener mConnStatusListener) {
        this.mConnStatusListener = mConnStatusListener;
    }

    public void setmDeviceScanListener(DeviceScanListener mDeviceScanListener, BthMethod control) {
        this.mDeviceScanListener = mDeviceScanListener;
        mControl = control;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
            //蓝牙搜索结束
            if (mDeviceScanListener != null) {
                mDeviceScanListener.finish();
            }
            Logger.t(TAG).d("蓝牙搜索结束", "蓝牙搜索结束");
        } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
            //搜索到蓝牙设备
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null) {
                if (mDeviceScanListener != null) {
                    mDeviceScanListener.found(device);
                }
            }
        } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            if (mControl.isMb_BTHConnectStatus() && mConnStatusListener != null) {
                mConnStatusListener.getConnStatus(new Conn(Conn.ConnStatus.DISCONNECT, "蓝牙断开"));
                mControl.setMb_BTHConnectStatus(false);
                mControl.disconnect();
            }

        }
    }
}
