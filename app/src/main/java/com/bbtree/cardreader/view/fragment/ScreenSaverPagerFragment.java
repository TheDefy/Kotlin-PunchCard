package com.bbtree.cardreader.view.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.bbtree.baselib.base.BaseFragment;
import com.bbtree.baselib.net.BaseParam;
import com.bbtree.baselib.net.GsonParser;
import com.bbtree.baselib.utils.ListUtils;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.BuildConfig;
import com.bbtree.cardreader.R;
import com.bbtree.cardreader.adapter.ScreenSaverPagerAdapter;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.entity.ScreenSaverBean;
import com.bbtree.cardreader.entity.dao.PlayNum;
import com.bbtree.cardreader.entity.eventbus.PauseMusicEvent;
import com.bbtree.cardreader.entity.eventbus.ScreenSaverCMD;
import com.bbtree.cardreader.entity.eventbus.ScreenSaverCircleEvent;
import com.bbtree.cardreader.entity.eventbus.ScreenSaverUPanEvent;
import com.bbtree.cardreader.entity.eventbus.ServiceCheckEvent;
import com.bbtree.cardreader.entity.requestEntity.Ad;
import com.bbtree.cardreader.entity.requestEntity.GetAdResData;
import com.bbtree.cardreader.entity.requestEntity.ScreenSaverResult;
import com.bbtree.cardreader.model.AdModule;
import com.bbtree.cardreader.model.DataInfoModule;
import com.bbtree.cardreader.receiver.NetworkStateReceiver;
import com.bbtree.cardreader.service.UpdateCheckService;
import com.bbtree.cardreader.utils.DeviceConfigUtils;
import com.bbtree.cardreader.utils.NetWorkUtil;
import com.bbtree.cardreader.utils.SPUtils;
import com.bbtree.cardreader.view.activity.MainActivity;
import com.bbtree.cardreader.view.widget.VerticalMarqueeTextView;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;


/**
 * Function:
 * Created by BBTree Team
 * Author: LiuJihui
 * Create Date: 2015/12/14
 * Create Time: 14:55
 */
public class ScreenSaverPagerFragment extends BaseFragment {

    @BindView(R.id.main_pager)
    ViewPager mViewPager;
    @BindView(R.id.frame_dots)
    LinearLayout dots_layout;   //小圆点linearLayout

    private ImageView img_last_circle; //保存上一个小圆点对象

    private static final long CHECK_UPDATE_TIME = 1000 * 60 * 10;
    private Timer timer;   //从服务器获取ad 数据 定时器
    public long SCREEN_TIME = Constant.ScreenSaver.SCREENSAVER_INTERVAL;  // 轮播时间
    private static final int DATA_OK = 100;    //数据正确
    private static final int DATA_ERROR = 101; //数据错误
    private static final int TIME_UP = 200;    //计时结束
    private ScreenSaverPagerAdapter adapter;
    private int num = 0;  //当前currentItem
    private ArrayList<ScreenSaverBean> screenSavers = new ArrayList<>();
    private ScreenSaverResult last_screenSaverResult;
    private MyPagerChangeListener myPagerChangeListener;
    private Handler mHandleCheckUpdate;
    private MainActivity mainActivity;

    private boolean isPullAd = true;// 是否拉去广告数据 true 是
    private boolean isScreenSaver = true;// 是否拉去轮播数据 true 是

    private boolean isUPanDelete;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case DATA_OK:// 清楚数据 显示新的数据
                    ScreenSaverResult screenSaverResult = (ScreenSaverResult) msg.obj;
                    if (screenSaverResult != null) {
                        closeCurrentItemVideo();
                        stopCircle();
                        setScreenSaversClear();
                        showData(screenSaverResult);
                    }
                    break;
                case DATA_ERROR:// 显示默认图
                    showDefaultImg();
                    break;
                case TIME_UP:// 加载数据
                    loadData(null);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private Handler pagerHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (isCurrentItemMarquee()) {
                num--;
                return false;
            }

