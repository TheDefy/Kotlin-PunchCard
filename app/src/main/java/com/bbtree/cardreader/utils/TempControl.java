package com.bbtree.cardreader.utils;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import com.bbtree.cardreader.config.TempConfig;
import com.bbtree.lib.bluetooth.temperature.beans.Conn;
import com.bbtree.lib.bluetooth.temperature.beans.GattServices;
import com.bbtree.lib.bluetooth.temperature.ble.BleMethod;
import com.bbtree.lib.bluetooth.temperature.ble.GattAttributes;
import com.bbtree.lib.bluetooth.temperature.classic.BthMethod;
import com.bbtree.lib.bluetooth.temperature.impl.ConnStatusListener;
import com.bbtree.lib.bluetooth.temperature.impl.DeviceScanListener;
import com.bbtree.lib.bluetooth.temperature.impl.DiscoverServiceListener;
import com.bbtree.lib.bluetooth.temperature.impl.TempListener;
import com.bbtree.lib.bluetooth.temperature.utils.ClsUtils;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Function: 提供对外调用
 * Created by BBTree Team
 * Author: LiuJihui
 * Create Date: 2015/10/28
 * Create Time: 17:25
 */
public class TempControl implements DeviceScanListener, ConnStatusListener, DiscoverServiceListener {

    public String TAG = TempControl.class.getSimpleName();

    private static TempControl mInstance = null;
    private boolean mb_BleOrBTH = false;   // false 经典   true ble;
    private Context mContext = null;   // 上下文
    private BthMethod mBthMethod = null;   //2.0 主要操作类
    private BleMethod mBleMethod = null;  //ble 主要操作类
    private BluetoothDevice device = null; //当前连接device
    private BluetoothAdapter mBtAdapter = null;
    private boolean connStatus = false; //当前连接状态
    Handler handler = new Handler();
    ///////////////////////////\\\\\\\\\\\\\\\\
    private ConnStatusListener connStatusListener = null;  //监听连接状态
    private TempListener tempListener = null;   //监听获取到温度
    /////\\\\\//////////////////////////////////
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();

    /**
     * @return false 经典蓝牙  true ble 设备
     */
    public boolean isMb_BleOrBTH() {
        return mb_BleOrBTH;
    }

    /**
     * @return 当前 或 最近连接的蓝牙设备
     */
    public BluetoothDevice getDevice() {
        return isMb_BleOrBTH() ? mBleMethod.getmBtDevice() : mBthMethod.getmBtDevice();
    }

    /**
     * @return 当前蓝牙连接状态
     */
    public boolean isConnStatus() {
        return isMb_BleOrBTH() ? mBleMethod.ismConnectionState() : mBthMethod.isMb_BTHConnectStatus();
    }

    /**
     * 设置蓝牙状态监听回调给界面
     *
     * @param connStatusListener
     */
    public void setConnStatusListener(ConnStatusListener connStatusListener) {
        this.connStatusListener = connStatusListener;
    }

    /**
     * 设置温度数据监听回调给界面
     *
     * @param tempListener
     */
    public void setTempListener(TempListener tempListener) {
        this.tempListener = tempListener;
    }

    /**
     * 构造方法
     *
     * @param context
     */
    public TempControl(Context context) {
        mContext = context.getApplicationContext();
    }

    public static synchronized TempControl getmInstance(Context mContext) {
        if (mInstance == null) {
            mInstance = new TempControl(mContext);
        }
        return mInstance;
    }

    /**
     * start方法  入口 负责获取adapter  打开蓝牙并开启搜索
     *
     * @return
     */
    public int start() {
        int start = 0;
        if (mBtAdapter != null && mBtAdapter.isEnabled()) {
            return 0;
        }
        switch (TempConfig.SDK_MODE) {
            case TempConfig.SdkMode.MODE_BLE:
                initBle();
                break;
            case TempConfig.SdkMode.MODE_CLASSIC:
                initBTH();
                break;
            case TempConfig.SdkMode.MODE_NONE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    // 4.3 以上 版本  使用 bleMethod
                    initBle();
                } else {
                    // 4.3 以下 版本  使用 BTHMethod
                    initBTH();
                }
                break;
        }

