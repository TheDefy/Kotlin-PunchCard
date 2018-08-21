package com.bbtree.cardreader.utils;


import android.os.Build;
import android.text.TextUtils;

import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.serialport.SerialPort;
import com.bbtree.cardreader.serialport.SerialPortFinder;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/05/20
 * Time: 下午5:06
 */
public class SerialPortUtils {

    private String TAG = "SerialPortUtils";

    private SerialPort mSerialPort;

    private SerialPortUtils() {
    }

    public static SerialPortUtils newInstance() {
        return new SerialPortUtils();
    }

    //打开串口
    public SerialPort getSerialPort(int baudRate, String device) throws SecurityException, IOException,
            InvalidParameterException {
        // M0,M1,M2默认串口号为ttyS7,M3串口号是ttyS5
        //mSerialPort = new SerialPort(new File("/dev/ttyS7"), 9600, 0);
        // 默认串口是ttyS2 波特率是115200
        mSerialPort = new SerialPort(new File(device), baudRate, 0);
        return mSerialPort;
    }

    //关闭串口
    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    /**
     * 获取所有串口,并不包含发射器路径
     */
    public static List<String> getAllSerailPorts() {
        List<String> list = new ArrayList<>();
        // 智趣单独逻辑
        if (Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V4) || Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V5)) {
            boolean iqeqDoubleReader = false;
            ShellUtils.CommandResult result = ShellUtils.execCommand("cat /sys/class/rksn/sn", true);
            if (!TextUtils.isEmpty(result.successMsg) && result.successMsg.length() > 6) {
                char c = result.successMsg.charAt(result.successMsg.length() - 6);
                if (String.valueOf(c).equals("W")) {
                    iqeqDoubleReader = true;
                }
            }
            String[] strings = new SerialPortFinder().getAllDevicesPath();
            if (strings != null) {
                for (String str : strings) {
                    if (str.equals("/dev/ttyS3")) {
                        list.add(str);
                    } else if (str.equals("/dev/ttyS2") && iqeqDoubleReader) {
                        list.add(str);
                    }
                }
            }
        }
        // 其他板子
        else {
//            String[] strings = new SerialPortFinder().getAllDevicesPath();
//            if (strings != null) {
//                for (String str : strings) {
//                    if (str.startsWith("/dev/ttyS") || str.startsWith("/dev/ttyUSB") || str.startsWith("/dev/ttySAC")) {
//                        //
//                        if (str.equals("/dev/ttyS0") && !Build.MODEL.equals(Constant.PlatformAdapter.CS3Plus)) {
//                            if (!Build.MODEL.equals(Constant.PlatformAdapter.M3X_A64)) {
//                                continue;
//                            }
//                        }
//                        // A64预留蓝牙
//                        if (str.equals("/dev/ttyS1") && Build.MODEL.equals(Constant.PlatformAdapter.M3X_A64)) {
//                            continue;
//                        }
//                        list.add(str);
//                    }
//                }
//            }
            // 智趣盒子走之前的逻辑
            if (Build.MODEL.equals(Constant.PlatformAdapter.Snobs)) {
                String[] strings = new SerialPortFinder().getAllDevicesPath();
                if (strings != null) {
                    for (String str : strings) {
                        if (str.startsWith("/dev/ttyS") || str.startsWith("/dev/ttyUSB") || str.startsWith("/dev/ttySAC")) {
                            list.add(str);
                        }
                    }
                }
            }
            // 其他走新逻辑
            else {
                String readerPath = SPUtils.getReaderPath("");
                if (!TextUtils.isEmpty(readerPath)) list.add(readerPath);
            }
        }
        for (String s : list) {
            com.orhanobut.logger.Logger.i("listen serial port:" + s);
        }
        return list;
    }

    /**
     * 获取波率
     */
    public static int getBaudRate() {
        int baudRate = SPUtils.getBaudRate(115200);
        if (baudRate < 1) {
            baudRate = 115200;
        }
        if (Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V5)
                || Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V4)
                || Build.MODEL.equals(Constant.PlatformAdapter.Squid)
                || Build.MODEL.equals(Constant.PlatformAdapter.Scallops)
                || Build.MODEL.equals(Constant.PlatformAdapter.Peas)
                || Build.MODEL.equals(Constant.PlatformAdapter.Donkey)) {
            baudRate = 9600;//强制9600
        }

        return baudRate;
    }

}
