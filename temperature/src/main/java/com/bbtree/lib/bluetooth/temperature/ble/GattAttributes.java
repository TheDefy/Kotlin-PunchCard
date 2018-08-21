package com.bbtree.lib.bluetooth.temperature.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GattAttributes {
    public static HashMap<String, String> attributes = new HashMap<String, String>();
    public static BleCharacteristics ble_GAOMU = new BleCharacteristics();
    public static BleCharacteristics ble_RYCOM = new BleCharacteristics();

    public static final int DEVICE_GAOMU = 1;
    public static final int DEVICE_RYCOM = 2;
    public static int CONN_DEVICE = 0;

    public static String SERVICE_UUID = null;
    public static String READ_UUID = null;
    public static String WRITE_UUID = null;
    public final static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

//    public final static String JXB_182_SERVICE_UUID = "0000FE18-0000-1000-8000-00805F9B34FB";
//    public final static String JXB_182_READ_UUID = "0000FE10-0000-1000-8000-00805F9B34FB";
//    public final static String JXB_182_WRITE_UUID = "0000FE11-0000-1000-8000-00805F9B34FB";

    static {

        ble_GAOMU.services = "000018F0-0000-1000-8000-00805F9B34FB";
        ble_GAOMU.read = "00002AF0-0000-1000-8000-00805F9B34FB";
        ble_GAOMU.write = "00002AF1-0000-1000-8000-00805F9B34FB";

        ble_RYCOM.services = "0000FE18-0000-1000-8000-00805F9B34FB";
        ble_RYCOM.read = "0000FE10-0000-1000-8000-00805F9B34FB";
        ble_RYCOM.write = "0000FE11-0000-1000-8000-00805F9B34FB";
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static BluetoothGattCharacteristic lookup(List<BluetoothGattService> serviceList) {
        for (BluetoothGattService service : serviceList) {
            if (service.getUuid().equals(UUID.fromString(ble_GAOMU.services))) {
                //如果有gaomu service
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    //如果有gaomu  characteristic
                    if (characteristic.getUuid().equals(UUID.fromString(ble_GAOMU.read))) {
                        return characteristic;
                    }
                }
            }
        }
        return null;
    }

    public static void initCharacteristics(int index) {
        switch (index) {
            case 1:
                SERVICE_UUID = ble_GAOMU.services;
                READ_UUID = ble_GAOMU.read;
                WRITE_UUID = ble_GAOMU.write;
                break;
            case 2:
                SERVICE_UUID = ble_RYCOM.services;
                READ_UUID = ble_RYCOM.read;
                WRITE_UUID = ble_RYCOM.write;
                break;
        }
    }

    public static class BleCharacteristics {
        public String services;
        public String read;
        public String write;
    }
}
