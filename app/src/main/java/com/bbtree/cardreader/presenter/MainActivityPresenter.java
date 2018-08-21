
package com.bbtree.cardreader.presenter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.RemoteException;
import android.text.TextUtils;

import com.bbtree.baselib.net.BaseParam;
import com.bbtree.baselib.utils.ListUtils;
import com.bbtree.baselib.utils.PackageUtils;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.DataTransfer;
import com.bbtree.cardreader.R;
import com.bbtree.cardreader.TTSWorker;
import com.bbtree.cardreader.common.Code;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.config.TempConfig;
import com.bbtree.cardreader.contact.MainActivityContract;
import com.bbtree.cardreader.entity.ClassSpeakerRequest;
import com.bbtree.cardreader.entity.TempReportData;
import com.bbtree.cardreader.entity.dao.CardInfo;
import com.bbtree.cardreader.entity.dao.PlayNum;
import com.bbtree.cardreader.entity.dao.Speaker;
import com.bbtree.cardreader.entity.dao.SpeakerConfig;
import com.bbtree.cardreader.entity.dao.TempRecord;
import com.bbtree.cardreader.entity.eventbus.ScreenSaverCMD;
import com.bbtree.cardreader.entity.eventbus.SwipeCardInfo;
import com.bbtree.cardreader.entity.eventbus.TTSInfo;
import com.bbtree.cardreader.entity.requestEntity.Ad;
import com.bbtree.cardreader.entity.requestEntity.GetAdResData;
import com.bbtree.cardreader.entity.requestEntity.PlayNumResult;
import com.bbtree.cardreader.entity.requestEntity.SpeakerConfigRequest;
import com.bbtree.cardreader.entity.requestEntity.SpeakerConfigResult;
import com.bbtree.cardreader.entity.requestEntity.TempConfigResult;
import com.bbtree.cardreader.model.AdModule;
import com.bbtree.cardreader.model.CardRecordModule;
import com.bbtree.cardreader.model.SpeakerModule;
import com.bbtree.cardreader.model.TempRecordModule;
import com.bbtree.cardreader.service.CardListenerService;
import com.bbtree.cardreader.utils.DeviceConfigUtils;
import com.bbtree.cardreader.utils.ReadPhoneInfo;
import com.bbtree.cardreader.utils.SPUtils;
import com.bbtree.cardreader.utils.TempControl;
import com.bbtree.childservice.utils.QRCodeUtils;
import com.bbtree.lib.bluetooth.temperature.beans.Conn;
import com.bbtree.lib.bluetooth.temperature.beans.Temp;
import com.bbtree.lib.bluetooth.temperature.impl.ConnStatusListener;
import com.bbtree.lib.bluetooth.temperature.impl.TempListener;
import com.google.zxing.WriterException;
import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;


/**
 * Description : MainActivityPresenter
 */
public class MainActivityPresenter implements MainActivityContract.Presenter {

    private static final String TAG = MainActivityPresenter.class.getSimpleName();

    private MainActivityContract.View mView;

    private long schoolId;

    private TTSWorker ttsWorker;
    private DataTransfer mDataTransfer;

    //add by baodian
    public DataTransfer getDataTransfer() {
        return mDataTransfer;
    }


    private SwipeCardInfo lastSwipeCardInfo = new SwipeCardInfo();

    private List<String> cpList = new ArrayList<>();// 获取读卡器路径 Arrays.asList("/dev/ttyS2", "/dev/ttyS3");

    /**
     * 所有要播报的信息
     */
    private LinkedBlockingQueue<Object[]> classSpeakerQueue;

    /**
     * 本地音响列表
     */
    private static List<Speaker> localExistSpeakers;

    /**
     * 本地音响配置
     */
    private static SpeakerConfig localExistSpeakerConfig;

    /**
     * 广告
     */
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<Ad> lastAds;// 上一次请求广告列表

