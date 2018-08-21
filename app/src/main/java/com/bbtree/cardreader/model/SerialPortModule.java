package com.bbtree.cardreader.model;

import android.os.Build;
import android.text.TextUtils;

import com.bbtree.baselib.net.GsonParser;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.entity.CardPath;
import com.bbtree.cardreader.entity.dao.CardInfo;
import com.bbtree.cardreader.entity.dao.CardRecord;
import com.bbtree.cardreader.entity.eventbus.ListenResultType;
import com.bbtree.cardreader.entity.eventbus.SwipeCardInfo;
import com.bbtree.cardreader.serialport.SerialPort;
import com.bbtree.cardreader.serialport.SerialPortFinder;
import com.bbtree.cardreader.utils.SPUtils;
import com.bbtree.cardreader.utils.SerialPortUtils;
import com.bbtree.childservice.utils.CardNumberBuildUtils;
import com.bbtree.childservice.utils.HexConver;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by zhouyl on 10/04/2017.
 */

public class SerialPortModule {

    static SerialPortModule instance;

    public static SerialPortModule getInstance() {
        if (instance == null) {
            instance = new SerialPortModule();
        }
        return instance;
    }

    private SerialPortModule() {
    }

    /**
     * 刷卡信息队列
     */
    private static LinkedBlockingQueue<CardPath> cardQueue;

    /**
     * 上一个刷卡数据
     */
    private CardPath lastCardPath = new CardPath();
    /**
     * 上一个刷卡时间
     */
    private long lastCardPathTime = 0;

