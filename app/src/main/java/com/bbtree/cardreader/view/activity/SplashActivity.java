package com.bbtree.cardreader.view.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bbtree.baselib.base.BaseActivity;
import com.bbtree.baselib.net.BaseParam;
import com.bbtree.baselib.net.GsonParser;
import com.bbtree.baselib.net.ResultObject;
import com.bbtree.baselib.rxjava.RxUtils;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.R;
import com.bbtree.cardreader.common.Code;
import com.bbtree.cardreader.entity.dao.CardRecord;
import com.bbtree.cardreader.entity.eventbus.ListenResultType;
import com.bbtree.cardreader.entity.requestEntity.SchoolInfo;
import com.bbtree.cardreader.model.CardInfoModule;
import com.bbtree.cardreader.model.CardRecordModule;
import com.bbtree.cardreader.model.DataInfoModule;
import com.bbtree.cardreader.model.SerialPortModule;
import com.bbtree.cardreader.receiver.NetworkStateReceiver;
import com.bbtree.cardreader.service.HeartBeatService;
import com.bbtree.cardreader.service.HotFixService;
import com.bbtree.cardreader.utils.NetWorkUtil;
import com.bbtree.cardreader.utils.ReadPhoneInfo;
import com.bbtree.cardreader.utils.SPUtils;
import com.bbtree.cardreader.view.anim.effects.bouncing_entrances.BounceInDownAnimator;
import com.bbtree.cardreader.view.dialogs.TipDialog;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.SignUpEvent;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;

//import static com.bbtree.cardreader.service.HeartBeatService.beginDeviceid;

/**
 * Created by zhouyl on 07/04/2017.
 */