    public MainActivityPresenter(MainActivityContract.View view) {
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void onDestroyPresenter() {
        mView = null;
        if (compositeDisposable != null) compositeDisposable.clear();
    }

    @Override
    public void start() {
        initMyPIN();
        initCrashReport();
        createQRCode();
        getMachineInfo();
        getSchoolName();
        getReaderPath();
        updateFailCountView();
    }

    @Override
    public void getAllSpeakers() {

        // 多媒体控制
        ((Activity) mView.getContext()).setVolumeControlStream(AudioManager.STREAM_MUSIC);

        SpeakerConfigRequest request = new SpeakerConfigRequest();
        request.setDeviceId(BaseParam.getDeviceId());
        request.setSchoolId(schoolId);
        request.setSn("");
        SpeakerModule.getInstance().getSpeakerConfig(request).doOnNext(new Consumer<SpeakerConfigResult>() {
            @Override
            public void accept(SpeakerConfigResult resultObject) {
                SpeakerModule.getInstance().saveSpeakerConfigToDB(resultObject);
            }
        })
                .subscribe(new Observer<SpeakerConfigResult>() {
                    @Override
                    public void onError(Throwable e) {
                        SpeakerModule.getInstance().getDBSpeakers();
                        initClassSpeaker();
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(SpeakerConfigResult resultObject) {
                        if (resultObject.getCode() == Code.SUCCESS) {
                            initClassSpeaker();
                        } else {
                            SpeakerModule.getInstance().getDBSpeakers();
                            initClassSpeaker();
                        }
                    }
                });
    }

    /**
     * 发射器串口 监听
     */
    private void initClassSpeaker() {
        //获取音响列表和音箱配置
        localExistSpeakers = SpeakerModule.getInstance().getLocalExistSpeakers();
        localExistSpeakerConfig = SpeakerModule.getInstance().getLocalExistSpeakerConfig();

        // 获取发射器串口
        String senderPath = SPUtils.getSenderPath("");
        Logger.t(TAG).i("senderPath：" + senderPath);
        if (TextUtils.isEmpty(senderPath)) {
            return;
        }
        classSpeakerQueue = new LinkedBlockingQueue<>(5);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(new SendSignal2Speaker());
    }


    @Override
    public void setSpeakerStub(TTSWorker ttsWorker) {
        this.ttsWorker = ttsWorker;
    }

    @Override
    public void setTransferStub(DataTransfer mDataTransfer) {
        this.mDataTransfer = mDataTransfer;
    }

    @Override
    public void startCardListener() {
        CardListenerService.startIt(mView.getContext());
    }

    @Override
    public void onEventAsyncSwipeCardInfo(SwipeCardInfo swipeCardInfo) {
        lastSwipeCardInfo = swipeCardInfo;
        if (swipeCardInfo.getCardInfo() != null) {
            byte[] bytes = mView.takePhotoBytes();
            if (null != bytes && bytes.length != 0) {
                swipeCardInfo.setPhotoByte(bytes);
                swipeCardInfo.setDegrees(BBTreeApp.getApp().getDegrees());
            }
            if (mDataTransfer != null) {
                try {
                    mDataTransfer.transferCardRecord(swipeCardInfo);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Logger.t(TAG).i("mDataTransfer is null");
            }
        }
    }

    @Override
    public void classSpeakerBroadcast(SwipeCardInfo swipeCardInfo) {
        if (localExistSpeakers == null || localExistSpeakers.size() == 0
                || CardListenerService.outputStream == null) {
            return;
        }
        Logger.t(TAG).i("音箱播报");
        if (swipeCardInfo == null) return;

        try {
            final CardInfo cardInfo = swipeCardInfo.getCardInfo();
            if (cardInfo == null) {
                return;
            }
            if (cardInfo.getUserType() != Constant.UserType.Student) {
                return;
            }
            if (classSpeakerQueue == null) {
                classSpeakerQueue = new LinkedBlockingQueue<>(5);
            }
            String class_id = cardInfo.getClassId() + "";
            Logger.i("card class_id:" + class_id);
            for (final Speaker speaker : localExistSpeakers) {
                if (speaker.getClassIds() == null) {
                    continue;
                }
                if (speaker.getClassIds().contains(class_id)) {
                    Object[] tempObject = new Object[2];
                    tempObject[0] = cardInfo;
                    tempObject[1] = speaker;
                    classSpeakerQueue.offer(tempObject);
                    Logger.t(TAG).i("classSpeakerQueue size:" + classSpeakerQueue.size());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEventBackgroundTTSInfo(TTSInfo ttsInfo) {
        if (ttsInfo != null) {
            readText(ttsInfo.text);
        }
    }

    @Override
    public void getTempConfig() {
        if (schoolId < 1) {
            mView.showFragmentBySchoolId(false);
        } else {
            mView.showFragmentBySchoolId(true);
            getTempConfigByAPI(schoolId);
        }
    }

    @Override
    public void measureTemp(boolean open) {
        if (open) {
            TempConfig.setSdkMode(TempConfig.SdkMode.MODE_CLASSIC);
            TempControl tempControl = TempControl.getmInstance(mView.getContext());
            tempControl.setConnStatusListener(mConnStatusListener);
            tempControl.setTempListener(mTempListener);
            tempControl.start();
        } else {
            TempControl.getmInstance(mView.getContext()).stop();
        }
    }

    @Override
    public void initAdData() {
        compositeDisposable.add(Observable.interval(0, Constant.ScreenSaver.REFRESH_SCREENSAVER, TimeUnit.MILLISECONDS)
                .flatMap(new Function<Long, ObservableSource<GetAdResData>>() {
                    @Override
                    public ObservableSource<GetAdResData> apply(@NonNull Long aLong) throws Exception {
                        Map adMap = new HashMap();
                        adMap.put("deviceId", BaseParam.getDeviceId());
                        adMap.put("sn", "");
                        return AdModule.getInstance().getAd(adMap);
                    }
                })
                .subscribe(new Consumer<GetAdResData>() {
                    @Override
                    public void accept(@NonNull GetAdResData result) throws Exception {

                        List<Ad> ads = new ArrayList<Ad>();
                        if (null != result && null != result.data) {
                            List<Ad> adsTemp = result.data.getAds();
                            if (!ListUtils.isZero(adsTemp)) {
                                for (Ad temp : adsTemp) {
                                    if (temp.getAdType() == 2) {
                                        ads.add(temp);
                                    }
                                }
                            }
                        }

                        if (!equalsAdsResult(ads))
                            if (!ListUtils.isZero(ads))
                                mView.showScreenUI(ads);
                            else
                                mView.showDefaultImg();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        mView.showDefaultImg();
                    }
                }));
    }

    @Override
    public void onEventAsyncAd(ScreenSaverCMD screenSaverCMD) {
        if (screenSaverCMD.cmd == ScreenSaverCMD.ScreenSaverAction.adUpdate) {
            Map adMap = new HashMap();
            adMap.put("deviceId", BaseParam.getDeviceId());
            adMap.put("sn", screenSaverCMD.sn);
            AdModule.getInstance().getAd(adMap).subscribe(new Consumer<GetAdResData>() {
                @Override
                public void accept(@NonNull GetAdResData result) throws Exception {
                    List<Ad> ads = new ArrayList<Ad>();
                    if (null != result && null != result.data) {
                        List<Ad> adsTemp = result.data.getAds();
                        if (!ListUtils.isZero(adsTemp)) {
                            for (Ad temp : adsTemp) {
                                if (temp.getAdType() == 2) {
                                    ads.add(temp);
                                }
                            }
                        }
                    }

                    if (!equalsAdsResult(ads))
                        if (!ListUtils.isZero(ads))
                            mView.showScreenUI(ads);
                        else
                            mView.showDefaultImg();
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(@NonNull Throwable throwable) throws Exception {
                    mView.showDefaultImg();
                }
            });
        }
    }

    @Override
    public void playNum(Ad ad, boolean isPushNum) {
        if (!isPushNum) return;
        //记录轮播次数并存本地库
        PlayNum num = new PlayNum();
        if (null != ad && ad.getId() > 0) {
            int id = ad.getId();
            num.setAdId(id);
            AdModule.getInstance().insert(num);
        }
    }

    @Override
    public void pushPlayNumApi() {
        compositeDisposable.add(Observable.interval(5 * 1000, Constant.ScreenSaver.REFRESH_SCREENSAVER, TimeUnit.MILLISECONDS)
                .filter(new Predicate<Long>() {
                    @Override
                    public boolean test(@NonNull Long aLong) throws Exception {
                        List<PlayNum> nums = AdModule.getInstance().queryAllPlayNumList();
                        return !ListUtils.isZero(nums);
                    }
                })
                .flatMap(new Function<Long, ObservableSource<PlayNumResult>>() {
                    @Override
                    public ObservableSource<PlayNumResult> apply(@NonNull Long aLong) throws Exception {
                        Map map = new HashMap();
                        map.put("deviceId", BaseParam.getDeviceId());
                        map.put("nums", AdModule.getInstance().queryAllPlayNumList());
                        return AdModule.getInstance().pushPlayNum(map);
                    }
                })
                .doOnNext(new Consumer<PlayNumResult>() {
                    @Override
                    public void accept(@NonNull PlayNumResult playNumResult) throws Exception {
                        AdModule.getInstance().queryAllPlayNList(playNumResult);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PlayNumResult>() {
                    @Override
                    public void accept(@NonNull PlayNumResult result) throws Exception {
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                    }
                }));
    }

    /**
     * 如果服务器返回对象和上次对象相等返回true ，
     */
    private boolean equalsAdsResult(List<Ad> ads) {
        if (lastAds == null) {
            lastAds = ads;
            return false;
        }
        if ((lastAds.size() == ads.size()) && ads.containsAll(lastAds)) {
            return true;
        }
        lastAds = ads;
        return false;
    }

    /**
     * 初始化协议唯一PIN值：对应不同的学校,用于和音箱通信
     */
    private void initMyPIN() {
        schoolId = SPUtils.getSchoolId(0L);
        String schoolIdStr = schoolId + "";
        if (schoolIdStr.length() <= 4) {
            SPUtils.setSpeakerPin(Integer.parseInt(schoolIdStr));
        } else {
            SPUtils.setSpeakerPin(Integer.parseInt(schoolIdStr.substring(schoolIdStr.length() - 4)));
        }
    }

    /**
     * 初始化crash
     */
    private void initCrashReport() {
        final String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(new Date());
        if (!PackageUtils.isDebuggable(mView.getContext())) {
            //给崩溃收集提供更多的信息
//            CrashReport.setUserId(BBTreeApp.getApp().getMachineAlias());
//            CrashReport.putUserData(mView.getContext(), "Alias", BBTreeApp.getApp().getMachineAlias());
//            CrashReport.putUserData(mView.getContext(), "SchoolName", SPUtils.getSchoolName(""));
//            CrashReport.putUserData(mView.getContext(), "AppStartTime", nowTime);
        }
    }

    /**
     * 生成二维码
     */
    private void createQRCode() {
        Bitmap qrCode = null;
        String targetUrl = Constant.QRCode.QRCODEBASEURL + Constant.QRCode.QRCODEALIAS + SPUtils.getDeviceAlias(null);
        try {
            qrCode = QRCodeUtils.createQRCode(targetUrl, 80, 80);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        if (null != qrCode) {
            mView.showQRCode(qrCode);
        }
    }

    /**
     * 获取设备信息
     */
    private void getMachineInfo() {
        String appVersionName = String.format(mView.getContext().getString(R.string.version_name_prefix), ReadPhoneInfo.getAppVersionName(mView.getContext()));
        String deviceAlias = String.format(mView.getContext().getString(R.string.sn_prefix), SPUtils.getDeviceAlias(""));
        mView.showMachineInfo(appVersionName, deviceAlias);
    }

    /**
     * 获取学校名字
     */
    private void getSchoolName() {
        String schoolName = SPUtils.getSchoolName("");
        mView.showSchoolName(schoolName);
    }

    /**
     * 获取读卡器路径
     */
    private void getReaderPath() {
        cpList.add(SPUtils.getReaderPath(""));
    }

    /**
     * 获取蓝牙测温枪开启与否
     */
    private void getTempConfigIsShow() {
        mView.showTempUI(SPUtils.getTempConfig(false));
    }

    /**
     * 获取温枪配置
     *
     * @param schoolId
     */
    private void getTempConfigByAPI(long schoolId) {
        Map map = new HashMap();
        map.put("schoolId", schoolId);
        TempRecordModule.getInstance().getTempConfig(map).subscribe(new Observer<TempConfigResult>() {
            @Override
            public void onError(Throwable e) {
                getTempConfigIsShow();
            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(TempConfigResult tempConfig) {
                if (tempConfig.getCode() == Code.SUCCESS) {
                    SPUtils.setTempConfig(tempConfig.isOpen());
                    mView.showTempUI(tempConfig.isOpen());
                    measureTemp(tempConfig.isOpen());
                } else {
                    getTempConfigIsShow();
                }
            }
        });
    }

    /**
     * 耳温枪状态监听
     */
    private ConnStatusListener mConnStatusListener = new ConnStatusListener() {
        @Override
        public void getConnStatus(final Conn status) {
            if (null != status && null != mView && null != mView.getContext()) {
                ((Activity) mView.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (status.getStatus()) {
                            case Conn.ConnStatus.CONNECTED:
                                Logger.d(TAG, ">>> 蓝牙连接");
                                mView.resetScreenLoop();
                                mView.showTempIcon(R.mipmap.temp_connected);
                                break;
                            case Conn.ConnStatus.CONNECTING:
                                Logger.d(TAG, ">>> 蓝牙正在连接");
                                mView.showTempIcon(R.mipmap.temp_connecting);
                                break;
                            case Conn.ConnStatus.DISCONNECT:
                                Logger.d(TAG, ">>> 蓝牙连接断开");
                                mView.showTempIcon(R.mipmap.temp_searching);
                                break;
                        }
                    }
                });
            }
        }
    };

    /**
     * 上传失败记录数
     */
    private void updateFailCountView() {

        Observable.interval(0, Constant.ScreenSaver.SCREENSAVER_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .map(new Function<Long, String[]>() {
                    @Override
                    public String[] apply(Long aLong) throws Exception {
                        long failRecordCount = CardRecordModule.getInstance().getFailRecordSync();
                        String failRecordCountStr = String.format(mView.getContext().getString(R.string.no_uploaded_records_prefix), failRecordCount + "");

                        long failUploadCount = CardRecordModule.getInstance().getFailUpload();
                        String failUploadCountStr = String.format(mView.getContext().getString(R.string.no_uploaded_pictures_prefix), failUploadCount + "");

                        return new String[]{failRecordCountStr, failUploadCountStr};
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String[]>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String[] value) {
                        if (mView != null && value != null && value.length >= 2) {
                            mView.showNoUploaded(value[0], value[1]);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private TempListener mTempListener = new TempListener() {
        @Override
        public void temperature(final Temp temp) {
            Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(ObservableEmitter<String> subscriber) throws Exception {
                    mView.resetScreenLoop();
                    final float tempValue = temp.getTemp();
                    final String tempText = String.format(mView.getContext().getResources().getString(R.string.temp_table_value), String.valueOf(tempValue));
                    readText(tempText);
                    if (lastSwipeCardInfo != null && lastSwipeCardInfo.getCardInfo() != null) {
                        TempRecord record = new TempRecord();
                        record.setId(UUID.randomUUID().toString());
                        record.setMac_id(lastSwipeCardInfo.getCardInfo().getId());
                        record.setCard_record_id(lastSwipeCardInfo.getCardRecord().getId());
                        record.setSchool_id(schoolId);
                        record.setTemp_time(new Date().getTime());
                        record.setTemp_unit(0);
                        record.setTemperature(tempValue);
                        record.setHas_sync(false);
                        TempRecordModule.getInstance().saveTempRecord(record);
                        TempReportData reportData = new TempReportData();
                        lastSwipeCardInfo.getCardInfo().setFamily(null);
                        lastSwipeCardInfo.getCardInfo().setFamilyString("");
                        reportData.setCardInfoNode(lastSwipeCardInfo.getCardInfo());
                        reportData.setTempNode(record);
                        transferTempRecord(reportData);
                    }
                    subscriber.onNext(tempText);
                }
            })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                mView.setSelectedPosition(Constant.ScreenSaverConstant.TYPE_CARD);
                                CardInfo cardInfo = lastSwipeCardInfo.getCardInfo();
                                mView.showMainUi();
                                if (cardInfo != null && cardInfo.getUserType() == 0) {
                                    mView.showCardNo(s);
                                } else {
                                    mView.showNowTemp(s);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

        }
    };

    /**
     * 朗读
     *
     * @param tempText
     */
    private void readText(String tempText) {
        if (ttsWorker != null) {
            int speaker = DeviceConfigUtils.getConfig().getSpeaker();
            try {
                ttsWorker.readText(speaker, tempText);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Logger.t(TAG).i("ttsWorker is null");
        }
    }

    /**
     * 上传体温数据
     *
     * @param tempReportData
     */
    private void transferTempRecord(TempReportData tempReportData) {
        if (mDataTransfer != null) {
            try {
                mDataTransfer.transferTempRecord(tempReportData);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Logger.t(TAG).i("mDataTransfer is null");
        }
    }

    private class SendSignal2Speaker implements Runnable {
        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Logger.i("开始给音箱发送指令");
                    long start = System.currentTimeMillis();
                    Object[] objects = classSpeakerQueue.take();

                    CardInfo cardInfo = (CardInfo) objects[0];
                    Speaker speaker = (Speaker) objects[1];
                    Logger.t(TAG).i("音箱名:" + speaker.getName() + "  对应班级:" + speaker.getClassIds());

                    // 发送朗读指令
                    StringBuilder targetText = new StringBuilder();
                    if (localExistSpeakerConfig.getPlayClass() == 1) {
                        targetText.append(cardInfo.getClassName() == null ? "" : cardInfo.getClassName());
                    }
                    for (int i = 0; i < localExistSpeakerConfig.getNamePlayNum(); i++) {
                        targetText.append(cardInfo.getName() + " ");
                    }
                    targetText.append(mView.getContext().getResources().getString(R.string.speaker_suffix));
                    for (int i = 1; i < localExistSpeakerConfig.getPlayNum(); i++) {
                        targetText.append(targetText);
                    }
                    Logger.t(TAG).i("播报内容：" + targetText.toString());

                    CardListenerService.request = new ClassSpeakerRequest(1, 10, SPUtils.getSpeakerPin(0),
                            speaker.getGroup_name(), speaker.getNumber(), 1, targetText.toString());
                    Logger.t(TAG).i("播报请求：" + CardListenerService.request.toString());
                    CardListenerService.outputStream.write(CardListenerService.request.getmCMDBytes());
                    // 两条指令之间必须有间隔（上一个信号必须返回否则会有干扰）音响才能正确播报
                    Thread.sleep(1500);
                    Logger.t(TAG).i("结束给音箱发送指令，耗时：" + (System.currentTimeMillis() - start));
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }
}
