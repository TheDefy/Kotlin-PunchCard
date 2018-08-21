package com.bbtree.cardreader.view.activity;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bbtree.baselib.base.AppExecutors;
import com.bbtree.baselib.base.BaseActivity;
import com.bbtree.baselib.net.BaseParam;
import com.bbtree.baselib.net.GsonParser;
import com.bbtree.baselib.utils.ListUtils;
import com.bbtree.baselib.utils.StringUtils;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.BuildConfig;
import com.bbtree.cardreader.DataTransfer;
import com.bbtree.cardreader.R;
import com.bbtree.cardreader.TTSWorker;
import com.bbtree.cardreader.camera.gl.CameraViewGL;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.config.Config;
import com.bbtree.cardreader.contact.MainActivityContract;
import com.bbtree.cardreader.entity.Family;
import com.bbtree.cardreader.entity.dao.CardInfo;
import com.bbtree.cardreader.entity.dao.CardRecord;
import com.bbtree.cardreader.entity.eventbus.NetworkQualityResult;
import com.bbtree.cardreader.entity.eventbus.ScreenSaverCMD;
import com.bbtree.cardreader.entity.eventbus.ScreenSaverUPanEvent;
import com.bbtree.cardreader.entity.eventbus.SwipeCardInfo;
import com.bbtree.cardreader.entity.eventbus.TTSInfo;
import com.bbtree.cardreader.entity.eventbus.TempConfigEventBus;
import com.bbtree.cardreader.entity.requestEntity.Ad;
import com.bbtree.cardreader.presenter.MainActivityPresenter;
import com.bbtree.cardreader.receiver.NetworkStateReceiver;
import com.bbtree.cardreader.service.CardListenerService;
import com.bbtree.cardreader.service.HeartBeatService;
import com.bbtree.cardreader.service.RecordPushService;
import com.bbtree.cardreader.service.TTSReaderService;
import com.bbtree.cardreader.utils.DeviceConfigUtils;
import com.bbtree.cardreader.utils.NetWorkUtil;
import com.bbtree.cardreader.utils.SPUtils;
import com.bbtree.cardreader.utils.ScreenUtils;
import com.bbtree.cardreader.utils.TempControl;
import com.bbtree.cardreader.view.ScreenSaverDelayShow;
import com.bbtree.cardreader.view.anim.effects.bouncing_entrances.FlashAnimator;
import com.bbtree.cardreader.view.anim.effects.bouncing_entrances.FlashOnceAnimator;
import com.bbtree.cardreader.view.banner.CustomBanner;
import com.bbtree.cardreader.view.dialogs.CameraSettingDialog;
import com.bbtree.cardreader.view.fragment.DeviceInfoFragment;
import com.bbtree.cardreader.view.fragment.NetWorkMonitorFragment;
import com.bbtree.cardreader.view.picasso.CircleTransform;
import com.bbtree.childservice.utils.QRCodeUtils;
import com.google.zxing.WriterException;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nineoldandroids.animation.Animator;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/05/29
 * Time: 上午11:55
 */
