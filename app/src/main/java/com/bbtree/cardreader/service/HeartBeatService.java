package com.bbtree.cardreader.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bbtree.baselib.base.BaseService;
import com.bbtree.baselib.net.BaseParam;
import com.bbtree.baselib.net.ResultObject;
import com.bbtree.baselib.rxjava.RxUtils;
import com.bbtree.baselib.utils.PackageUtils;
import com.bbtree.baselib.utils.StringUtils;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.common.Code;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.common.Instruction;
import com.bbtree.cardreader.common.Urls;
import com.bbtree.cardreader.entity.CommandEntity;
import com.bbtree.cardreader.entity.eventbus.ScreenSaverCMD;
import com.bbtree.cardreader.entity.eventbus.TempConfigEventBus;
import com.bbtree.cardreader.entity.requestEntity.Reporter;
import com.bbtree.cardreader.entity.requestEntity.TempConfigResult;
import com.bbtree.cardreader.model.CardInfoModule;
import com.bbtree.cardreader.model.CardRecordModule;
import com.bbtree.cardreader.model.DataInfoModule;
import com.bbtree.cardreader.model.SpeakerModule;
import com.bbtree.cardreader.model.TempRecordModule;
import com.bbtree.cardreader.report.FailRecordReport;
import com.bbtree.cardreader.utils.DeviceConfigUtils;
import com.bbtree.cardreader.utils.ReadPhoneInfo;
import com.bbtree.cardreader.utils.SPUtils;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;

import static com.bbtree.baselib.net.BaseParam.getMacAddress;

/**
 * 心跳处理和 失败任务定时轮训
 * Created by zhouyl on 19/04/2017.
 */

public class HeartBeatService extends BaseService {

    private final static String TAG = HeartBeatService.class.getSimpleName();

    private final static int HEART_BEAT_INTERVAL_TIME = 61;
    private AMapLocationProvider provider;
    private HashMap map;