    public void listenAllPorts() {
        // 智趣串口是固定的,智趣盒子是双读卡器
        if (Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V4)
                || Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V5)
                || Build.MODEL.equals(Constant.PlatformAdapter.Snobs)) {
            EventBus.getDefault().post(new ListenResultType(1));
            return;
        }

        // 读卡器路径
        String readerPath = SPUtils.getReaderPath("");
        Logger.i("readerPath:" + readerPath);

        if (!TextUtils.isEmpty(readerPath)) {
            EventBus.getDefault().post(new ListenResultType(1));
            return;
        }

        EventBus.getDefault().post(new ListenResultType(2));
        cardQueue = new LinkedBlockingQueue<>(2);

        // 获取到所有驱动路径
        String[] allDevicesPath = new SerialPortFinder().getAllDevicesPath();
        ExecutorService serialPortThreadPool = Executors.newFixedThreadPool(allDevicesPath.length + 1);

        for (String devicePath : allDevicesPath) {
            serialPortThreadPool.execute(new SerialPortThread(devicePath));
        }
        serialPortThreadPool.execute(new SendCardIDRunnable());
    }

    /**
     * 监听线程
     */
    private class SerialPortThread extends Thread {
        String serialPortName;

        SerialPortThread(String deviceName) {
            serialPortName = deviceName;
        }

        @Override
        public void run() {
            final String T = serialPortName;
            Logger.t(T).i("serialPortName:" + serialPortName);
            long coastStart = System.currentTimeMillis();
            SerialPort mSerialPort = null;
            try {
                int baudRate = SerialPortUtils.getBaudRate();
                String protocolJsonV2 = SPUtils.getRFID_ProtocolV2(null);
                Integer[] protocolItem;
                if (!TextUtils.isEmpty(protocolJsonV2)) {
                    protocolItem = GsonParser.parse2Entity(protocolJsonV2, Integer[].class);
                    if (protocolItem == null) {
                        protocolItem = new Integer[]{8};
                    }
                } else {
                    protocolItem = new Integer[]{8};
                }
                SerialPortUtils serialPortUtils = SerialPortUtils.newInstance();
                mSerialPort = serialPortUtils.getSerialPort(baudRate, serialPortName);
                InputStream inputStream = mSerialPort.getInputStream();
                OutputStream outputStream = mSerialPort.getOutputStream();
                // 有些读卡器的头只有握手才能刷卡,握手一次
                outputStream.write(HexConver.hexStr2Bytes("08ACFFFFFFFFFFFF00"));
                while (!Thread.currentThread().isInterrupted()) {
                    int size;
                    byte[] cardData = new byte[48 + 5];//扇区数据.三个块的数据。5为读卡器增加的开头
                    if (inputStream == null) {
                        break;
                    }
                    size = readInputStreamStartWithTimeout(inputStream, cardData, 50);
                    if (size == 0) {
                        continue;
                    }
                    byte[] tempResult = new byte[size];
                    System.arraycopy(cardData, 0, tempResult, 0, size);
                    cardData = tempResult;
                    if (size > 0) {
                        String originalResult = HexConver.byte2HexStr(cardData, cardData.length);
                        // 读卡器握手成功
                        if ("5A5948010103".equals(originalResult)) {
                            SPUtils.setReaderPath(serialPortName);
                            String msg = "读卡器串口:" + serialPortName + " 耗时:" + (System.currentTimeMillis() - coastStart) + " MODEL" + Build.MODEL;
                            SPUtils.setLoadingConfig(msg);
                            EventBus.getDefault().post(new ListenResultType(3));
                            break;
                        }
                        long[] probablyResult = new long[protocolItem.length];
                        for (int i = 0; i < protocolItem.length; i++) {
                            long result = tryToSumNO(protocolItem[i], cardData);
                            if (result > 0) {
                                probablyResult[i] = result;
                            }
                        }
                        CardPath cp = new CardPath();
                        cp.setCardNo(probablyResult);
                        cp.setPath(serialPortName);
                        boolean cardQueueResult = cardQueue.offer(cp);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                if (mSerialPort != null) {
                    mSerialPort.close();
                    mSerialPort = null;
                }
            }
        }
    }

    public int readInputStreamStartWithTimeout(InputStream is, byte[] b, int timeoutMillis)
            throws IOException {
        int bufferOffset = is.read(b);
        long start = System.currentTimeMillis();
        long maxTimeMillis = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < maxTimeMillis && bufferOffset < b.length) {
            int readLength = java.lang.Math.min(is.available(), b.length - bufferOffset);
            // can alternatively use bufferedReader, guarded by isReady():
            int readResult = is.read(b, bufferOffset, readLength);
            if (readResult == -1) {
                Logger.i(">>>>>card RX >>>>> BREAK");
                break;
            }
            bufferOffset += readResult;
        }
        Logger.i(">>>>>card RX >>>>> ReadDataCoast:" + (System.currentTimeMillis() - start));
        Logger.i(">>>>>card RX >>>>> bufferOffset:" + bufferOffset);
        return bufferOffset;
    }

    /**
     * 尝试解码
     *
     * @param cardData
     * @return
     */
    public long tryToSumNO(int protocol, byte[] cardData) {
        long cardNumber;
        // IQEQ的平台，特殊对待
        if (Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V5)
                || Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V4)) {
            cardNumber = CardNumberBuildUtils.getInstance().buildIQEQ(cardData);
            if (cardNumber > 0) {
                return cardNumber;
            }
        } else if (Build.MODEL.equals(Constant.PlatformAdapter.Squid)
                || Build.MODEL.equals(Constant.PlatformAdapter.Scallops)
                || Build.MODEL.equals(Constant.PlatformAdapter.Peas)
                || Build.MODEL.equals(Constant.PlatformAdapter.Donkey)) {
            cardNumber = CardNumberBuildUtils.buildHSJ522BT(cardData);
            if (cardNumber > 0) {
                return cardNumber;
            }
        }
        int cardReaderType = SPUtils.getCardReaderType();
        if (cardReaderType == Constant.CardReaderType.ID) {
            cardNumber = CardNumberBuildUtils.getInstance().buildIDCardV2(protocol, cardData);
            if (cardNumber > 0) {
                return cardNumber;
            }
        } else if (cardReaderType == Constant.CardReaderType.IC) {
            cardNumber = CardNumberBuildUtils.getInstance().buildICCardV2(cardData);
            if (cardNumber > 0) {
                return cardNumber;
            }
        } else {
            cardNumber = CardNumberBuildUtils.getInstance().buildIDCardV2(protocol, cardData);
            if (cardNumber > 0) {
                SPUtils.setCardReaderType(Constant.CardReaderType.ID);
                return cardNumber;
            }
            cardNumber = CardNumberBuildUtils.getInstance().buildICCardV2(cardData);
            if (cardNumber > 0) {
                SPUtils.setCardReaderType(Constant.CardReaderType.ID);
                return cardNumber;
            }
        }

        return 0L;
    }

    /**
     * 读取到卡号的处理
     */
    private class SendCardIDRunnable implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    CardPath cp = cardQueue.take();
                    // 剔除重复打卡数据
                    if (Arrays.equals(lastCardPath.cardNo, cp.cardNo)
                            && System.currentTimeMillis() - lastCardPathTime < 200L) {
                        continue;
                    }
                    lastCardPathTime = System.currentTimeMillis();
                    lastCardPath = cp;

                    long[] probablyResult = cp.getCardNo();
                    String path = cp.getPath();

                    long startAll = System.currentTimeMillis();

                    SwipeCardInfo swipeCardInfo;
                    boolean validity = false;
                    for (long cardID : probablyResult) {
                        if (cardID > 0) {
                            validity = true;
                            swipeCardInfo = OrganizeData(String.valueOf(cardID), path);
                            if (swipeCardInfo.getCardInfo() != null) {
                                break;
                            }
                        }
                    }
                    if (!validity) {
                        continue;
                    }
                    SPUtils.setReaderPath(path);
                    String msg = "读卡器串口:" + path + " 耗时:" + (System.currentTimeMillis() - startAll) + " MODEL" + Build.MODEL;
                    SPUtils.setLoadingConfig(msg);

                    EventBus.getDefault().post(new ListenResultType(3));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * 数据处理
     *
     * @param cardNumber
     */
    public SwipeCardInfo OrganizeData(String cardNumber, String path) {
        CardRecord cardRecord = new CardRecord();
        UUID uuid = UUID.randomUUID();
        cardRecord.setId(uuid.toString());
        cardRecord.setCard_serial_number(cardNumber);
        cardRecord.setRecord_time(new Date().getTime());
        cardRecord.setHas_sync(false);
        cardRecord.setHas_upload(false);

        List<CardInfo> dbResultList = CardInfoModule.getInstance().getByCardNumber(cardRecord.getCard_serial_number());
        SwipeCardInfo swipeCardInfo = new SwipeCardInfo();

        if (!dbResultList.isEmpty()) {
            swipeCardInfo.setCardInfo(dbResultList.get(0));
            cardRecord.setCard_number(swipeCardInfo.getCardInfo().getCardNumber());
        }
        swipeCardInfo.setCardRecord(cardRecord);
        swipeCardInfo.setPath(path);

        return swipeCardInfo;
    }
}