public class SplashActivity extends BaseActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    @BindView(R.id.iv_logo)
    ImageView iv_logo;
    @BindView(R.id.tv_notice)
    TextView tv_notice;

    private TipDialog tipDialog;

    private boolean isSchoolCom, isUIAnimator;

    private long startTime;

    private void startHeartBeatService() {// 开启心跳服务
        startService(new Intent(this, HeartBeatService.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        startTime = System.currentTimeMillis();

        //beginDeviceid = BaseParam.getInstance().readDeviceIdFromDisk();

//        uploadImgOss();


        if (BaseParam.context == null) {
            BaseParam.context = this.getApplicationContext();
        }

        startHeartBeatService();

        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        //给本机指定DNS
        DataInfoModule.getInstance().setDns();
        initView();
        hotFixWork(); //一个机器执行过一次就可以了吧， 不用每次都启动。
        SerialPortModule.getInstance().listenAllPorts();
    }

    private void initView() {
        BounceInDownAnimator mAnimator = new BounceInDownAnimator();
        mAnimator.build(iv_logo);
        mAnimator.setDuration(3000);
        mAnimator.addAnimListener(animatorListener);
        mAnimator.start();
    }

    private void initData() {
        String secretKey = SPUtils.getSecretKey(null);
        String deviceAlias = SPUtils.getDeviceAlias(null);

        BaseParam.getInstance().getDeviceId();


        if (TextUtils.isEmpty(secretKey) || TextUtils.isEmpty(deviceAlias) || TextUtils.isEmpty(BaseParam.beginDeviceid)) {
            DataInfoModule.getInstance().getAppKey().subscribe(new DefaultObserver<ResultObject>() {

                @Override
                public void onError(Throwable e) {
                    Logger.t("AppKey Error").i(e.getMessage());
                    Answers.getInstance().logSignUp(new SignUpEvent()
                            .putMethod("register")
                            .putSuccess(false)
                            .putCustomAttribute("device_id", BaseParam.getInstance().getDeviceId())
                            .putCustomAttribute("app_version_code", ReadPhoneInfo.getAppVersionCode(BBTreeApp.getApp()))
                            .putCustomAttribute("app_version_name", ReadPhoneInfo.getAppVersionName(BBTreeApp.getApp()))
                            .putCustomAttribute("model", Build.MODEL));
                    e.printStackTrace();
                    showErrorDialog();
                }

                @Override
                public void onComplete() {

                }

                @Override
                public void onNext(ResultObject resultObject) {
                    if (resultObject.getCode() == Code.SUCCESS) {
                        String key = (String) ((Map) resultObject.getObject()).get("key");
                        SPUtils.setSecretKey(key);
                        SPUtils.setDeviceAlias((String) ((Map) resultObject.getObject()).get("alias"));
                        RxUtils.setSecretKey(key);


                        BaseParam.getInstance().writeDeviceId2Disk(BaseParam.getInstance().getDeviceId());

                        initSchoolInfo();
                        Answers.getInstance().logSignUp(new SignUpEvent()
                                .putMethod("register")
                                .putSuccess(true)
                                .putCustomAttribute("device_id", BaseParam.getInstance().getDeviceId())
                                .putCustomAttribute("app_version_code", ReadPhoneInfo.getAppVersionCode(BBTreeApp.getApp()))
                                .putCustomAttribute("app_version_name", ReadPhoneInfo.getAppVersionName(BBTreeApp.getApp()))
                                .putCustomAttribute("model", Build.MODEL));

                    } else {
                        showErrorDialog();
                    }
                }
            });
        } else {
            RxUtils.setSecretKey(secretKey);

            initSchoolInfo();
        }
    }

    /**
     * 拉去所有的卡信息、更新设备信息、获取学校信息
     */
    private void initSchoolInfo() {
        CardInfoModule.getInstance().getCards().subscribe(new DefaultObserver<ResultObject>() {
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

        DataInfoModule.getInstance().getDeviceConfig().subscribe(new DefaultObserver<ResultObject>() {
            @Override
            public void onNext(ResultObject result) {
                if (result.getCode() == Code.SUCCESS && result.getObject() != null) {
                    SPUtils.setDeviceConfig(String.valueOf(result.getObject()));
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        DataInfoModule.getInstance().getSchoolInfo().subscribe(new DefaultObserver<SchoolInfo>() {
            @Override
            public void onNext(SchoolInfo result) {
                if (result.getCode() == Code.SUCCESS) {
                    if (!TextUtils.isEmpty(result.getSchoolName())) {
                        SPUtils.setSchoolName(result.getSchoolName());
                    }
                    if (result.getSchoolId() > 0) {
                        SPUtils.setSchoolId(result.getSchoolId());
                    }
                    if (result.getBaudRate() > 0) {
                        SPUtils.setBaudRate(result.getBaudRate());
                    }
                    if (result.getRfidProtocolV2() != null) {
                        SPUtils.setRFID_ProtocolV2(GsonParser.parserToJson(result.getRfidProtocolV2()));
                    }
                }
                isSchoolCom = true;
                endWork();
            }

            @Override
            public void onError(Throwable e) {
                isSchoolCom = true;
                endWork();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    Handler handler = new Handler();

    private void endWork() {
        if (isSchoolCom && isUIAnimator) {
            if (System.currentTimeMillis() - startTime < 3000) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }
                }, System.currentTimeMillis() - startTime);
            }
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void showErrorDialog() {
        if (tipDialog == null) {
            tipDialog = new TipDialog();
        }
        if (tipDialog != null && tipDialog.isAdded()) {
            return;
        }
        tipDialog.show(getFragmentManager(), "tip_APP_KEY_ERROR");

        NetworkStateReceiver.removeRegisterObserver(netChangeObserver);
        NetworkStateReceiver.registerObserver(netChangeObserver);
    }

    /**
     * 热补程序
     */
    private void hotFixWork() {
        Intent hotFixIntent = new Intent(SplashActivity.this, HotFixService.class);
        hotFixIntent.setAction(HotFixService.ACTION_HOTFIX);
        startService(hotFixIntent);
    }

    private AnimatorListenerAdapter animatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            isUIAnimator = true;
            endWork();
        }
    };

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        NetworkStateReceiver.removeRegisterObserver(netChangeObserver);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainResult(ListenResultType listenResultType) {
        switch (listenResultType.getResultType()) {
            case 1:
                BBTreeApp.getApp().enableMDWatchDog();
                initData();
                break;
            case 2:
                BBTreeApp.getApp().disableMDWatchDog();
                iv_logo.setVisibility(View.GONE);
                tv_notice.setText(R.string.reader_checking);
                tv_notice.setVisibility(View.VISIBLE);
                break;
            case 3:
                setOvertimeTask();
                break;
            default:
                break;
        }
    }

    /**
     * 定时重启
     */
    private void setOvertimeTask() {
        Observable.interval(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                if (aLong == 1L) {
                    iv_logo.setVisibility(View.GONE);
                    tv_notice.setText(R.string.reader_checked);
                    tv_notice.setVisibility(View.VISIBLE);
                } else if (aLong == 4L)
                    BBTreeApp.getApp().restartApp(500);
            }
        });
    }

    private NetworkStateReceiver.NetChangeObserver netChangeObserver = new NetworkStateReceiver.NetChangeObserver() {
        @Override
        public void onConnect(NetWorkUtil.NetType type) {
            Logger.t(TAG).i("网络已连接");
            initData();
        }

        @Override
        public void onDisConnect() {
            Logger.t(TAG).i("网络已断开");
        }
    };

    /**
     * 上穿alioss图片
     */
    private void uploadImgOss() {

        CardRecord mCardRecord = new CardRecord();
//        mCardRecord.setCard_holder("/storage/sdcard0/BBTree/zhs_card_cl.png");
        mCardRecord.setCard_holder("/storage/sdcard0/Pictures/zhs_card_cl.png");
        CardRecordModule.getInstance().uploadImg(mCardRecord)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<CardRecord>() {
                    @Override
                    public void accept(CardRecord cardRecord) throws Exception {
                        Log.d("88888", "Cloud_url:" + cardRecord.getCloud_url());
                    }
                })
                .subscribe(new DefaultObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                        Log.d("88888", "onNext");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("88888", "onError:" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