        if (mBtAdapter == null)
            return 0; // 不支持蓝牙

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                removeBond();
                devices.clear();
                if (mb_BleOrBTH) {
                    Logger.d(">>>开启搜索ble 设备");
                    mBleMethod.searchBleDevice(true);
                } else {
                    Logger.d(">>>开启搜索bth 设备");
                    mBthMethod.searchBTH();
                }
            }
        }, 4000);
        return 1;  //蓝牙开启ok
    }

    protected void initBle() {
        mBleMethod = BleMethod.getmInstance(mContext);
        mBleMethod.initialize();
        mBleMethod.setmDeviceScanListener(this);
        mBleMethod.setmConnStatusListener(this);
        mBleMethod.setmDiscoverServiceListener(this);
        mBleMethod.setmTempListener(tempListener);
        mBtAdapter = mBleMethod.getmBluetoothAdapter();
        mb_BleOrBTH = true;
    }

    protected void initBTH() {
        mBthMethod = BthMethod.getmInstance(mContext);
        mBthMethod.initClassicaBth();
        mBthMethod.setmDeviceScanListener(this);
        mBthMethod.setmTempListener(tempListener);
        mBthMethod.setmConnStatusListener(this);
        mBtAdapter = mBthMethod.getmBtAdapter();
        mb_BleOrBTH = false;
    }

    protected void removeBond() {
        if (mBtAdapter == null)
            return;
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
// If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                try {
                    ClsUtils.removeBond(device.getClass(), device);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        if (mBtAdapter == null) {
            return;
        }
        if (mb_BleOrBTH) {
            devices.clear();
            mBleMethod.searchBleDevice(false);
            mBleMethod.disconnect();

        } else {
            devices.clear();
            mBthMethod.unRegisterReceiver();
            mBthMethod.cancleDiscovery();
            mBthMethod.disconnect();
        }
        if (mBtAdapter != null) {
            mBtAdapter.disable();
            mBtAdapter = null;
        }
    }

    private void connect(BluetoothDevice device) {
        if (!isConnStatus()) {
            Logger.d(">>>尝试连接");
            if (mb_BleOrBTH) {
                mBleMethod.searchBleDevice(false);
                mBleMethod.connect(device);
            } else {
                mBthMethod.cancleDiscovery();
                mBthMethod.connect(device);
            }
        }

    }


    /**
     * 发现设备
     *
     * @param device
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void found(final BluetoothDevice device) {
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (mb_BleOrBTH) {
                    int type = device.getType();
                    if (type == BluetoothDevice.DEVICE_TYPE_LE || type == BluetoothDevice.DEVICE_TYPE_DUAL) {
                        if (!devices.contains(device)) {
                            devices.add(device);
                            Logger.d(">>>添加设备的名称+ " + device.getName());
                            //E1:42:8C:C5:9A:6C   JXB_TTM_42E1 BF4030
                            if (device.getName() != null && device.getName().equals("BF4030")) {
                                connect(device);
                            }
                        }
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        int type = device.getType();
                        if (type == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                            if (!devices.contains(device)) {
                                devices.add(device);
                                Logger.d(">>>添加设备的名称classica+ " + device.getName());
                                if (device.getName() != null && device.getName().equals("BF4030")) {
                                    connect(device);
                                }
                            }
                        }
                    } else {
                        if (!devices.contains(device)) {
                            devices.add(device);
                            Logger.d(">>>添加设备的名称classica+ " + device.getName());
                            if (device.getName() != null && device.getName().equals("BF4030")) {
                                connect(device);
                            }
                        }
                    }

                }
            }
        });
    }

    /**
     * 搜索结束
     */
    @Override
    public void finish() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isConnStatus()) {
                    if (mb_BleOrBTH) {
                        Logger.d("一次搜索结束清空缓存");
                        devices.clear();
                        mBleMethod.searchBleDevice(true);
                    } else {
                        devices.clear();
                        mBthMethod.searchBTH();
                    }
                }
            }
        }, 5000);
    }

    /**
     * 连接状态
     *
     * @param conn
     */
    @Override
    public void getConnStatus(final Conn conn) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (connStatusListener != null) {
                    connStatusListener.getConnStatus(conn);
                }
                if (conn.getStatus() == Conn.ConnStatus.DISCONNECT) {
                    if (mb_BleOrBTH) {
                        Logger.d(">>>开启搜索ble 设备");
                        devices.clear();
                        mBleMethod.searchBleDevice(true);
                    } else {
                        Logger.d(">>>开启搜索bth 设备");
                        devices.clear();
                        mBthMethod.searchBTH();
                    }
                }
            }
        });

    }

    /**
     * 连接ble 设备以后获取service
     *
     * @param services
     */
    @Override
    public void discoverService(final GattServices services) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isConnStatus()) {
                    //判断是否有characteristic
                    List<BluetoothGattService> gattServices = services.getServices();
                    BluetoothGattCharacteristic characteristic = GattAttributes.lookup(gattServices);
                    if (characteristic != null) {
                        mBleMethod.setCharacteristicNotification(characteristic, true);
                    } else {
                        mBleMethod.disconnect();
                    }
                }
            }
        });

    }

}
