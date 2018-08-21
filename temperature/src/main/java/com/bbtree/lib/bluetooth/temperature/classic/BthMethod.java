package com.bbtree.lib.bluetooth.temperature.classic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;

import com.bbtree.lib.bluetooth.temperature.beans.Conn;
import com.bbtree.lib.bluetooth.temperature.beans.Temp;
import com.bbtree.lib.bluetooth.temperature.classic.broadcastreceiver.BluetoothBroadcastReceiver;
import com.bbtree.lib.bluetooth.temperature.impl.ConnStatusListener;
import com.bbtree.lib.bluetooth.temperature.impl.DeviceScanListener;
import com.bbtree.lib.bluetooth.temperature.impl.TempListener;
import com.bbtree.lib.bluetooth.temperature.utils.ChangeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


/**
 * Function: 初始化蓝牙 蓝牙主要功能
 * Created by BBTree Team
 * Author: LiuJihui
 * Create Date: 2015/10/27
 * Create Time: 11:37
 */
public class BthMethod {
    private String TAG = BthMethod.class.getSimpleName();

    private boolean mb_BTHConnectStatus = false;
    private static BthMethod mInstance = null;
    private Context mContext = null;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothDevice mBtDevice = null;
    private BluetoothSocket mBtSocket = null;
    private InputStream mInputStream = null;
    private ConnBluetoothThread connThread = null;
    private ReadThread readThread = null;
    private BluetoothBroadcastReceiver receiver = null;
    ////\\\\\/////////////////
    private ConnStatusListener mConnStatusListener = null;
    private TempListener mTempListener = null;
    private DeviceScanListener mDeviceScanListener = null;

    public void setmConnStatusListener(ConnStatusListener mConnStatusListener) {
        this.mConnStatusListener = mConnStatusListener;
        if (receiver != null) {
            receiver.setmConnStatusListener(mConnStatusListener);
        }
    }

    public void setmTempListener(TempListener mTempListener) {
        this.mTempListener = mTempListener;
    }

    public void setmDeviceScanListener(DeviceScanListener mDeviceScanListener) {
        this.mDeviceScanListener = mDeviceScanListener;
        if (receiver != null) {
            receiver.setmDeviceScanListener(mDeviceScanListener, this);
        }
    }

    public BluetoothAdapter getmBtAdapter() {
        return mBtAdapter;
    }

    public BluetoothDevice getmBtDevice() {
        return mBtDevice;
    }

    public boolean isMb_BTHConnectStatus() {
        return mb_BTHConnectStatus;
    }

    public void setMb_BTHConnectStatus(boolean mb_BTHConnectStatus) {
        this.mb_BTHConnectStatus = mb_BTHConnectStatus;
    }

    public static synchronized BthMethod getmInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BthMethod(context);
        }
        return mInstance;
    }

    public BthMethod(Context context) {
        mContext = context;
    }

    /**
     * 初始化经典蓝牙设配器 如果返回false 则蓝牙不能用 此方法中所有方法都不能调用
     *
     * @return
     */
    public boolean initClassicaBth() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            new IllegalArgumentException("BTH is not exit ! ");
            return false;
        }
        if (!mBtAdapter.isEnabled()) {
            mBtAdapter.enable(); // 开启蓝牙
        }
        registerReceiver();
        return true;
    }

    /**
     * 注册广播监听 蓝牙搜索结束和搜索到蓝牙设备的广播
     */
    public void registerReceiver() {
        receiver = new BluetoothBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //蓝牙搜索结束
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);  //蓝牙连接断开
        filter.addAction(BluetoothDevice.ACTION_FOUND);  //搜索到蓝牙设备
