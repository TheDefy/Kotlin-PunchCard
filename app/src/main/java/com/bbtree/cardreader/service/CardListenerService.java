package com.bbtree.cardreader.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.bbtree.baselib.base.BaseService;
import com.bbtree.baselib.utils.ListUtils;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.BuildConfig;
import com.bbtree.cardreader.R;
import com.bbtree.cardreader.TTSWorker;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.config.DeviceConfig;
import com.bbtree.cardreader.config.VoiceConfig;
import com.bbtree.cardreader.entity.CardPath;
import com.bbtree.cardreader.entity.ClassSpeakerRequest;
import com.bbtree.cardreader.entity.dao.CardInfo;
import com.bbtree.cardreader.entity.eventbus.ClassSpeakerResponse;
import com.bbtree.cardreader.entity.eventbus.SwipeCardInfo;
import com.bbtree.cardreader.model.SerialPortModule;
import com.bbtree.cardreader.serialport.SerialPort;
import com.bbtree.cardreader.utils.DeviceConfigUtils;
import com.bbtree.cardreader.utils.ReadPhoneInfo;
import com.bbtree.cardreader.utils.SPUtils;
import com.bbtree.cardreader.utils.SerialPortUtils;
import com.bbtree.childservice.utils.HexConver;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/05/25
 * Time: 下午4:40
 */
public class CardListenerService extends BaseService {
    public static final String ACTION_START = "com.bbtree.card-listener.start";
    private String TAG = CardListenerService.class.getSimpleName();
    //    private static LinkedBlockingQueue<long[]> cardQueue;
    private static LinkedBlockingQueue<CardPath> cardQueue;

    public static ClassSpeakerRequest request;
    public static ClassSpeakerResponse response;
    public static OutputStream outputStream;
    int cardReaderType;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    List<byte[]> cardDataLists = new ArrayList<>();
    int count = 0;
    /**
     * 上一个刷卡数据
     */
    private CardPath lastCardPath = new CardPath();
    private long lastCardPathTime = 0;
    byte[] lastCardBytes = null;

    private StringBuffer sbFactoryTip = new StringBuffer();

    static {
        cardQueue = new LinkedBlockingQueue<>(5);
    }