            mViewPager.setCurrentItem(msg.what);
            setSelectionAndMarquee();
            return false;
        }
    });

    private boolean isCurrentItemMarquee() {
        View view = adapter.getPrimaryItem();
        if (view != null) {
            VerticalMarqueeTextView notice_content = (VerticalMarqueeTextView) view.findViewById(R.id.notice_content);
            if (notice_content != null) {
                return notice_content.isMarquee();
            }
        }
        return false;
    }

    /**
     * 关闭正在播放的轮播视频
     */
    private void closeCurrentItemVideo() {
        View view = adapter.getPrimaryItem();
        if (view != null) {
            VideoView mVideoView = (VideoView) view.findViewById(R.id.vv_video);
            if (mVideoView != null && mVideoView.isPlaying()) {
                mVideoView.stopPlayback();
                // 正在播放重置轮播
                ScreenSaverUPanEvent screenSaverUPan = new ScreenSaverUPanEvent();
                screenSaverUPan.setDelete(true);
                screenSaverUPan.setToScreenDelay(0);
                EventBus.getDefault().post(screenSaverUPan);
            }
        }
    }

    @Override
    public int contentView() {
        return R.layout.pager_screen_saver_frg;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        ButterKnife.bind(this, getContentView());
        if (mContext instanceof MainActivity)
            mainActivity = (MainActivity) mContext;
        SCREEN_TIME = DeviceConfigUtils.getConfig().getScreenSaverInterval();
        adapter = new ScreenSaverPagerAdapter(mContext);
        mViewPager.setAdapter(adapter);  //设置适配器
        myPagerChangeListener = new MyPagerChangeListener(mContext);
        mViewPager.addOnPageChangeListener(myPagerChangeListener); //设置滑动监听
        mViewPager.setCurrentItem(num);  //设置第一个显示的position
        showDefaultImg();
        if (NetWorkUtil.isNetworkAvailable(BBTreeApp.getApp())) {
            beginTimer();   //开启定时获取ad数据
        } else {
            ScreenSaverResult resultObject = new ScreenSaverResult();
            List<String> videoUrls = SPUtils.getStrListValue(Constant.UPanConstant.VIDEO_LIST_STR_KEY);
            if (!ListUtils.isZero(videoUrls)) {
                List<ScreenSaverResult.Videos> videos = new ArrayList<ScreenSaverResult.Videos>();
                for (String s : videoUrls) {
                    File file = new File(s);
                    if (file.exists() && file.length() > 0) {
                        ScreenSaverResult.Videos video = new ScreenSaverResult.Videos();
                        video.setUrl(s);
                        videos.add(video);
                    }
                }
                if (resultObject != null) {
                    resultObject.setVideos(videos);
                }
                mHandler.obtainMessage(DATA_OK, resultObject).sendToTarget(); //刷新显示数据
            }
        }
        NetworkStateReceiver.registerObserver(netChangeObserver);
    }

    NetworkStateReceiver.NetChangeObserver netChangeObserver = new NetworkStateReceiver.NetChangeObserver() { // 网络连接的监听事件
        @Override
        public void onConnect(NetWorkUtil.NetType type) {
            Logger.i("网络已连接");
            mHandler.obtainMessage(TIME_UP).sendToTarget();
        }

        @Override
        public void onDisConnect() {
            Logger.i("网络已断开");
        }
    };

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (this.isAdded()) {
                stopCircle();
                isCheckVersion(false);
            }
        } else {
            if (this.isAdded() && this.isVisible()) {
                startCircle();
                checkVersionUpdate();
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (this.isAdded() && this.isVisible()) {
            checkVersionUpdate();
        }
    }

    /**
     * 屏保页面十分钟一次检测版本更新
     */
    public void checkVersionUpdate() {
        if (mHandleCheckUpdate == null) {
            mHandleCheckUpdate = new Handler();
            if (BuildConfig.isDebug)
                UpdateCheckService.startCheckUpdate(getActivity());
        }
        mHandleCheckUpdate.postDelayed(mCheckVersionUpdateRunnable, CHECK_UPDATE_TIME);
    }

    private Runnable mCheckVersionUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            UpdateCheckService.startCheckUpdate(getActivity());
            if (mHandleCheckUpdate != null)
                mHandleCheckUpdate.postDelayed(mCheckVersionUpdateRunnable, CHECK_UPDATE_TIME);
        }
    };

    private void isCheckVersion(boolean isStartCheck) {
        if (mHandleCheckUpdate == null)
            return;
        if (isStartCheck) {
            mHandleCheckUpdate.removeCallbacks(mCheckVersionUpdateRunnable);
            mHandleCheckUpdate.postDelayed(mCheckVersionUpdateRunnable, CHECK_UPDATE_TIME);
        } else {
            mHandleCheckUpdate.removeCallbacks(mCheckVersionUpdateRunnable);
            mHandleCheckUpdate = null;
        }
    }

    /**
     * 开启定时获取数据
     */
    private void beginTimer() {
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mHandler.obtainMessage(TIME_UP).sendToTarget();
                }
            }, 0, Constant.ScreenSaver.REFRESH_SCREENSAVER);
        }

    }

    /**
     * 开启轮播
     */
    public void startCircle() {
        pagerHandler.postDelayed(pagerRun, SCREEN_TIME);
    }

    /**
     * 清空list dots_layout
     */
    public void setScreenSaversClear() {
        if (screenSavers != null) {
            screenSavers.clear();
            adapter.setList(screenSavers);
        }
        if (dots_layout != null) {
            dots_layout.removeAllViews();
        }
    }

    /**
     * 停止轮播
     */
    public void stopCircle() {
        pagerHandler.removeCallbacks(pagerRun);
        pagerHandler.removeMessages(num);
    }

    private Runnable pagerRun = new Runnable() {
        @Override
        public void run() {
            pagerHandler.sendEmptyMessage(++num);
            pagerHandler.postDelayed(this, SCREEN_TIME);
        }
    };

    private void loadData(String sn) {
        Map saverMap = new HashMap();
        saverMap.put("deviceId", BaseParam.getDeviceId());
        saverMap.put("sn", isScreenSaver ? sn : "");

        Map adMap = new HashMap();
        adMap.put("deviceId", BaseParam.getDeviceId());
        adMap.put("sn", isPullAd ? sn : "");

        Observable.zip(DataInfoModule.getInstance().getScreenSaver(saverMap), AdModule.getInstance().getAd(adMap), new BiFunction<ScreenSaverResult, GetAdResData, ScreenSaverResult>() {
            @Override
            public ScreenSaverResult apply(@NonNull ScreenSaverResult screenSaverResult, @NonNull GetAdResData result) throws Exception {
                List<Ad> ads = new ArrayList<Ad>();
                if (null != result && null != result.data) {
                    List<Ad> adsTemp = result.data.getAds();
                    if (!ListUtils.isZero(adsTemp)) {
                        for (Ad temp : adsTemp) {
                            if (temp.getAdType() == 1) {
                                ads.add(temp);
                            }
                        }
                    }
                }
                if (!ListUtils.isZero(ads) && screenSaverResult != null) {
                    screenSaverResult.setAds(ads);
                }
                return screenSaverResult;
            }
        })
                .subscribe(new Observer<ScreenSaverResult>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull ScreenSaverResult resultObject) {
                        List<String> videoUrls = SPUtils.getStrListValue(Constant.UPanConstant.VIDEO_LIST_STR_KEY);
                        if (!ListUtils.isZero(videoUrls)) {
                            List<ScreenSaverResult.Videos> videos = new ArrayList<ScreenSaverResult.Videos>();
                            for (String s : videoUrls) {
                                File file = new File(s);
                                if (file.exists() && file.length() > 0) {
                                    ScreenSaverResult.Videos video = new ScreenSaverResult.Videos();
                                    video.setUrl(s);
                                    videos.add(video);
                                }
                            }
                            if (resultObject != null) {
                                resultObject.setVideos(videos);
                            }
                        }
                        if (!equalsScreenSaverResult(resultObject)) {
                            mHandler.obtainMessage(DATA_OK, resultObject).sendToTarget(); //刷新显示数据
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    //显示默认图
    private void showDefaultImg() {
        screenSavers.clear();
        adapter.setList(screenSavers);
        if (mainActivity != null) {
            mainActivity.setSelectedPosition(Constant.ScreenSaverConstant.TYPE_PIC);
        }

        ScreenSaverBean bean = new ScreenSaverBean();
        bean.setType(Constant.ScreenSaverConstant.TYPE_PIC);
        screenSavers.add(bean);
        adapter.setList(screenSavers);
        initDots(screenSavers);
        startCircle();
    }

    private void showData(ScreenSaverResult screenSaverResult) {
        List<Ad> ads;
        List<ScreenSaverResult.Notices> notices;
        List<ScreenSaverResult.Pictures> pictures;
        List<ScreenSaverResult.Videos> videos;
        ads = screenSaverResult.getAds();
        notices = screenSaverResult.getNotices();
        pictures = screenSaverResult.getPictures();
        videos = screenSaverResult.getVideos();

        if (SPUtils.isFirstInsetUPan()) {
            if (!ListUtils.isZero(videos)) {
                for (int i = 0; i < videos.size(); i++) {
                    ScreenSaverBean bean = new ScreenSaverBean();
                    bean.setType(Constant.ScreenSaverConstant.TYPE_VIDEO);
                    bean.setVideo(videos.get(i));
                    screenSavers.add(bean);
                }
            }
        }

        if (ads != null && !ads.isEmpty()) {
            for (int i = 0; i < ads.size(); i++) {
                ScreenSaverBean bean = new ScreenSaverBean();
                bean.setType(Constant.ScreenSaverConstant.TYPE_ADS);
                bean.setAd(ads.get(i));
                screenSavers.add(bean);
            }
        }
        if (notices != null && !notices.isEmpty()) {
            for (int i = 0; i < notices.size(); i++) {
                ScreenSaverBean bean = new ScreenSaverBean();
                bean.setType(Constant.ScreenSaverConstant.TYPE_TEXT);
                bean.setNotice(notices.get(i));
                screenSavers.add(bean);
            }
        }
        if (pictures != null && !pictures.isEmpty()) {
            for (int i = 0; i < pictures.size(); i++) {
                ScreenSaverBean bean = new ScreenSaverBean();
                bean.setType(Constant.ScreenSaverConstant.TYPE_PIC);
                bean.setPicture(pictures.get(i));
                screenSavers.add(bean);
            }
        }

        if (!SPUtils.isFirstInsetUPan()) {
            if (!ListUtils.isZero(videos)) {
                for (int i = 0; i < videos.size(); i++) {
                    ScreenSaverBean bean = new ScreenSaverBean();
                    bean.setType(Constant.ScreenSaverConstant.TYPE_VIDEO);
                    bean.setVideo(videos.get(i));
                    screenSavers.add(bean);
                }
            }
        }

        if (isUPanDelete) {
            List<ScreenSaverBean> tempScreenSavers = new ArrayList<>();
            if (!ListUtils.isZero(screenSavers)) {
                for (ScreenSaverBean temp : screenSavers) {
                    if (temp.getType() == Constant.ScreenSaverConstant.TYPE_VIDEO) {
                        tempScreenSavers.add(temp);
                    }
                }
            }
            screenSavers.removeAll(tempScreenSavers);
        }

        if (screenSavers.isEmpty()) {
            showDefaultImg();
            return;
        }

        adapter.setList(screenSavers);
        initDots(screenSavers);
        startCircle();
        setSelectionAndMarquee();
    }

    private void addVideoData() {
        List<String> videoUrls = SPUtils.getStrListValue(Constant.UPanConstant.VIDEO_LIST_STR_KEY);
        if (ListUtils.isZero(videoUrls)) return;
        for (String s : videoUrls) {
            File file = new File(s);
            if (file.exists() && file.length() > 0) {
                ScreenSaverResult.Videos video = new ScreenSaverResult.Videos();
                video.setUrl(s);
                ScreenSaverBean bean = new ScreenSaverBean();
                bean.setType(Constant.ScreenSaverConstant.TYPE_VIDEO);
                bean.setVideo(video);
                screenSavers.add(bean);
            }
        }
    }

    private void setSelectionAndMarquee() {
        if (mainActivity != null) {
            if (screenSavers != null && screenSavers.size() > 0) {
                ScreenSaverBean screenSaver = screenSavers.get(mViewPager.getCurrentItem() % screenSavers.size());
                Logger.i("data setSelectedPosition" + "::::::notice:" + (null != screenSaver.getNotice() ? screenSaver.getNotice().getContent() : "")
                        + "::::picture" + (null != screenSaver.getPicture() ? screenSaver.getPicture().getTitle() : ""));
                int type = screenSaver.getType();
                mainActivity.setSelectedPosition(type);
                if (Constant.ScreenSaverConstant.TYPE_NONE == type
                        || Constant.ScreenSaverConstant.TYPE_TEXT == type) {
                    View view = adapter.getPrimaryItem();
                    if (view != null) {
                        VerticalMarqueeTextView notice_content = (VerticalMarqueeTextView) view.findViewById(R.id.notice_content);
                        if (notice_content != null) {
                            stopCircle();
                            notice_content.startMarquee();
                        }
                    }
                } else if (Constant.ScreenSaverConstant.TYPE_ADS == type) {
                    //记录轮播次数并存本地库
                    PlayNum num = new PlayNum();
                    Ad ad = screenSaver.getAd();
                    if (null != ad && ad.getId() > 0) {
                        int id = ad.getId();
                        num.setAdId(id);
                        AdModule.getInstance().insert(num);
                    }
                } else if (Constant.ScreenSaverConstant.TYPE_VIDEO == type) {
                    // 播放视频时 暂停音乐
                    PauseMusicEvent pauseMusicEvent = new PauseMusicEvent();
                    pauseMusicEvent.type = PauseMusicEvent.PauseMusicType.pause;
                    pauseMusicEvent.setPause(true);
                    pauseMusicEvent.setDelayTime(Integer.MAX_VALUE);
                    EventBus.getDefault().post(pauseMusicEvent);

                    View view = adapter.getPrimaryItem();
                    if (view != null) {
                        mainActivity.setShowCamera(View.INVISIBLE);
                        final VideoView mVideoView = (VideoView) view.findViewById(R.id.vv_video);
                        if (mVideoView != null) {
                            mVideoView.setZOrderMediaOverlay(true);
                            ScreenSaverResult.Videos video = screenSaver.getVideo();
                            if (video == null || TextUtils.isEmpty(video.getUrl())) return;
                            stopCircle();
                            mVideoView.start();
//                            mVideoView.requestFocus();
                            mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    pagerHandler.postDelayed(pagerRun, 0);

                                    // 播放视频完 续播音乐
                                    PauseMusicEvent pauseMusicEvent = new PauseMusicEvent();
                                    pauseMusicEvent.type = PauseMusicEvent.PauseMusicType.pause;
                                    pauseMusicEvent.setPause(false);
                                    EventBus.getDefault().post(pauseMusicEvent);
                                }
                            });
                            mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                @Override
                                public boolean onError(MediaPlayer mp, int what, int extra) {
                                    Logger.i("MediaPlayer onError what:" + what);
                                    pagerHandler.postDelayed(pagerRun, 0);
                                    return true;
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    /**
     * 如果服务器返回对象和上次对象相等返回true ，
     */
    private boolean equalsScreenSaverResult(ScreenSaverResult result) {
        if (result == null) {
            return true;
        }
        if (last_screenSaverResult == null) {
            last_screenSaverResult = result;
            return false;
        }
        String lastObj = GsonParser.parserToJson(last_screenSaverResult);
        String newObj = GsonParser.parserToJson(result);
        if (lastObj.equals(newObj)) {
            return true;
        }
        last_screenSaverResult = result;
        return false;
    }

    private void initDots(ArrayList<ScreenSaverBean> list) {
        if (dots_layout == null || !isAdded()) {
            return;
        }
        dots_layout.removeAllViews();
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_focus_select);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(bitmap.getWidth() / 2, bitmap.getWidth() / 2);
        layoutParams.setMargins(bitmap.getWidth() / 3, 0, bitmap.getWidth() / 3, 0);
        // 循环取得小点图片
        for (int i = 0; i < list.size(); i++) {
            ImageView img = new ImageView(mContext);
            img.setLayoutParams(new ViewGroup.LayoutParams(bitmap.getWidth() / 2, bitmap.getHeight() / 2));
            img.setBackgroundResource(R.mipmap.ic_focus_select);
            img.setPadding(bitmap.getWidth() / 2, 0, bitmap.getWidth() / 2, 0);
            img.setLayoutParams(layoutParams);
            dots_layout.addView(img);
        }
        if (list.size() > 0) {
            img_last_circle = (ImageView) dots_layout.getChildAt(0);
            img_last_circle.setBackgroundResource(R.mipmap.ic_focus);
        }
        bitmap.recycle();
    }

    public class MyPagerChangeListener implements ViewPager.OnPageChangeListener {

        private Context mContext = null;

        public MyPagerChangeListener(Context context) {
            mContext = context;
        }

        //页面被华东
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        //页面被选中
        @Override
        public void onPageSelected(int position) {
            if (img_last_circle != null) {
                img_last_circle.setBackgroundResource(R.mipmap.ic_focus_select);
            }
            position = position % screenSavers.size();
            if (dots_layout != null && isAdded()) {
                ImageView img = (ImageView) dots_layout.getChildAt(position);
                if (img != null) {
                    img.setBackgroundResource(R.mipmap.ic_focus);
                    img_last_circle = img;
                }
            }
        }

        //页面华东状态改变
        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case ViewPager.SCROLL_STATE_DRAGGING:
                    break;
                case ViewPager.SCROLL_STATE_IDLE:
                    //稳定  上滑上滑 停止
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    //滑动  隐藏
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        isCheckVersion(false);
        setScreenSaversClear();
        stopCircle();
        mainActivity = null;
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (myPagerChangeListener != null) {
            mViewPager.removeOnPageChangeListener(myPagerChangeListener);
        }
        NetworkStateReceiver.removeRegisterObserver(netChangeObserver);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventAsyncScreenSaverCMD(ScreenSaverCMD screenSaverCMD) {
        if (screenSaverCMD.cmd == ScreenSaverCMD.ScreenSaverAction.screenSaveUpdate) {
            isScreenSaver = true;
            isPullAd = false;
        } else if (screenSaverCMD.cmd == ScreenSaverCMD.ScreenSaverAction.adUpdate) {
            isPullAd = true;
            isScreenSaver = false;
        }
        loadData(screenSaverCMD.sn);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventAsyncScreenSaverUPan(ScreenSaverUPanEvent screenSaverUPan) {
        Logger.t("ScreenSaverUPanEvent").i("ScreenSaverUPanEvent::::fragment" + screenSaverUPan.toString());
        isUPanDelete = screenSaverUPan.isDelete();
        stopCircle();
        loadData(null);
    }

    @Subscribe
    public void onEventMainThread(ServiceCheckEvent event) {
        if (event.isNewVersion()) {
            isCheckVersion(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRestartCircle(ScreenSaverCircleEvent event) {
        if (event != null) {
            pagerHandler.postDelayed(pagerRun, event.getCircleDelayTime());
        }
    }

    /**
     * 停止视频播放，并释放VideoView资源
     */
    public void releaseVideoView() {
        View view = adapter.getPrimaryItem();
        if (view != null) {
            VideoView mVideoView = (VideoView) view.findViewById(R.id.vv_video);
            if (mVideoView != null && mVideoView.isPlaying()) {
                mVideoView.stopPlayback();
            }
        }
    }
}