//        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);  //配对状态改变
//        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);   //自动配对广播
        mContext.registerReceiver(receiver, filter);
    }

    /**
     * 取消广播  在退出程序时候调用
     */
    public void unRegisterReceiver() {
        if (receiver != null) {
            mContext.unregisterReceiver(receiver);
            receiver = null;
        }

    }

    /**
     * 搜索蓝牙设备
     * ps:此搜索方法 在sdk 版本< 4.4 时候只能搜索到 经典蓝牙设备 > 4.4 时候能搜索到ble 设备和经典蓝牙设备
     * 可以通过api 19 新增加的方法 btDevice.getType() 方法来区分设备类型
     */
    public void searchBTH() {
        if (!mBtAdapter.isDiscovering() && !isMb_BTHConnectStatus()) {
            mBtAdapter.startDiscovery();
        }
    }

    /**
     * 取消搜索
     */
    public void cancleDiscovery() {
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
    }

    public void connect(BluetoothDevice device) {
        disconnect();
        mBtDevice = mBtAdapter.getRemoteDevice(device.getAddress());
        initSocketByMethod();
        connThread = new ConnBluetoothThread();
        connThread.start();
    }

    /**
     * 初始化蓝牙Socket
     */
    private void initSocketByMethod() {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                mBtSocket = mBtDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            }
        } catch (IOException e) {
            mBtSocket = null;
            e.printStackTrace();
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (connThread != null) {
            connThread.interrupt();
            connThread = null;
        }

        if (mb_BTHConnectStatus) {
            mb_BTHConnectStatus = false;
//            if (mConnStatusListener != null)
//                mConnStatusListener.getConnStatus(new Conn(Conn.ConnStatus.DISCONNECT, "断开连接"));
        }

        if (readThread != null) {
            readThread.interrupt();
            readThread = null;
        }
        if (mBtSocket != null) {
            try {
                mBtSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mBtSocket = null;
        }


        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //连接蓝牙的线程
    private class ConnBluetoothThread extends Thread {
        @Override
        public void run() {
            super.run();
            if (mBtSocket != null) {
                try {
                    if (mConnStatusListener != null)
                        mConnStatusListener.getConnStatus(new Conn(Conn.ConnStatus.CONNECTING, "正在连接"));
//                    EventBus.getDefault().post(new Conn(Conn.ConnStatus.CONNECTING, "正在连接"));
                    mBtSocket.connect();
                    //连接成功
                    mb_BTHConnectStatus = true;

                    if (mConnStatusListener != null)
                        mConnStatusListener.getConnStatus(new Conn(Conn.ConnStatus.CONNECTED, "连接成功"));
//                    EventBus.getDefault().post(new Conn(Conn.ConnStatus.CONNECTED, "连接成功"));
                    if (mb_BTHConnectStatus) {
                        readThread = new ReadThread();
                        readThread.start();
                    }
                } catch (Exception e) {
                    mb_BTHConnectStatus = false;
                    if (mConnStatusListener != null)
                        mConnStatusListener.getConnStatus(new Conn(Conn.ConnStatus.DISCONNECT, "连接失败"));
//                    EventBus.getDefault().post(new Conn(Conn.ConnStatus.DISCONNECT, "连接失败"));
                    e.printStackTrace();
                }
            }
        }

    }

    //监听蓝牙数据的线程
    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                mInputStream = mBtSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] buffer = new byte[100];
            int bytes = 0;

            while (mb_BTHConnectStatus && !interrupted()) {
                try {
                    if (mInputStream.available() > 0) {
                        if ((bytes = mInputStream.read(buffer)) > 0) {
                            byte[] byte_buf = new byte[bytes];
                            for (int i = 0; i < bytes; i++) {
                                byte_buf[i] = buffer[i];
                            }
                            //解析数据 发送
                            if (mTempListener != null) {
                                mTempListener.temperature(new Temp(ChangeUtils.getTemp(byte_buf), byte_buf));
                            }
//                        EventBus.getDefault().post(new Temp(ChangeUtils.getTemp(byte_buf), byte_buf));

                        }
                    }
                } catch (IOException e) {
                    //蓝牙连接断开
                    mb_BTHConnectStatus = false;
                    if (mConnStatusListener != null) {
                        mConnStatusListener.getConnStatus(new Conn(Conn.ConnStatus.DISCONNECT, "蓝牙断开"));
                    }
//                    EventBus.getDefault().post(new Conn(Conn.ConnStatus.DISCONNECT, "蓝牙断开"));
                    try {
                        mInputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }
        }
    }
}