    private ExecutorService serialPortThreadPool;
    private TTSWorker ttsWorker;
    private ServiceConnection mRemoteConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            ttsWorker = TTSWorker.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            ttsWorker = null;
        }
    };

    public static void startIt(Context hostContext) {
        Logger.i("startIt");
        Intent intent = new Intent(hostContext, CardListenerService.class);
        intent.setAction(CardListenerService.ACTION_START);
        hostContext.startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initSpeaker();

        cardReaderType = SPUtils.getCardReaderType();

        listenSerialPort();
    }

    /**
     * 播音进程
     */
    private void initSpeaker() {
        Intent intent = new Intent("com.bbtree.tts.action.read");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String packageName = BBTreeApp.getApp().getResources().getString(R.string.package_name);
        intent.setComponent(new ComponentName(packageName,
                packageName + ".service.TTSReaderService"));
        bindService(intent, mRemoteConnection, BIND_AUTO_CREATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int startCommandResult = START_STICKY;
        if (intent == null) {
            return startCommandResult;
        }
        String action = intent.getAction();
        if (action.equals(ACTION_START)) {
            if (serialPortThreadPool == null) {
                listenSerialPort();
            }
        }
        return startCommandResult;
    }

    /**
     * 串口监听
     */
    private void listenSerialPort() {
        List<String> list = SerialPortUtils.getAllSerailPorts();

        if (list.size() == 0) {
            //TODO 此处需要处理无可用串口情况
            return;
        }

        int coreNum = ReadPhoneInfo.getCPUCoreNums();
        final SendCardIDRunnable[] sendCardIDRunnable = new SendCardIDRunnable[coreNum <= 2 ? 1 : coreNum / 2];

        serialPortThreadPool = Executors.newFixedThreadPool(list.size() + 1 + sendCardIDRunnable.length);
        for (int i = 0; i < list.size(); i++) {
            String port = list.get(i);
            Logger.i("listenSerialPort port:" + port);
            serialPortThreadPool.execute(new SerialPortRunnable(port));
        }

        String senderPath = SPUtils.getSenderPath("");
        if (!TextUtils.isEmpty(senderPath)) {
            Logger.i("listenSerialPort senderPath:" + senderPath);
            serialPortThreadPool.execute(new SenderSerialPortRunnable(senderPath));
        }

        for (SendCardIDRunnable worker : sendCardIDRunnable) {
            worker = new SendCardIDRunnable();
            serialPortThreadPool.execute(worker);
        }
    }

//    /**
//     * 数据进来时候，启动超时读取处理
//     *
//     * @param is
//     * @param b
//     * @param timeoutMillis
//     * @return
//     * @throws IOException
//     */
//    private int readInputStreamStartWithTimeout(InputStream is, byte[] b, int timeoutMillis)
//            throws IOException {
//        int bufferOffset = is.read(b);
//        long start = System.currentTimeMillis();
//        long maxTimeMillis = System.currentTimeMillis() + timeoutMillis;
//        while (System.currentTimeMillis() < maxTimeMillis && bufferOffset < b.length) {
//            int readLength = Math.min(is.available(), b.length - bufferOffset);
//            // can alternatively use bufferedReader, guarded by isReady():
//            int readResult = is.read(b, bufferOffset, readLength);
//            if (readResult == -1) {
//                Logger.i(">>>>>card RX >>>>> BREAK");
//                break;
//            }
//            bufferOffset += readResult;
//        }
//        Logger.i(">>>>>card RX >>>>> ReadDataCoast:" + (System.currentTimeMillis() - start));
//        Logger.i(">>>>>card RX >>>>> bufferOffset:" + bufferOffset);
//        return bufferOffset;
//    }

    /**
     * 重置数组
     *
     * @param cardData
     */
    private void resetArray(byte[] cardData) {
        Arrays.fill(cardData, Byte.valueOf("17"));
    }

    /**
     * 朗读部分
     *
     * @param cardInfo
     */
    private void read(CardInfo cardInfo) {
        String text = null;
        DeviceConfig deviceConfig = DeviceConfigUtils.getConfig();
        if (cardInfo == null) {
            text = "无此卡信息";
        } else {
            List<VoiceConfig> voiceList = deviceConfig.getCardVoice();
            if (ListUtils.isZero(voiceList)) return;
            Logger.v("voice speak:" + new Gson().toJson(voiceList));
            boolean inRange = false;//判断有无符合条件的
            VoiceConfig matchConfig = null;
            //先取本地存储的服务器通知的配置
            for (VoiceConfig config : voiceList) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constant.VoiceFormat.TimeFormat, Locale.getDefault());
                String nowTime = simpleDateFormat.format(new Date());
                if (inTheInterval(config.getStart(), config.getEnd(), nowTime, Constant.VoiceFormat.TimeFormat)) {
                    inRange = true;
                    matchConfig = config;
                    break;
                }
            }
            // 如果当前时间段没有匹配到合适的配置,则取默认配置
            if (!inRange) {
                voiceList = deviceConfig.getDefaultVoice();
            }
            for (VoiceConfig config : voiceList) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constant.VoiceFormat.TimeFormat, Locale.getDefault());
                String nowTime = simpleDateFormat.format(new Date());
                if (inTheInterval(config.getStart(), config.getEnd(), nowTime, Constant.VoiceFormat.TimeFormat)) {
                    matchConfig = config;
                    break;
                }
            }
            if (matchConfig != null) {
                //发现符合条件的
                String[] names = analyticName(cardInfo);

                if (cardInfo.getUserType() == Constant.UserType.Teacher) {
                    text = matchConfig.getTeacher();
                } else if (cardInfo.getUserType() == Constant.UserType.Student) {
                    text = matchConfig.getStudent();
                }
                if (!TextUtils.isEmpty(text)) {
                    text = text.replaceAll(Constant.VoiceFormat.FullName,
                            TextUtils.isEmpty(names[0]) ? "" : names[0]);
                    text = text.replaceAll(Constant.VoiceFormat.FamilyName,
                            TextUtils.isEmpty(names[1]) ? "" : names[1]);
                    text = text.replaceAll(Constant.VoiceFormat.FirstName,
                            TextUtils.isEmpty(names[2]) ? "" : names[2]);
                    text = text.replaceAll(Constant.VoiceFormat.ClassName,
                            TextUtils.isEmpty(cardInfo.getClassName()) ?
                                    "" : cardInfo.getClassName());
                } else {
                    text = names[0];
                }
            }
        }
        int speaker = deviceConfig.getSpeaker();
