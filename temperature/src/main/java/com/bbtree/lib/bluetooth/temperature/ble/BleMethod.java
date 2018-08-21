package com.bbtree.lib.bluetooth.temperature.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;

import com.bbtree.lib.bluetooth.temperature.R;
import com.bbtree.lib.bluetooth.temperature.beans.Conn;
import com.bbtree.lib.bluetooth.temperature.beans.GattServices;
import com.bbtree.lib.bluetooth.temperature.beans.Temp;
import com.bbtree.lib.bluetooth.temperature.impl.ConnStatusListener;
import com.bbtree.lib.bluetooth.temperature.impl.DeviceScanListener;
import com.bbtree.lib.bluetooth.temperature.impl.DiscoverServiceListener;
import com.bbtree.lib.bluetooth.temperature.impl.TempListener;
import com.bbtree.lib.bluetooth.temperature.utils.ChangeUtils;
import com.orhanobut.logger.Logger;

import java.util.List;
import java.util.UUID;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleMethod {
    private final String TAG = BleMethod.class.getSimpleName();
    private static BleMethod mInstance = null;
    private final int SEARCH_TIMER = 1000 * 10;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mBluetoothGatt;
    private Context mContext = null;
    private Handler mHandler = null;
    private boolean searching = false;
    private BluetoothDevice mBtDevice = null;

    public boolean mConnectionState = false;
    /////////////\\\\\\\\\\\\\\///////////////
    private ConnStatusListener mConnStatusListener = null;
    private TempListener mTempListener = null;
    private DiscoverServiceListener mDiscoverServiceListener = null;
    private DeviceScanListener mDeviceScanListener = null;
    ///////////\\\\\\\\\\\\\\\\\\\//////////////////////////////


    public void setmConnStatusListener(ConnStatusListener mConnStatusListener) {
        this.mConnStatusListener = mConnStatusListener;
    }

    public void setmTempListener(TempListener mTempListener) {
        this.mTempListener = mTempListener;
    }

    public void setmDiscoverServiceListener(DiscoverServiceListener mDiscoverServiceListener) {
        this.mDiscoverServiceListener = mDiscoverServiceListener;
    }

    public void setmDeviceScanListener(DeviceScanListener mDeviceScanListener) {
        this.mDeviceScanListener = mDeviceScanListener;
    }

    public boolean ismConnectionState() {
        return mConnectionState;
    }

    private void setmConnectionState(boolean mConnectionState) {
        this.mConnectionState = mConnectionState;
    }

    public BluetoothDevice getmBtDevice() {
        return mBtDevice;
    }

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    // 搜索蓝牙设备回调
    private LeScanCallback mLeScanCallback = new LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//            Log.e(TAG , );
            //EventBus.getDefault().post(device);
            if (mDeviceScanListener != null) {
                mDeviceScanListener.found(device);
            }
        }
    };

    // 链接状态等系统回调
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            // ble connect status

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Logger.t(TAG).d(">>> ble connected");
                mBluetoothGatt.discoverServices();
//                EventBus.getDefault().post(new Conn(Conn.ConnStatus.CONNECTED, mContext.getString(R.string.str_connected)));
                if (mConnStatusListener != null) {
                    mConnStatusListener.getConnStatus(new Conn(Conn.ConnStatus.CONNECTED, mContext.getString(R.string.ble_temp_connected)));
                }
                setmConnectionState(true);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Logger.t(TAG).d(">>> ble disconnected");
                setmConnectionState(false);
//                EventBus.getDefault().post(new Conn(Conn.ConnStatus.DISCONNECT, mContext.getString(R.string.str_disconnect)));
                if (mConnStatusListener != null) {
                    mConnStatusListener.getConnStatus(new Conn(Conn.ConnStatus.DISCONNECT, mContext.getString(R.string.ble_temp_disconnect)));
                }

            }
        }


        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //services is discovered
//            EventBus.getDefault().post(new GattServices(getSupportedGattServices()));
            if (mDiscoverServiceListener != null) {
                mDiscoverServiceListener.discoverService(new GattServices(getSupportedGattServices()));
            }
        }


        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // receive data[]
            byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
