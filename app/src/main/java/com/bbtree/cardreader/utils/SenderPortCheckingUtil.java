package com.bbtree.cardreader.utils;

import android.os.Build;
import android.text.TextUtils;

import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.entity.ClassSpeakerRequest;
import com.bbtree.cardreader.entity.eventbus.ClassSpeakerResponse;
import com.bbtree.cardreader.serialport.SerialPort;
import com.bbtree.cardreader.serialport.SerialPortFinder;
import com.bbtree.childservice.utils.HexConver;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SenderPortCheckingUtil {

    public ClassSpeakerRequest request;
    public ClassSpeakerResponse response;
    public OutputStream mOutputStream;

    private SenderPortCheckingUtil() {
        request = new ClassSpeakerRequest(1, 0);
        response = request.getResponse();

        // 智趣
        if (Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V4) || Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V5)) {
            ExecutorService serialPortThreadPool = Executors.newFixedThreadPool(2);
            serialPortThreadPool.execute(new SenderCheckingThread("/dev/ttyS2"));
            return;
        }

        // 获取到所有驱动路径
        String[] allDevicesPath = new SerialPortFinder().getAllDevicesPath();
        int coreNum = ReadPhoneInfo.getCPUCoreNums();
        ExecutorService serialPortThreadPool = Executors.newFixedThreadPool(allDevicesPath.length + (coreNum <= 2 ? 1 : coreNum / 2));

        String readerPath = SPUtils.getReaderPath("");
        for (String devicePath : allDevicesPath) {
            // 如果是读卡器路径则不监听
            if (!TextUtils.isEmpty(readerPath) && readerPath.equals(devicePath)) {
                continue;
            }
            serialPortThreadPool.execute(new SenderCheckingThread(devicePath));
        }
    }

    private static class SenderPortUtilHolder {
        private static SenderPortCheckingUtil instance = new SenderPortCheckingUtil();
    }

    public static SenderPortCheckingUtil getInstance() {
        return SenderPortUtilHolder.instance;
    }

    /**
     * 监听线程
     */
    private class SenderCheckingThread extends Thread {
        String serialPortName;

        SenderCheckingThread(String deviceName) {
            serialPortName = deviceName;
        }

        @Override
        public void run() {
            SerialPort mSerialPort = null;
            try {
                SerialPortUtils serialPortUtils = SerialPortUtils.newInstance();
                mSerialPort = serialPortUtils.getSerialPort(115200, serialPortName);
                final String T = serialPortName;
                InputStream inputStream = mSerialPort.getInputStream();
                OutputStream outputStream = mSerialPort.getOutputStream();
                // 发射器握手指令
                outputStream.write(request.getmCMDBytes());
                while (!Thread.currentThread().isInterrupted()) {
                    int size;
                    byte[] buffer = new byte[512];
                    size = inputStream.read(buffer);
                    if (size > 0) {
                        String originalResult = HexConver.byte2HexStr(buffer, buffer.length);
                        Logger.t(T).i("serialPortName:" + serialPortName + " originalResult:" + originalResult);
                        byte[] data = new byte[size];
                        System.arraycopy(buffer, 0, data, 0, size);
                        String dataResult = HexConver.byte2HexStr(data, data.length);
                        Logger.t(T).i("serialPortName:" + serialPortName + " dataResult:" + dataResult);
                        // 发射器接收消息成功
                        if (originalResult.startsWith(response.getHexCMDResult()) || (response != null && response.getHexCMDResult().contains(dataResult))) {
                            // 保存发射器路径
                            mOutputStream = outputStream;
                            SPUtils.setSenderPath(serialPortName);
                            Logger.t(T).i("save SENDER_PATH in sp:" + serialPortName);
                            EventBus.getDefault().post(response);
                        }
                    }
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Thread.currentThread().interrupt();
                if (mSerialPort != null) {
                    mSerialPort.close();
                    mSerialPort = null;
                }
            }
        }
    }

}