//        if (result >= Speaker.BaiduInterval[0] && result <= Speaker.BaiduInterval[1]) {
//            BaiduTTSBuilder.getInstance().read(text);
//        } else if (result >= Speaker.XunfeiInterval[0] && result <= Speaker.XunfeiInterval[1]) {
//            IflytekTTSBuilder.getInstance().read(text);
//        } else {
//            IflytekTTSBuilder.getInstance().read(text);
//        }
        try {
            if (ttsWorker != null) {
                ttsWorker.readText(speaker, text);
            } else {
                Logger.i("ttsWorker is null");
            }
        } catch (RemoteException e) {
            Logger.i(e.getMessage());
        }
    }

    /**
     * 名字重新解析
     *
     * @param cardInfo
     * @return
     */
    private String[] analyticName(CardInfo cardInfo) {
        String name = cardInfo.getName();
        String alias = cardInfo.getAlias();
        String familyName = cardInfo.getFamilyName();

        String fullNameCorrect = (TextUtils.isEmpty(cardInfo.getAlias()) ? name : alias);

        if (TextUtils.isEmpty(fullNameCorrect)) {
            fullNameCorrect = "匿名";
            familyName = null;
        }
        String familyNameCorrect = TextUtils.isEmpty(familyName) ?
                fullNameCorrect.substring(0, 1) : fullNameCorrect.substring(0, familyName.length());
        String firstNameCorrect = TextUtils.isEmpty(familyName) ?
                fullNameCorrect.substring(1, fullNameCorrect.length()) : fullNameCorrect.substring(familyName.length(), fullNameCorrect.length());

        String[] nameStr = new String[3];
        nameStr[0] = fullNameCorrect;//全称
        nameStr[1] = familyNameCorrect;//姓氏
        nameStr[2] = firstNameCorrect;//名字
        return nameStr;
    }

//    int count2 = 0 ;
    /**
     * 读取到卡号的处理
     */
    private class SendCardIDRunnable implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
//                    String name = Thread.currentThread().getName();
//                    long[] probablyResult = cardQueue.take();

                    CardPath cp = cardQueue.take();

                    // 剔除重复打卡数据
                    /*if (Arrays.equals(lastCardPath.cardNo, cp.cardNo)
                            && System.currentTimeMillis() - lastCardPathTime < 2000L) {
                        continue;
                    }
                    lastCardPathTime = System.currentTimeMillis();
                    lastCardPath = cp;*/

                    long[] probablyResult = cp.getCardNo();
                    String path = cp.getPath();
                    long startAll = System.currentTimeMillis();

                    Logger.i(">>>>>card RX >>>>> CardQueueSize:" + cardQueue.size());
                    SwipeCardInfo swipeCardInfo = null;
                    boolean validity = false;
                    for (long cardID : probablyResult) {
                        Logger.i("cardID pure:" + cardID);
                        if (cardID > 0) {
                            Logger.i("cardID:" + cardID);
                            validity = true;
                            swipeCardInfo = SerialPortModule.getInstance().OrganizeData(String.valueOf(cardID), path);
                            if (swipeCardInfo.getCardInfo() != null) {
                                int index;
                                if (((index = sbFactoryTip.indexOf(swipeCardInfo.getCardRecord().getCard_serial_number())) > 0)
                                        && (BuildConfig.isDebug || BuildConfig.isFactory)
                                        && SPUtils.getFactoryTip(false)) {
                                    sbFactoryTip.insert(index, "✓ ");
                                }
                                break;
                            }
                        }
                    }
                    if ((BuildConfig.isDebug || BuildConfig.isFactory)
                            && SPUtils.getFactoryTip(false)) {
                        Logger.i("sbFactoryTip:" + sbFactoryTip);
                        EventBus.getDefault().post(sbFactoryTip.toString());
                        sbFactoryTip.setLength(0);
                    }
                    if (!validity) {
                        continue;
                    }