    @Override
    public void onCreate() {
        super.onCreate();
        heartBeatTick();
//        CardRecordModule.getInstance().uploadFailRecord();//失败卡记录上传
//        TempRecordModule.getInstance().uploadFailTempRecord();//失败温度数据
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 定位、心跳
     */
    private void heartBeatTick() {
        Observable.interval(0, HEART_BEAT_INTERVAL_TIME, TimeUnit.SECONDS)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long aLong) throws Exception {
                        getLocation();
                        newHeartBeatRequest();
                        // TODO 旧心跳接口
                        HeartBeatRequest();
                        if (aLong % 15 == 0) {
                            uploadMachineInfo();
                        }
                        return aLong;
                    }
                })
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<Long>() {

                    @Override
                    public void onNext(Long value) {
                        Logger.t(TAG).i("时间 ：" + System.currentTimeMillis() + "======value:" + value);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /**
     * 从服务器拉取所有卡
     *
     * @param sn
     */
    private void getAllCardInfo(String... sn) {
        CardInfoModule.getInstance().getCards(sn)
                .subscribe(new DefaultObserver<ResultObject>() {
                    @Override
                    public void onNext(ResultObject value) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 获取机器配置信息
     */
    private void getDeviceConfig(final String... sn) {
        DataInfoModule.getInstance().getDeviceConfig(sn)
                .subscribe(new Observer<ResultObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResultObject result) {
                        if (result.getCode() == Code.SUCCESS) {
                            SPUtils.setDeviceConfig(String.valueOf(result.getObject()));
                            //如果SN不为空，说明是服务器发的指令，此时需要更新配置文件
                            if (sn.length > 0 && !TextUtils.isEmpty(sn[0])) {
//                                final String model = ReadPhoneInfo.getPhoneModel();
//                                if (model.startsWith("CS3")) {
//                                    IflytekTTSBuilder.getInstance().build(BBTreeApp.getApp());
//                                }
//                                BaiduTTSBuilder.getInstance().build(BBTreeApp.getApp());
                                EventBus.getDefault().post(DeviceConfigUtils.getConfig());
                            }
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

    /**
     * 切换耳温枪功能
     */
    private void getTempConfig() {
        long schoolId = SPUtils.getSchoolId(0L);
        if (schoolId > 0L) {
            Map map = new HashMap();
            map.put("schoolId", schoolId);
            TempRecordModule.getInstance().getTempConfig(map).subscribe(new Observer<TempConfigResult>() {
                @Override
                public void onError(Throwable e) {
                    TempConfigEventBus tempConfigEventBus = new TempConfigEventBus();
                    tempConfigEventBus.requestSuccess = false;
                    EventBus.getDefault().post(tempConfigEventBus);
                }

                @Override
                public void onComplete() {

                }

                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(TempConfigResult tempConfig) {
                    TempConfigEventBus tempConfigEventBus = new TempConfigEventBus();
                    tempConfigEventBus.result = tempConfig;
                    if (tempConfig.getCode() == Code.SUCCESS) {
                        SPUtils.setTempConfig(tempConfig.isOpen());
                        tempConfigEventBus.requestSuccess = true;
                        EventBus.getDefault().post(tempConfigEventBus);
                    } else {
                        tempConfigEventBus.requestSuccess = false;
                        EventBus.getDefault().post(tempConfigEventBus);
                    }

                }
            });
        }
    }

    /**
     * 处理心跳指令
     */
    private void processHeartBeat(CommandEntity commandEntity) {
        int command = commandEntity.getCommand();
        Logger.t(TAG).i("time:(" + StringUtils.formatDate(new Date(System.currentTimeMillis()), "HH:mm:ss") + ")===CommandEntity===" + command + ":" + commandEntity.getSn());
        switch (command) {
            case Instruction.CardsPull:// 15 从服务器拉取所有卡
                getAllCardInfo(commandEntity.getSn());
                break;
            case Instruction.ClearCards:// 1 清空本地卡信息
                BBTreeApp.getApp().getDaoSessionInstance().getCardInfoDao().deleteAll();
                break;
            case Instruction.DeleteCards: // 3 删除卡片
                CardInfoModule.getInstance().curdCards(commandEntity.getSn(), CardInfoModule.DELETECARD);
                break;
            case Instruction.AddCards: // 4 增加卡片
                CardInfoModule.getInstance().curdCards(commandEntity.getSn(), CardInfoModule.ADDCARD);
                break;
            case Instruction.PullDeviceConfig: // 12 获取机器配置信息
                getDeviceConfig(commandEntity.getSn());
                break;
            case Instruction.PullFailedRecord: // 5 拉取失败队列
                CardRecordModule.getInstance().uploadFailRecord(commandEntity.getSn());//失败记录上传
                TempRecordModule.getInstance().uploadFailTempRecord(commandEntity.getSn());//失败温度数据
                break;
            case Instruction.InstallAPK: // 9 安装应用
                UpdateCheckService.startCheckUpdate(getApplicationContext());
                break;
            case Instruction.ScreenSaverUpdate: // 10 屏保/轮播更新
                SPUtils.setFirstInsetUPan(false);
                ScreenSaverCMD cmd = new ScreenSaverCMD();
                cmd.cmd = ScreenSaverCMD.ScreenSaverAction.screenSaveUpdate;
                cmd.sn = commandEntity.getSn();
                EventBus.getDefault().post(cmd);
                break;
            case Instruction.CardsPush: // 2 推送本地现在的所有卡信息给服务器
                CardInfoModule.getInstance().cardsPush(commandEntity.getSn());
                break;
            case Instruction.SwitchTemp: // 20 切换耳温枪功能
                getTempConfig();
                break;
            case Instruction.GetSpeakerConfig: // 21 获取音响列表
                SpeakerModule.getInstance().getLocalAllSpeakers(commandEntity.getSn());
                break;
            case Instruction.QueryUploadFail: // 16 查询未上传成功的条数
                FailRecordReport.getInstance().getReport(BBTreeApp.getApp(), commandEntity.getSn());
                break;
            case Instruction.AdUpdate:// 22 广告更新
                SPUtils.setFirstInsetUPan(false);
                ScreenSaverCMD cmd1 = new ScreenSaverCMD();
                cmd1.cmd = ScreenSaverCMD.ScreenSaverAction.adUpdate;
                cmd1.sn = commandEntity.getSn();
                EventBus.getDefault().post(cmd1);
                break;
            default:
                break;
        }
    }

    //新的心跳
    private void newHeartBeatRequest() {
        if (map == null) {
            map = new HashMap();
            map.put("deviceId", BaseParam.getInstance().getDeviceId());
        }
        RxUtils.postMap(Urls.HEARTBEATNEW, map)
                .map(RxUtils.getMap())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResultObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResultObject value) {
                        Logger.t(TAG).i("Heart Beat code :" + value.getCode());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 获取定位信息
     */
    private void getLocation() {
        if (provider == null) {
            provider = AMapLocationProvider.getInstance();
        }
        provider.init(this);
        provider.start();
    }


    //public static String beginDeviceid="";
    // -------------------------------------旧心跳接口---------------------------------

    /**
     * 心跳请求
     */
    private void HeartBeatRequest() {
        RxUtils.postEntity(Urls.HEARTBEAT, Reporter.getInstance().build(BBTreeApp.getApp()))
                .map(RxUtils.getList("commands", CommandEntity[].class))
                .filter(new Predicate<ResultObject>() {
                    @Override
                    public boolean test(ResultObject resultObject) throws Exception {
                        return resultObject.getCode() == Code.SUCCESS;
                    }
                })
                .flatMap(new Function<ResultObject, Observable<CommandEntity>>() {
                    @Override
                    public Observable<CommandEntity> apply(ResultObject resultObject) throws Exception {
                        return Observable.fromIterable((List<CommandEntity>) resultObject.getObject());
                    }
                })
                .map(new Function<CommandEntity, String>() {
                    @Override
                    public String apply(CommandEntity commandEntity) throws Exception {
                        processHeartBeat(commandEntity);
                        return commandEntity.getSn();
                    }
                })
                .subscribe(new DefaultObserver<Object>() {

                    @Override
                    public void onNext(Object value) {
                        Logger.t(TAG).i("CommandEntity 时间：" + System.currentTimeMillis() + "======value:" + value);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    // -------------------------------------旧心跳接口---------------------------------//

    static boolean first = true;


    private void uploadMachineInfo() {

        if( BaseParam.context == null )
        {
            BaseParam.context = this.getApplicationContext();
        }

        BaseParam.getInstance().getDeviceId();

        Map map = new HashMap();
        map.put("appVersionCode", ReadPhoneInfo.getAppVersionCode(BBTreeApp.getApp()));
        map.put("alias", SPUtils.getDeviceAlias(""));
        map.put("macWifi", TextUtils.isEmpty(ReadPhoneInfo.getLocalMacAddress(BBTreeApp.getApp())) ? "" : ReadPhoneInfo.getLocalMacAddress(BBTreeApp.getApp()));
        map.put("mac", TextUtils.isEmpty(getMacAddress()) ? "" : getMacAddress());
        map.put("model", Build.MODEL);
        map.put("hasWatchDog", null == PackageUtils.getPackageInfo(BBTreeApp.getApp(), Constant.PackageNameInfo.WatchDog) ? 0 : 1);
        map.put("display", Build.DISPLAY);
        map.put("fingerprint", Build.FINGERPRINT);

        String show = "";
        if( first == false )
        {
            String get = BaseParam.getInstance().readDeviceIdFromDisk();
            show = "("+first+")"+ "valid path: "+BaseParam.validPath+ " getdeviceid:"+get;
        }
        else
        {
            show = "("+first+")"+ "valid path: "+BaseParam.validPath+ " getdeviceid:"+BaseParam.getInstance().beginDeviceid;
        }

        //map.put("deviceId", BaseParam.getInstance().getImei() + "_" + BaseParam.getInstance().getMacAddress()+" FileContent:"+"("+first+")"+BaseParam.getInstance().beginDeviceid);
        map.put("deviceId",show);
        first = false;
        map.put("imei", TextUtils.isEmpty(BaseParam.getInstance().getImei()) ? "" : BaseParam.getInstance().getImei());
        map.put("terminalType", BaseParam.getInstance().getMachineType());

        RxUtils.postMapNoCrypt(Urls.MACHINEINFO, map)
                .map(RxUtils.getMap())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResultObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResultObject value) {
                        Logger.t(TAG).i("Heart Beat code :" + value.getCode());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /**
     * 自动重启
     */
    private void autoReboot() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Calendar targetTime = Calendar.getInstance();
                while (true) {
                    targetTime.setTime(new Date());
                    final int nowYear = targetTime.get(Calendar.YEAR);
                    Logger.i(">>>>>>nowYear>>>" + nowYear);
                    if (nowYear < 2015) {
                        try {
                            Thread.sleep(30000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        break;
                    }
                }
                Logger.i("autoReboot task will start");

                targetTime.add(Calendar.DAY_OF_MONTH, 1);
                targetTime.set(Calendar.HOUR_OF_DAY, 1);
                targetTime.set(Calendar.MINUTE, 0);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Logger.d("next reboot is :" + sdf.format(targetTime.getTime()));

                AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent("com.bbtree.cardreader.action.TIMEUP");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(HeartBeatService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                am.set(AlarmManager.RTC_WAKEUP, targetTime.getTimeInMillis(), pendingIntent);
            }
        }).start();

    }
}