public class MainActivity extends BaseActivity implements MainActivityContract.View {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.iv_net_status)
    ImageView iv_net_status;
    @BindView(R.id.tv_net_warning)
    TextView tv_net_warning;
    @BindView(R.id.iv_bluetooth_status)
    ImageView iv_bluetooth_status;

    @BindView(R.id.tv_schoolName)
    TextView tv_schoolName;

    @BindView(R.id.iv_swipe_card)
    ImageView iv_swipe_card;
    @BindView(R.id.iv_notification)
    ImageView iv_notification;
    @BindView(R.id.iv_recipe)
    ImageView iv_recipe;
    @BindView(R.id.iv_picture)
    ImageView iv_picture;

    @BindView(R.id.iv_user_avatar)
    ImageView user_avatar;
    @BindView(R.id.tv_user_name)
    TextView user_name;

    @BindView(R.id.iv_familyA)
    ImageView familyA;
    @BindView(R.id.iv_familyB)
    ImageView familyB;
    @BindView(R.id.iv_familyC)
    ImageView familyC;
    @BindView(R.id.iv_familyD)
    ImageView familyD;
    @BindView(R.id.familyA_name)
    TextView familyA_name;
    @BindView(R.id.familyB_name)
    TextView familyB_name;
    @BindView(R.id.familyC_name)
    TextView familyC_name;
    @BindView(R.id.familyD_name)
    TextView familyD_name;

    /**
     * 拍照动画载体
     */
    @BindView(R.id.take_photo_cap)
    ImageView take_photo_cap;

    @BindView(R.id.iv_qr_code)
    ImageView iv_qr_code;
    @BindView(R.id.rl_user_info)
    View rl_user_info;
    @BindView(R.id.rl_no_user_info)
    View rl_no_user_info;
    @BindView(R.id.iv_no_card_qr_code)
    ImageView iv_no_card_qr_code;

    @BindView(R.id.tv_class)
    TextView tv_class;
    @BindView(R.id.tv_temperature)
    TextView tv_temperature;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.tv_card_no)
    TextView tv_card_no;

    @BindView(R.id.tv_sn)
    TextView tv_sn;
    @BindView(R.id.tv_versionName)
    TextView tv_versionName;
    @BindView(R.id.tv_no_uploaded_records)
    TextView tv_no_uploaded_records;
    @BindView(R.id.tv_no_uploaded_pictures)
    TextView tv_no_uploaded_pictures;

    /**
     * 相机界面
     */
    @BindView(R.id.camera_watch)
    CameraViewGL camera_watch;

    @BindView(R.id.rl_main)
    RelativeLayout mainUI;

    @BindView(R.id.iv_left)
    ImageView leftIcon;

    @BindView(R.id.iv_right)
    ImageView rightIcon;

    /**
     * ad广告
     */
    @BindView(R.id.iv_ad)
    CustomBanner mAdBanner;
    private long adScrollTime = Constant.ScreenSaver.SCREENSAVER_INTERVAL;  // 轮播时间

    private boolean isPushNum = true;

    /**
     * 圆形图片
     */
    private CircleTransform circleTransform;

    private DeviceInfoFragment deviceInfoFrg;
    /**
     * 轮播工具类
     */
    private ScreenSaverDelayShow screenSaverDelayShow;
    private NetWorkMonitorFragment mNetWorkMonitor;

    private long lastAnim;

    /**
     * 相机拍照动画
     */
    private FlashOnceAnimator mCameraAnimator;
    /**
     * 网络异常动画
     */
    private FlashAnimator mFlashAnimator;

    private TTSWorker ttsWorker;
    private DataTransfer mDataTransfer;

    private long lastClickBackKey = -1;

    private MainActivityContract.Presenter mPresenter;

    private long lastAnimTime = 0;

    private Drawable drawableTime, drawableCard, drawableClass, drawableTemp;

    private Drawable familyADrawable, familyBDrawable, familyCDrawable, familyDDrawable;

    private CompositeDisposable compositeDisposable;

    private long enterScreenSaverTime = Constant.ScreenSaver.SCREENSAVER_DELAY;  // 打卡后进入轮播的时间

    /**
     * 工厂包提示开关
     */
    private AlertDialog factoryTipSwitchDialog;
    /**
     * 工厂包提示
     */
    private AlertDialog factoryTipDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_main_new);
        ButterKnife.bind(this);
        new MainActivityPresenter(this);
        compositeDisposable = new CompositeDisposable();
        enterScreenSaverTime = DeviceConfigUtils.getConfig().getScreenSaverDelay();
        initView();
        initAnimator();
        mPresenter.start();
        setViewListener();

        bindTransferService();
        bindSpeakerService();
        //startHeartBeatService();

        mPresenter.getTempConfig();
        mPresenter.startCardListener();
        mPresenter.getAllSpeakers();

        initBanner();
        mPresenter.initAdData();// 初始化固定广告数据
        mPresenter.pushPlayNumApi();// 上传固定广告轮播次数
    }

    private void initBanner() {
        adScrollTime = DeviceConfigUtils.getConfig().getScreenSaverInterval();
    }

    private void startHeartBeatService() {// 开启心跳服务
        startService(new Intent(MainActivity.this, HeartBeatService.class));
    }

    private void bindSpeakerService() {//绑定语音播报的进程
        String package_name = getContext().getString(R.string.package_name);
        Intent intent = new Intent("com.bbtree.tts.action.read");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(package_name, package_name + ".service.TTSReaderService"));
        bindService(intent, mRemoteConnection, BIND_AUTO_CREATE);
    }

    private void bindTransferService() {// 绑定数据上传的进程
        String package_name = getContext().getString(R.string.package_name);
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(package_name, package_name + ".service.RecordPushService"));
        bindService(intent, mRemoteConnection, BIND_AUTO_CREATE);
    }

    /**
     * 初始化view
     */
    private void initView() {
        tv_class.setText(String.format(getString(R.string.default_class), ""));
        tv_temperature.setText(String.format(getString(R.string.default_temperature), ""));
        tv_time.setText(String.format(getString(R.string.default_time), ""));
        tv_card_no.setText(String.format(getString(R.string.default_number), ""));

        setSelectedPosition(Constant.ScreenSaverConstant.TYPE_CARD);

        circleTransform = new CircleTransform();
        initCardRes();
        initFamily();
    }

    /**
     * initAnimator
     */
    private void initAnimator() {
        mCameraAnimator = new FlashOnceAnimator();
        mCameraAnimator.setDuration(200);
        mCameraAnimator.build(take_photo_cap).addAnimListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                take_photo_cap.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                take_photo_cap.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                take_photo_cap.setLayerType(View.LAYER_TYPE_NONE, null);
                take_photo_cap.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                take_photo_cap.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mFlashAnimator = new FlashAnimator();
        mFlashAnimator.setDuration(2000);
        mFlashAnimator.build(tv_net_warning);
        mFlashAnimator.addAnimListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                tv_net_warning.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                tv_net_warning.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                tv_net_warning.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    /**
     * 设置view监听
     */
    private void setViewListener() {
        NetworkStateReceiver.registerNetworkStateReceiver(MainActivity.this);
        NetworkStateReceiver.registerObserver(netChangeObserver);
    }

    @OnClick(R.id.iv_unit_broadcast)
    void startClassSpeakerActivity() {
        Logger.t(TAG).i("开启分班播报配置界面");
        startActivity(new Intent(this, ClassSpeakerSettingActivity.class));
        overridePendingTransition(R.anim.activity_left_in, R.anim.activity_left_out);
        if (CardListenerService.outputStream == null) {
            finish();
        }
    }

    @OnClick({R.id.tv_versionName, R.id.tv_sn})
    void startRotateCamera() {
        Logger.t(TAG).i("版本号和SN被点击，旋转摄像头");
        stopScreenLoop();
        CameraSettingDialog cameraSettingDialog = new CameraSettingDialog(MainActivity.this);
        cameraSettingDialog.build();
        cameraSettingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                resetScreenLoop();
            }
        });
    }

    @OnClick(R.id.tv_no_uploaded_records)
    void showDeviceId() {
        Toast.makeText(MainActivity.this, BaseParam.getDeviceId(), Toast.LENGTH_SHORT).show();
    }

    @OnLongClick(R.id.iv_net_status)
    boolean checkNetStatus() {
        Logger.t(TAG).i("开启检测网络界面");
        stopScreenLoop();
        mNetWorkMonitor = new NetWorkMonitorFragment();
        mNetWorkMonitor.show(getFragmentManager());
        return false;
    }

    NetworkStateReceiver.NetChangeObserver netChangeObserver = new NetworkStateReceiver.NetChangeObserver() { // 网络连接的监听事件
        @Override
        public void onConnect(NetWorkUtil.NetType type) {
            Logger.t(TAG).i("网络已连接");
            reFreshNetStatus(Constant.NetCheckResult.SUCCESS);
        }

        @Override
        public void onDisConnect() {
            Logger.t(TAG).i("网络已断开");
            iv_net_status.setImageResource(R.mipmap.net_unavailable);
            showNetWarning(Constant.NetCheckResult.FAIL);
        }
    };

    ServiceConnection mRemoteConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Logger.i(">>>>ComponentName:" + className.getClassName());
            if (TextUtils.equals(className.getClassName(), TTSReaderService.class.getName())) {
                ttsWorker = TTSWorker.Stub.asInterface(service);
                mPresenter.setSpeakerStub(ttsWorker);
            } else if (TextUtils.equals(className.getClassName(), RecordPushService.class.getName())) {
                mDataTransfer = DataTransfer.Stub.asInterface(service);
                mPresenter.setTransferStub(mDataTransfer);
                try {
                    Camera.Size size = camera_watch.getTargetSize();
                    mDataTransfer.setCameraInfo(size == null ? 0 : size.width,
                            size == null ? 0 : size.height, camera_watch.cameraPreviewFormat());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (TextUtils.equals(className.getClassName(), TTSReaderService.class.getName())) {
                ttsWorker = null;
                mPresenter.setSpeakerStub(ttsWorker);
            } else if (TextUtils.equals(className.getClassName(), RecordPushService.class.getName())) {
                mDataTransfer = null;
                mPresenter.setTransferStub(mDataTransfer);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (camera_watch != null && !camera_watch.getPreviewing()) {
            camera_watch.startPreview();
        }
        reFreshNetStatus(Constant.NetCheckResult.SUCCESS);
        /*
        //only to crash
        CountDownTimer cdt = new CountDownTimer(10000, 3000) {
            @Override
            public void onTick(long millisUntilFinished) {



                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //此时已在主线程中，可以更新UI了
                        int y= 89/0;
                    }
                });


            }
            @Override
            public void onFinish() {

            }
        };

        cdt.start();
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*if (camera_watch != null) {
            camera_watch.stopPreview();
        }*/
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        mPresenter.onDestroyPresenter();
        NetworkStateReceiver.unRegisterNetworkStateReceiver(MainActivity.this);
        NetworkStateReceiver.removeRegisterObserver(netChangeObserver);
        TempControl.getmInstance(MainActivity.this).stop();
        try {
            unbindService(mRemoteConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (camera_watch != null) {
            camera_watch.release();
        }
        CardListenerService.request = null;
        super.onDestroy();

        SPUtils.setFirstInsetUPan(false);


        //TODO 暂时有空指针问题,不能处理.
        /*screenSaverDelayShow.release();
        screenSaverDelayShow = null;*/
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainNetworkQuality(NetworkQualityResult entity) {
        reFreshNetStatus(entity.getNetworkResult());
    }


    //int onlycrash = 0;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainSwipeCardInfo(SwipeCardInfo swipeCardInfo) throws IOException {
        setShowCamera(View.VISIBLE);
        Logger.t(TAG).i("SwipeCardInfo ::: onEventMainThread");
        compositeDisposable.add(rxUpdateCardUI(swipeCardInfo));
/*
        onlycrash++;

        if( onlycrash >= 10 )
        {
            ImageBlur ee=null;
            ee.blurBitMap(null,3);
        }
*/

//        updateCardUI(swipeCardInfo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainTempConfig(TempConfigEventBus tempConfig) {
        if (tempConfig.requestSuccess && tempConfig.result.isOpen()) {
            mPresenter.measureTemp(true);
        } else {
            mPresenter.measureTemp(false);
        }
        boolean isTemperatureOpen = SPUtils.getTempConfig(false);
        showTempUI(isTemperatureOpen);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventBackgroundSwipeCardInfo(SwipeCardInfo swipeCardInfo) {

        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                setShowCamera(View.VISIBLE);
            }
        });

        Logger.t(TAG).i("SwipeCardInfo ::: onEventAsync");
        //add by baodian
        //CardRecordModule.getInstance().clearTask();
        try {
            mPresenter.getDataTransfer().overTask();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mPresenter.onEventAsyncSwipeCardInfo(swipeCardInfo);
        mPresenter.classSpeakerBroadcast(swipeCardInfo);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEventBackgroundTTSInfo(TTSInfo ttsInfo) {
        mPresenter.onEventBackgroundTTSInfo(ttsInfo);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventAsyncScreenSaverCMD(ScreenSaverCMD screenSaverCMD) {
        mPresenter.onEventAsyncAd(screenSaverCMD);
    }

    /**
     * 停止屏保轮训
     */
    private void stopScreenLoop() {
        if (screenSaverDelayShow != null) {
            screenSaverDelayShow.stop();
        }
    }

    /**
     * 合法/非法卡信息ui切换
     */
    private void switchCardInfo(boolean authorized) {
        if (authorized) {
            rl_user_info.setVisibility(View.VISIBLE);
            rl_no_user_info.setVisibility(View.INVISIBLE);
        } else {
            rl_user_info.setVisibility(View.INVISIBLE);
            rl_no_user_info.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 非法卡信息ui显示
     *
     * @param cardRecord
     */
    private void showNoCardInfoUI(CardRecord cardRecord) {
//        Countly.sharedInstance().recordEvent("nocardinfo", 1);
        long schoolId = SPUtils.getSchoolId(0L);
        String targetUrl = "http://s0.bbtree.com/activity/hardware/timecard/timecard.html?cardMacId=" + cardRecord.getCard_serial_number() + "&schoolId=" + schoolId;
        if (iv_no_card_qr_code == null) {
            return;
        }
        try {
            iv_no_card_qr_code.setImageBitmap(QRCodeUtils.createQRCode(targetUrl, 210, 210));
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    /**
     * 刷卡界面更新
     *
     * @param swipeCardInfo
     */
    private void updateCardUI(SwipeCardInfo swipeCardInfo) {
        setSelectedPosition(Constant.ScreenSaverConstant.TYPE_CARD);
        if (mNetWorkMonitor != null) { // 关闭网络诊断页面
            mNetWorkMonitor.removeSelf(getFragmentManager());
        }
        resetScreenLoop();//重置及时方案
        if (BuildConfig.isDebug || BuildConfig.isFactory) {// toast 卡号
            Toast.makeText(this, "卡号：" + swipeCardInfo.getCardRecord().getCard_serial_number(), Toast.LENGTH_SHORT).show();
        }
        if (swipeCardInfo.getCardInfo() == null) {// 非法卡信息ui处理
            switchCardInfo(false);
            showNoCardInfoUI(swipeCardInfo.getCardRecord());
            updateFamily(null);
            return;
        }

        switchCardInfo(true);
        CardInfo cardInfo = swipeCardInfo.getCardInfo();
        user_name.setText(cardInfo.getName());
        Drawable[] compoundDrawables = tv_temperature.getCompoundDrawables();
        Rect bounds = compoundDrawables[0].getBounds();
        drawableTime.setBounds(bounds);
        drawableCard.setBounds(bounds);
        drawableClass.setBounds(bounds);
        drawableTemp.setBounds(bounds);
        if (cardInfo.getUserType() == 0) {
            tv_class.setText(String.format(getString(R.string.default_time), StringUtils.formatDate(new Date(swipeCardInfo.getCardRecord().getRecord_time()), "HH:mm")));
            tv_class.setCompoundDrawables(drawableTime, null, null, null);
            tv_temperature.setText(String.format(getString(R.string.default_number), swipeCardInfo.getCardInfo().getCardNumber()));
            tv_temperature.setCompoundDrawables(drawableCard, null, null, null);
            tv_time.setText(String.format(getString(R.string.default_class), cardInfo.getClassName()));
            tv_time.setCompoundDrawables(drawableClass, null, null, null);
            tv_card_no.setText(String.format(getString(R.string.default_temperature), getResources().getString(R.string.temperature_table_value)));
            tv_card_no.setCompoundDrawables(drawableTemp, null, null, null);
        } else {
            tv_class.setText(String.format(getString(R.string.default_class), cardInfo.getClassName()));
            tv_class.setCompoundDrawables(drawableClass, null, null, null);
            tv_temperature.setText(String.format(getString(R.string.default_temperature), getResources().getString(R.string.temperature_table_value)));
            tv_temperature.setCompoundDrawables(drawableTemp, null, null, null);
            tv_time.setText(String.format(getString(R.string.default_time), StringUtils.formatDate(new Date(swipeCardInfo.getCardRecord().getRecord_time()), "HH:mm")));
            tv_time.setCompoundDrawables(drawableTime, null, null, null);
            tv_card_no.setText(String.format(getString(R.string.default_number), swipeCardInfo.getCardInfo().getCardNumber()));
            tv_card_no.setCompoundDrawables(drawableCard, null, null, null);
        }

        Uri uri = Uri.parse(cardInfo.getAvatar() == null ? "" : cardInfo.getAvatar());
        Picasso.with(BBTreeApp.getApp()).load(uri)
                .transform(circleTransform)
                .placeholder(R.mipmap.baby_face_default2)
                .error(R.mipmap.baby_face_default2)
                .into(user_avatar);
        updateFamily(cardInfo);
        if (Config.withAnim && (System.currentTimeMillis() - lastAnim) > 750) {
            mCameraAnimator.start();
            lastAnim = System.currentTimeMillis();
        }
    }

    /**
     * 网络状态刷新
     *
     * @param netQuality 网络质量好坏
     */
    private void reFreshNetStatus(int netQuality) {
        if (deviceInfoFrg != null) {
            //不为空的时候说明未授权
            deviceInfoFrg.updateUI();
        }
        if (NetWorkUtil.isNetworkAvailable(MainActivity.this)) {
            showNetWarning(netQuality);
            if (NetWorkUtil.getConnectedType(MainActivity.this) == ConnectivityManager.TYPE_ETHERNET) {
                //有线状态
                if (netQuality == Constant.NetCheckResult.SUCCESS) {
                    iv_net_status.setImageResource(R.mipmap.net_available);
                } else {
                    iv_net_status.setImageResource(R.mipmap.net_abnormal);
                }
            } else if (NetWorkUtil.getConnectedType(MainActivity.this) == ConnectivityManager.TYPE_WIFI) {
                //无线状态
                if (netQuality == Constant.NetCheckResult.SUCCESS) {
                    iv_net_status.setImageResource(R.mipmap.wifi_available);
                } else {
                    iv_net_status.setImageResource(R.mipmap.wifi_abnormal);
                }
            } else {
                //未知状态
                if (netQuality == Constant.NetCheckResult.SUCCESS) {
                    iv_net_status.setImageResource(R.mipmap.net_available);
                } else {
                    iv_net_status.setImageResource(R.mipmap.net_abnormal);
                }
            }

        } else {
            showNetWarning(Constant.NetCheckResult.FAIL);
            iv_net_status.setImageResource(R.mipmap.net_unavailable);
        }
    }

    /**
     * 网络异常警告
     *
     * @param result
     */
    private void showNetWarning(int result) {
        switch (result) {
            case Constant.NetCheckResult.BAIDU_ABNORMAL:
                tv_net_warning.setVisibility(View.GONE);
                if (mFlashAnimator.isRunning()) {
                    mFlashAnimator.cancel();
                    mFlashAnimator.reset(tv_net_warning);
                }
                break;
            case Constant.NetCheckResult.BBTREE_ABNORMAL:
                tv_net_warning.setBackgroundResource(R.mipmap.warn_network_unreachable);
                tv_net_warning.setVisibility(View.VISIBLE);
                if (!mFlashAnimator.isRunning()) {
                    mFlashAnimator.start();
                }
                tv_net_warning.setText(R.string.network_warning_unreachable);
                break;
            case Constant.NetCheckResult.FAIL:
                tv_net_warning.setBackgroundResource(R.mipmap.warn_network_fail);
                tv_net_warning.setVisibility(View.VISIBLE);
                if (!mFlashAnimator.isRunning()) {
                    mFlashAnimator.start();
                }
                tv_net_warning.setText(R.string.network_warning_fail);
                break;
            case Constant.NetCheckResult.SUCCESS:
                tv_net_warning.setVisibility(View.GONE);
                if (mFlashAnimator.isRunning()) {
                    mFlashAnimator.cancel();
                    mFlashAnimator.reset(tv_net_warning);
                }
                break;
            default:
                break;
        }
    }


    /**
     * 家庭列表关系更新
     *
     * @param cardInfo
     */
    private void updateFamily(CardInfo cardInfo) {
        if (cardInfo == null || TextUtils.isEmpty(cardInfo.getFamilyString())) {
            setDefaultFamilyUI();
            return;
        }

        if ((System.currentTimeMillis() - lastAnimTime) < 200L) {
            return;
        }
        lastAnimTime = System.currentTimeMillis();

        if (cardInfo.getUserType() == 0) {
            long time = System.currentTimeMillis();
            final Calendar mCalendar = Calendar.getInstance();
            mCalendar.setTimeInMillis(time);
            int am_pm = mCalendar.get(Calendar.AM_PM);

            if (am_pm == 0) {// 上午
                Picasso.with(BBTreeApp.getApp()).load(R.mipmap.morning1).into(familyA);
                Picasso.with(BBTreeApp.getApp()).load(R.mipmap.morning2).into(familyB);
                Picasso.with(BBTreeApp.getApp()).load(R.mipmap.morning3).into(familyC);
                Picasso.with(BBTreeApp.getApp()).load(R.mipmap.morning4).into(familyD);
            } else { // 下午
                Picasso.with(BBTreeApp.getApp()).load(R.mipmap.afternoon1).into(familyA);
                Picasso.with(BBTreeApp.getApp()).load(R.mipmap.afternoon2).into(familyB);
                Picasso.with(BBTreeApp.getApp()).load(R.mipmap.afternoon3).into(familyC);
                Picasso.with(BBTreeApp.getApp()).load(R.mipmap.afternoon4).into(familyD);
            }
            familyA.setBackground(null);
            familyB.setBackground(null);
            familyC.setBackground(null);
            familyD.setBackground(null);
            familyA_name.setVisibility(View.GONE);
            familyB_name.setVisibility(View.GONE);
            familyC_name.setVisibility(View.GONE);
            familyD_name.setVisibility(View.GONE);
        } else {
            familyA.setBackground(familyADrawable);
            familyB.setBackground(familyBDrawable);
            familyC.setBackground(familyCDrawable);
            familyD.setBackground(familyDDrawable);
            List<Family> familyList = null;
            try {
                String json = cardInfo.getFamilyString();
                familyList = GsonParser.jsonToList(json, Family.class);
            } catch (Exception e) {
                setDefaultFamilyUI();
                return;
            }
            if (ListUtils.isZero(familyList)) {
                setDefaultFamilyUI();
            } else {

                Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default)
                        .into(familyA);
                familyA_name.setVisibility(View.VISIBLE);
                familyA_name.setText("未设置");

                Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default)
                        .into(familyB);
                familyB_name.setVisibility(View.VISIBLE);
                familyB_name.setText("未设置");

                Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default)
                        .into(familyC);
                familyC_name.setVisibility(View.VISIBLE);
                familyC_name.setText("未设置");

                Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default)
                        .into(familyD);
                familyD_name.setVisibility(View.VISIBLE);
                familyD_name.setText("未设置");

                int size = familyList.size();
                switch (size) {
                    case 1:
                        Family family = familyList.get(0);
                        if (!TextUtils.isEmpty(family.getAvatar())) {
                            Picasso.with(BBTreeApp.getApp()).load(Uri.parse(family.getAvatar()))
                                    .transform(circleTransform)
//                                    .placeholder(R.mipmap.parent_face_default)
                                    .error(R.mipmap.parent_face_default)
                                    .into(familyA);
                        } else {
                            Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default).into(familyA);
                        }

                        familyA_name.setText(TextUtils.isEmpty(family.getRelationship()) ? "" : family.getRelationship());
                        familyA_name.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        Family family21 = familyList.get(0);
                        if (!TextUtils.isEmpty(family21.getAvatar())) {
                            Picasso.with(BBTreeApp.getApp()).load(Uri.parse(family21.getAvatar()))
                                    .transform(circleTransform)
//                                    .placeholder(R.mipmap.parent_face_default)
                                    .error(R.mipmap.parent_face_default)
                                    .into(familyA);
                        } else {
                            Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default).into(familyA);
                        }
                        familyA_name.setText(TextUtils.isEmpty(family21.getRelationship()) ? "" : family21.getRelationship());
                        familyA_name.setVisibility(View.VISIBLE);

                        Family family22 = familyList.get(1);
                        if (!TextUtils.isEmpty(family22.getAvatar())) {
                            Picasso.with(BBTreeApp.getApp()).load(Uri.parse(family22.getAvatar()))
                                    .transform(circleTransform)
//                                    .placeholder(R.mipmap.parent_face_default)
                                    .error(R.mipmap.parent_face_default)
                                    .into(familyB);
                        } else {
                            Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default).into(familyB);
                        }
                        familyB_name.setText(TextUtils.isEmpty(family22.getRelationship()) ? "" : family22.getRelationship());
                        familyB_name.setVisibility(View.VISIBLE);

                        break;
                    case 3:

                        Family family31 = familyList.get(0);
                        if (!TextUtils.isEmpty(family31.getAvatar())) {
                            Picasso.with(BBTreeApp.getApp()).load(Uri.parse(family31.getAvatar()))
                                    .transform(circleTransform)
//                                    .placeholder(R.mipmap.parent_face_default)
                                    .error(R.mipmap.parent_face_default)
                                    .into(familyA);
                        } else {
                            Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default).into(familyA);
                        }
                        familyA_name.setText(TextUtils.isEmpty(family31.getRelationship()) ? "" : family31.getRelationship());
                        familyA_name.setVisibility(View.VISIBLE);

                        Family family32 = familyList.get(1);
                        if (!TextUtils.isEmpty(family32.getAvatar())) {
                            Picasso.with(BBTreeApp.getApp()).load(Uri.parse(family32.getAvatar()))
                                    .transform(circleTransform)
//                                    .placeholder(R.mipmap.parent_face_default)
                                    .error(R.mipmap.parent_face_default)
                                    .into(familyB);
                        } else {
                            Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default).into(familyB);
                        }
                        familyB_name.setText(TextUtils.isEmpty(family32.getRelationship()) ? "" : family32.getRelationship());
                        familyB_name.setVisibility(View.VISIBLE);

                        Family family33 = familyList.get(2);
                        if (!TextUtils.isEmpty(family33.getAvatar())) {
                            Picasso.with(BBTreeApp.getApp()).load(Uri.parse(family33.getAvatar()))
                                    .transform(circleTransform)
//                                    .placeholder(R.mipmap.parent_face_default)
                                    .error(R.mipmap.parent_face_default)
                                    .into(familyC);
                        } else {
                            Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default).into(familyC);
                        }
                        familyC_name.setText(TextUtils.isEmpty(family33.getRelationship()) ? "" : family33.getRelationship());
                        familyC_name.setVisibility(View.VISIBLE);

                        break;
                    case 4:

                        Family family41 = familyList.get(0);
                        if (!TextUtils.isEmpty(family41.getAvatar())) {
                            Picasso.with(BBTreeApp.getApp()).load(Uri.parse(family41.getAvatar()))
                                    .transform(circleTransform)
//                                    .placeholder(R.mipmap.parent_face_default)
                                    .error(R.mipmap.parent_face_default)
                                    .into(familyA);
                        } else {
                            Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default).into(familyA);
                        }
                        familyA_name.setText(TextUtils.isEmpty(family41.getRelationship()) ? "" : family41.getRelationship());
                        familyA_name.setVisibility(View.VISIBLE);

                        Family family42 = familyList.get(1);
                        if (!TextUtils.isEmpty(family42.getAvatar())) {
                            Picasso.with(BBTreeApp.getApp()).load(Uri.parse(family42.getAvatar()))
                                    .transform(circleTransform)
//                                    .placeholder(R.mipmap.parent_face_default)
                                    .error(R.mipmap.parent_face_default)
                                    .into(familyB);
                        } else {
                            Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default).into(familyB);
                        }
                        familyB_name.setText(TextUtils.isEmpty(family42.getRelationship()) ? "" : family42.getRelationship());
                        familyB_name.setVisibility(View.VISIBLE);

                        Family family43 = familyList.get(2);
                        if (!TextUtils.isEmpty(family43.getAvatar())) {
                            Picasso.with(BBTreeApp.getApp()).load(Uri.parse(family43.getAvatar()))
                                    .transform(circleTransform)
//                                    .placeholder(R.mipmap.parent_face_default)
                                    .error(R.mipmap.parent_face_default)
                                    .into(familyC);
                        } else {
                            Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default).into(familyC);
                        }
                        familyC_name.setText(TextUtils.isEmpty(family43.getRelationship()) ? "" : family43.getRelationship());
                        familyC_name.setVisibility(View.VISIBLE);

                        Family family44 = familyList.get(3);
                        if (!TextUtils.isEmpty(family44.getAvatar())) {
                            Picasso.with(BBTreeApp.getApp()).load(Uri.parse(family44.getAvatar()))
                                    .transform(circleTransform)
//                                    .placeholder(R.mipmap.parent_face_default)
                                    .error(R.mipmap.parent_face_default)
                                    .into(familyD);
                        } else {
                            Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default).into(familyD);
                        }
                        familyD_name.setText(TextUtils.isEmpty(family44.getRelationship()) ? "" : family44.getRelationship());
                        familyD_name.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }
    }


    private void initCardRes() {
        drawableTime = getResources().getDrawable(R.mipmap.icon_time);
        drawableCard = getResources().getDrawable(R.mipmap.icon_card);
        drawableClass = getResources().getDrawable(R.mipmap.icon_class);
        drawableTemp = getResources().getDrawable(R.mipmap.icon_temperature);
    }

    private void initFamily() {
        familyADrawable = getResources().getDrawable(R.mipmap.main_family_bg_a);
        familyBDrawable = getResources().getDrawable(R.mipmap.main_family_bg_b);
        familyCDrawable = getResources().getDrawable(R.mipmap.main_family_bg_c);
        familyDDrawable = getResources().getDrawable(R.mipmap.main_family_bg_d);
    }

    /**
     * 家庭关系设置默认
     */
    private void setDefaultFamilyUI() {

        familyA_name.setVisibility(View.VISIBLE);
        familyA_name.setText("未设置");

        familyB_name.setVisibility(View.VISIBLE);
        familyB_name.setText("未设置");

        familyC_name.setVisibility(View.VISIBLE);
        familyC_name.setText("未设置");

        familyD_name.setVisibility(View.VISIBLE);
        familyD_name.setText("未设置");

        familyA.setBackground(familyADrawable);
        familyB.setBackground(familyBDrawable);
        familyC.setBackground(familyCDrawable);
        familyD.setBackground(familyDDrawable);

        Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default).into(familyA);
        Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default).into(familyB);
        Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default).into(familyC);
        Picasso.with(BBTreeApp.getApp()).load(R.mipmap.parent_face_default).into(familyD);
    }

    @Override
    public void onBackPressed() {
        long nowCurrentMillion = System.currentTimeMillis();
        if (nowCurrentMillion - lastClickBackKey <= Constant.KeyPress.BACK_KEY_INTERVAL) {
            finish();
        } else {
            Toast.makeText(this, R.string.press_double_back_key_for_exit, Toast.LENGTH_SHORT).show();
            lastClickBackKey = nowCurrentMillion;
        }
    }

    @Override
    public void setPresenter(MainActivityContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showMachineInfo(String appVersion, String deviceAlias) {
        tv_versionName.setText(appVersion);
        tv_sn.setText(deviceAlias);
    }

    @Override
    public void showQRCode(Bitmap qrCode) {
        iv_qr_code.setImageBitmap(qrCode);
    }

    @Override
    public void showSchoolName(String schoolName) {
        tv_schoolName.setText(schoolName);
    }

    @Override
    public void showTempUI(boolean open) {
        iv_bluetooth_status.setVisibility(open ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showFragmentBySchoolId(boolean b) {
        if (b) {
            screenSaverDelayShow = new ScreenSaverDelayShow(this, R.id.floating_layer, enterScreenSaverTime, mPresenter);//屏保初始化
        } else {
            deviceInfoFrg = new DeviceInfoFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.floating_layer, deviceInfoFrg);
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void resetScreenLoop() {
        fixIsPushNum();
        if (camera_watch != null) {
            camera_watch.startPreview();
        }
        if (screenSaverDelayShow != null) {
            screenSaverDelayShow.resetDelayTime(enterScreenSaverTime);
        }
    }

    /**
     * 修改是否上传固定广告数量
     */
    private void fixIsPushNum() {
        isPushNum = false;
        Observable.timer(DeviceConfigUtils.getConfig().getScreenSaverDelay(), TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        isPushNum = true;
                    }
                });
    }

    @Override
    public void showCardNo(String s) {
        tv_card_no.setText(String.format(getString(R.string.default_temperature), s));
    }

    @Override
    public void showNowTemp(String s) {
        tv_temperature.setText(String.format(getString(R.string.default_temperature), s));
    }

    @Override
    public void showTempIcon(int imageResource) {
        iv_bluetooth_status.setImageResource(imageResource);
    }

    @Override
    public byte[] takePhotoBytes() {
        Logger.t(TAG).i("takePhoto");
        byte[] photoBytes = new byte[0];
        if (camera_watch != null) {
            photoBytes = camera_watch.getNowFrame();
        }
        return photoBytes;
    }

    @Override
    public void showNoUploaded(String recordCount, String pictureCount) {
        tv_no_uploaded_records.setText(recordCount);
        tv_no_uploaded_pictures.setText(pictureCount);
    }

    @Override
    public void setSelectedPosition(int position) {
        Logger.i("setSelectedPosition：" + position);
        iv_swipe_card.setSelected(position == Constant.ScreenSaverConstant.TYPE_CARD);
        iv_notification.setSelected(position == Constant.ScreenSaverConstant.TYPE_TEXT);
//        iv_recipe.setSelected(position == 3);
        iv_recipe.setVisibility(View.GONE);
        iv_picture.setSelected(position == Constant.ScreenSaverConstant.TYPE_PIC || position == Constant.ScreenSaverConstant.TYPE_ADS);
    }

    @Override
    public void showScreenUI(final List<Ad> ads) {

        mAdBanner.setPages(new CustomBanner.ViewCreator<Ad>() {
            @Override
            public View createView(Context context, int position) {
                Ad ad = ads.get(mAdBanner.getActualPosition(position));
                Logger.i("ad mAdBanner" + "::::::url1:" + (null != ad.getPic1() ? ad.getPic1() : "default"));
                RoundedImageView mView = (RoundedImageView) LayoutInflater.from(MainActivity.this).inflate(R.layout.main_ad_item, null);
                String url;
                if (ScreenUtils.getOrientation(BBTreeApp.getApp()) != Configuration.ORIENTATION_LANDSCAPE) {// 固定轮播广告位横竖屏相反
                    url = ad.getPic1();
                } else {
                    url = ad.getPic2();
                }
                RequestCreator loadAd;
                if (TextUtils.isEmpty(url))
                    loadAd = Picasso.with(BBTreeApp.getApp()).load(R.mipmap.main_ad_default);
                else
                    loadAd = Picasso.with(BBTreeApp.getApp()).load(url);

                loadAd.placeholder(R.mipmap.main_ad_default)
                        .error(R.mipmap.main_ad_default)
//                        .transform(new CompressionTransformer(mContext))
                        .config(Bitmap.Config.RGB_565).into(mView);

                return mView;
            }

            @Override
            public void updateUI(Context context, View view, int position, Ad data) {
                Ad ad = ads.get(mAdBanner.getActualPosition(position));
                mPresenter.playNum(ad, isPushNum);
            }
        }, ads);


        if (ads.size() > 1) {
            //设置指示器类型，有普通指示器(ORDINARY)、数字指示器(NUMBER)和没有指示器(NONE)三种类型。
            mAdBanner.setIndicatorStyle(CustomBanner.IndicatorStyle.ORDINARY);

            //设置两个点图片作为翻页指示器，只有指示器为普通指示器(ORDINARY)时有用。
            //第一个参数是指示器的选中的样式，第二个参数是指示器的未选中的样式。
            mAdBanner.setIndicatorRes(R.mipmap.ic_focus, R.mipmap.ic_focus_select);

            //设置指示器的方向。
            //这个方法跟在布局中设置app:indicatorGravity是一样的。
            mAdBanner.setIndicatorGravity(CustomBanner.IndicatorGravity.CENTER);

            mAdBanner.startTurning(adScrollTime);

        } else {
            mAdBanner.setIndicatorStyle(CustomBanner.IndicatorStyle.NONE);
            mAdBanner.setIndicatorRes(R.color.main_half_transparent, R.mipmap.ic_focus);
            mAdBanner.stopTurning();
        }
    }

    @Override
    public void showDefaultImg() {
        List<Ad> ads = new ArrayList<>();
        ads.clear();
        Ad bean = new Ad();
        ads.add(bean);
        showScreenUI(ads);
    }

    @Override
    public void showMainUi() {
        setShowCamera(View.VISIBLE);
    }

    private void clearTask() {
        if (compositeDisposable != null) {
//            compositeDisposable.dispose();
            compositeDisposable.clear();
        }
    }


    Cancellable cancellable = new Cancellable() {
        @Override
        public void cancel() throws Exception {


        }
    };

    private Disposable rxUpdateCardUI(final SwipeCardInfo swipeCardInfo) {
        clearTask();
        return Observable.defer(new Callable<Observable<SwipeCardInfo>>() {
            @Override
            public Observable<SwipeCardInfo> call() throws Exception {
                return Observable.just(swipeCardInfo);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<SwipeCardInfo>() {
                    @Override
                    public void onNext(@NonNull SwipeCardInfo swipeCardInfo) {
                        updateCardUI(swipeCardInfo);

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainScreenSaverUPan(ScreenSaverUPanEvent screenSaverUPan) {
        Logger.t("ScreenSaverUPanEvent").i("ScreenSaverUPanEvent::::activity" + screenSaverUPan.toString());

        fixIsPushNum();
        if (camera_watch != null && !camera_watch.getPreviewing()) {
            camera_watch.startPreview();
        }
        if (screenSaverDelayShow != null) {
            screenSaverDelayShow.resetDelayTime(screenSaverUPan.getToScreenDelay());//插拔u盘的特殊重置
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventShowFactoryTip(String factoryTip) {
        Logger.i("onEventShowFactoryTip:" + factoryTip);
        String target = factoryTip.replace("-8:", "反8H10D：\t\t\t\t")
                .replace("-6:", "反6H8D：\t\t\t\t")
                .replace("8:", "正8H10D：\t\t\t\t")
                .replace("6:", "正6H8D：\t\t\t\t");
        String[] split = target.split("！");
        if (factoryTipDialog != null && factoryTipDialog.isShowing()) factoryTipDialog.dismiss();
        factoryTipDialog = new AlertDialog.Builder(this).setTitle("解析协议及结果").setItems(split, null).show();
    }

    @OnClick({R.id.tc_time})
    void switchFactoryTip() {
        if (!BuildConfig.isDebug && !BuildConfig.isFactory) {
            return;
        }
        Logger.t(TAG).i("设置工厂包提示开关");
        if (factoryTipSwitchDialog != null) {
            if (factoryTipSwitchDialog.isShowing()) return;
            else factoryTipSwitchDialog.show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.factory_tip);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SPUtils.setFactoryTip(true);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SPUtils.setFactoryTip(false);
            }
        });
        factoryTipSwitchDialog = builder.create();
        factoryTipSwitchDialog.show();
    }

    /**
     * 摄像头显示与否
     *
     * @param visibility
     */
    public void setShowCamera(int visibility) {
        if (mainUI.getVisibility() == visibility) return;
        mainUI.setVisibility(visibility);
        leftIcon.setVisibility(visibility);
        rightIcon.setVisibility(visibility);
//        camera_watch.setVisibility(visibility);
    }

}