//                    SwipeCardInfo swipeCardInfo = OrganizeData(String.valueOf(cardID));

                    if (swipeCardInfo.getCardInfo() == null) {
                        read(null);
                    } else {
//                        count2 ++ ;
//                        Log.e("插入数据库", count2+"");
                        //cancel by baodian
                        //CardRecordModule.getInstance().insertRecord(swipeCardInfo.getCardRecord());
                        read(swipeCardInfo.getCardInfo());
                    }
                    long start = System.currentTimeMillis();
                    EventBus.getDefault().post(swipeCardInfo);
                    SPUtils.setFirstInsetUPan(false);
                    Logger.i(">>>>> take time :>>>>" + (System.currentTimeMillis() - start));
                    Logger.i(">>>>> take time all:>>>>" + (System.currentTimeMillis() - startAll));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 读卡器监听
     */
    private class SerialPortRunnable implements Runnable {
        private String serialPortName;

        public SerialPortRunnable(String deviceName) {
            serialPortName = deviceName;
        }

        @Override
        public void run() {
            SerialPort mSerialPort = null;
            SerialPortUtils serialPortUtils = SerialPortUtils.newInstance();
            int baudRate = SerialPortUtils.getBaudRate();
            String protocolJsonV2 = SPUtils.getRFID_ProtocolV2(null);
            Integer[] protocolItem;
            if (!TextUtils.isEmpty(protocolJsonV2)) {
                protocolItem = new Gson().fromJson(protocolJsonV2, Integer[].class);
                if (protocolItem == null) {
                    protocolItem = new Integer[]{8};
                }
            } else {
                protocolItem = new Integer[]{8};
            }

            try {
                mSerialPort = serialPortUtils.getSerialPort(baudRate, serialPortName);
            } catch (IOException | SecurityException e) {
                e.printStackTrace();
            }
            if (mSerialPort == null) {
                return;
            }
            final String T = serialPortName;

            Logger.t(T).i(">>>>>card RX start with:" + serialPortName);
            InputStream inputStream = mSerialPort.getInputStream();
            OutputStream outputStream = mSerialPort.getOutputStream();
            try {
                // 有些读卡器的头只有握手才能刷卡,握手一次
                outputStream.write(HexConver.hexStr2Bytes("08ACFFFFFFFFFFFF00"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                int size;
                byte[] cardData = new byte[48 + 5];//扇区数据.三个块的数据。5为读卡器增加的开头
                if (inputStream == null) {
                    break;
                }
                try {
//                    size = inputStream.read(buffer);
                    size = SerialPortModule.getInstance().readInputStreamStartWithTimeout(inputStream, cardData, 50);
                    Logger.t(T).i(serialPortName + ">>>>>card RX size:" + size);
                    if (size == 0) {
                        continue;
                    }
                    long coastStart = System.currentTimeMillis();
                    Logger.t(T).i(">>>>>card RX start read size:" + size);
                    byte[] tempResult = new byte[size];
                    System.arraycopy(cardData, 0, tempResult, 0, size);
                    cardData = tempResult;


                    /**
                     * 在读数据层面做拦截处理,同一张卡1s秒之内多次打卡无效.
                     */

                    if (Arrays.equals(lastCardBytes, cardData)
                            && System.currentTimeMillis() - lastCardPathTime < 1000L) {
                        continue;
                    }
                    lastCardPathTime = System.currentTimeMillis();
                    lastCardBytes = cardData;

                    count++;

                    Log.e("count", count + "");




                    /**
                     * 在读数据层面做拦截处理,10张有效打卡在20s秒之内多次打卡无效.
                     */
                    /*if (containsBytes(cardDataLists, cardData) && System.currentTimeMillis() - lastCardPathTime < 20 * 1000L) {
                        continue;
                    } else {
                        lastCardPathTime = System.currentTimeMillis();
                        if (cardDataLists.size() > 10) {
                            cardDataLists.remove(11);
                            Logger.e("cardDataLists size is 11");
                        }
                        cardDataLists.add(cardData);
                    }*/


                    if (size > 0) {
                        String originalResult = HexConver.byte2HexStr(cardData, cardData.length);

                        Logger.t(T).i(serialPortName + ">>>>>card RX start read originalResult:" + originalResult);

                        // 读卡器握手成功
                        if ("5A5948010103".equals(originalResult)) {
                            resetArray(cardData);
                            continue;
                        }
                        long[] probablyResult = new long[protocolItem.length];
                        for (int i = 0; i < protocolItem.length; i++) {
                            long result = SerialPortModule.getInstance().tryToSumNO(protocolItem[i], cardData);
                            if (result > 0) {
                                probablyResult[i] = result;
                                Logger.t(T).i(">>>>>card RX >>>>>tryToSumNO with protocol[" + protocolItem[i] +
                                        "] Result:" + "[" + result + "] take:" + (System.currentTimeMillis() - coastStart));
                            }
                            if ((BuildConfig.isDebug || BuildConfig.isFactory) && SPUtils.getFactoryTip(false)) {
                                sbFactoryTip.append(protocolItem[i]).append(":").append(result).append("！");
                            }
                        }
                        CardPath cp = new CardPath();
                        cp.setCardNo(probablyResult);
                        cp.setPath(serialPortName);
                        boolean cardQueueResult = cardQueue.offer(cp);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 当前线程被打断才会关闭串口监听
            serialPortUtils.closeSerialPort();
        }
    }

    /**
     * 发射器监听
     */
    private class SenderSerialPortRunnable implements Runnable {
        private String serialPortName;

        public SenderSerialPortRunnable(String deviceName) {
            serialPortName = deviceName;
        }

        @Override
        public void run() {
            SerialPort mSerialPort = null;
            SerialPortUtils serialPortUtils = SerialPortUtils.newInstance();
            try {
                mSerialPort = serialPortUtils.getSerialPort(115200, serialPortName);
                // 将打开的串口放入到allPorts中
            } catch (IOException | SecurityException e) {
                e.printStackTrace();
            }
            if (mSerialPort == null) {
                return;
            }
            final String T = serialPortName;

            InputStream inputStream = mSerialPort.getInputStream();
            outputStream = mSerialPort.getOutputStream();

            while (!Thread.currentThread().isInterrupted()) {
                int size;
                if (inputStream == null) {
                    break;
                }
                try {
                    byte[] buffer = new byte[20];
                    size = inputStream.read(buffer);
                    Logger.t(T).i(serialPortName + " size:" + size);
                    if (size == 0) {
                        continue;
                    }
                    if (size > 0) {
                        String originalResult = HexConver.byte2HexStr(buffer, buffer.length);
                        Logger.t(T).i("serialPortName:" + serialPortName + " originalResult:" + originalResult);
                        byte[] data = new byte[size];
                        System.arraycopy(buffer, 0, data, 0, size);
                        String dataResult = HexConver.byte2HexStr(data, data.length);
                        Logger.t(T).i("serialPortName:" + serialPortName + " dataResult:" + dataResult);
                        // 发射器接收消息成功
                        if (null != request) {
                            response = request.getResponse();
                            Logger.t(T).i("request.getHexCMD:" + request.getHexCMD());
                            Logger.t(T).i("response.getHexCMDResult:" + response.getHexCMDResult());
                            if (originalResult.startsWith(response.getHexCMDResult()) || (response != null && response.getHexCMDResult().contains(dataResult))) {
                                EventBus.getDefault().post(response);
                                continue;
                            }
                        }

                    }
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 当前线程被打断才会关闭串口监听
            serialPortUtils.closeSerialPort();
        }
    }

    /**
     * 循环制：允许开始时间和结束时间跨天
     *
     * @param start
     * @param end
     * @param target
     * @param dateformat
     * @return
     */
    public static boolean inTheInterval(String start, String end, String target, String dateformat) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dateformat, Locale.getDefault());
            Date startDate = sdf.parse(start);
            Date endDate = sdf.parse(end);
            Date targetDate = sdf.parse(target);

            if (startDate.after(endDate)) {//跨天
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);
                calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 12);
                startDate = sdf.parse(sdf.format(calendar.getTime()));

                calendar.setTime(endDate);
                calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 12);
                endDate = sdf.parse(sdf.format(calendar.getTime()));

                calendar.setTime(targetDate);
                calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 12);
                targetDate = sdf.parse(sdf.format(calendar.getTime()));
            }

            return startDate.compareTo(targetDate) <= 0 && endDate.compareTo(targetDate) >= 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private boolean containsBytes(List<byte[]> byteList, byte[] bytes) {
        for (byte[] b : byteList) {
            if (Arrays.equals(b, bytes)) {
                return true;
            }
        }
        return false;
    }
}