//                EventBus.getDefault().post(new Temp(ChangeUtils.getTemp(data), data));
                if (mTempListener != null) {
                    mTempListener.temperature(new Temp(ChangeUtils.getTemp(data), data));
                }
            }

        }
    };

    public BleMethod(Context mContext) {
        this.mContext = mContext;
        mHandler = new Handler(mContext.getMainLooper());
    }

    public static synchronized BleMethod getmInstance(Context mContext) {
        if (mInstance == null) {
            mInstance = new BleMethod(mContext);
        }
        return mInstance;
    }

    /**
     * 初始化 蓝牙
     *
     * @return
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (!mContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            // ! android 4.3
            Logger.t(TAG).d(">>>SDK version  less than  4.3");
            return false;
        }
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Logger.t(TAG).d(">>>mBluetoothManager is null");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Logger.t(TAG).d(">>>mBluetoothAdapter is null");
            return false;
        }

        return true;
    }

    /**
     * 搜索蓝牙
     *
     * @param flag
     */
    public void searchBleDevice(final boolean flag) {
        if (!getBleState()) {
            Logger.t(TAG).d("ble state is not turn on!");
            return;
        }
        if (flag) {
            searching = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            new CountDownTimer(SEARCH_TIMER, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    if (searching) {
                        searching = false;
                        Logger.t(TAG).d("ble 蓝牙搜索结束");
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                        EventBus.getDefault().post(new DiscoveryFinish(true));
                        if (mDeviceScanListener != null) {
                            mDeviceScanListener.finish();
                        }
                    }

                }
            }.start();

        } else {
            if (searching) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                searching = false;
            }
        }
    }

    /**
     * 链接蓝牙
     *
     * @param dev 蓝牙设备
     * @return
     */
    public boolean connect(final BluetoothDevice dev) {
        disconnect();
        if (mBluetoothAdapter == null || dev == null) {
            Logger.t(TAG).w("BluetoothAdapter not initialized or not support ble");
            return false;
        }

        //连接上次已连接过的蓝牙设备
        // Previously connected device.  Try to reconnect.
//	        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
//	                && mBluetoothGatt != null) {
//	            LOG.t(TAG).d( "Trying to use an existing mBluetoothGatt for connection.");
//	            if (mBluetoothGatt.connect()) {
//	                mConnectionState = STATE_CONNECTING;
//	                return true;
//	            } else {
//	                return false;
//	            }
//	        }
        String address = dev.getAddress();
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Logger.t(TAG).w("Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        Logger.t(TAG).d("Trying to create a new connection.");
        mBtDevice = device;
        setmConnectionState(true);
        return true;
    }

    /**
     * 断开蓝牙链接
     */
    public void disconnect() {
        if (searching) {
            searchBleDevice(false);
        }
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Logger.t(TAG).w("BluetoothAdapter not initialized");
            return;
        }
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

    }


    /**
     * 改变接收状态
     *
     * @param enabled
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic mBtGattCharacteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Logger.t(TAG).w("BluetoothAdapter not initialized");
            return;
        }
//        BluetoothGattService mBtGattService = mBluetoothGatt.getService(UUID.fromString(GattAttributes.SERVICE_UUID));
//        if (mBtGattService == null) {
//            LOG.t(TAG).d( "mBtGattCharacteristic not exist");
//            return;
//        }
//        BluetoothGattCharacteristic mBtGattCharacteristic = mBtGattService.getCharacteristic(UUID.fromString(GattAttributes.READ_UUID));
        mBluetoothGatt.setCharacteristicNotification(mBtGattCharacteristic, enabled);

//        if (UUID_HEART_RATE_MEASUREMENT.equals(mBtGattCharacteristic.getUuid())) {
        BluetoothGattDescriptor descriptor = mBtGattCharacteristic.getDescriptor(
                UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
//        }
    }


    /**
     * 获取 service 列表
     *
     * @return
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    private boolean getBleState() {
        return (mBluetoothAdapter != null && mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON);
    }

}
