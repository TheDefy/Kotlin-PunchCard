package com.bbtree.cardreader.view.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bbtree.baselib.base.BaseFragment;
import com.bbtree.baselib.net.BaseParam;
import com.bbtree.baselib.utils.BitmapUtils;
import com.bbtree.baselib.utils.ListUtils;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.BuildConfig;
import com.bbtree.cardreader.R;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.entity.ScreenSaverBean;
import com.bbtree.cardreader.entity.dao.PlayNum;
import com.bbtree.cardreader.entity.eventbus.ScreenSaverCMD;
import com.bbtree.cardreader.entity.eventbus.ServiceCheckEvent;
import com.bbtree.cardreader.entity.eventbus.ScreenSaverCircleEvent;
import com.bbtree.cardreader.entity.requestEntity.ScreenSaverResult;
import com.bbtree.cardreader.model.AdModule;
import com.bbtree.cardreader.model.DataInfoModule;
import com.bbtree.cardreader.receiver.NetworkStateReceiver;
import com.bbtree.cardreader.service.UpdateCheckService;
import com.bbtree.cardreader.utils.DeviceConfigUtils;
import com.bbtree.cardreader.utils.NetWorkUtil;
import com.bbtree.cardreader.view.activity.MainActivity;
import com.bbtree.cardreader.view.banner.CustomBanner;
import com.bbtree.cardreader.view.widget.VerticalMarqueeTextView;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;


/**
 * Function:
 * Created by BBTree Team
 * Author: LiuJihui
 * Create Date: 2015/12/14
 * Create Time: 14:55
 */
public class ScreenSaverPagerFragmentNew extends BaseFragment {

    @BindView(R.id.banner)
    CustomBanner mBanner;

    private static final long CHECK_UPDATE_TIME = 1000 * 60 * 10;
    public long SCREEN_TIME = Constant.ScreenSaver.SCREENSAVER_INTERVAL;  // 轮播时间

    private Handler mHandleCheckUpdate;
    private MainActivity mainActivity;

    private CompressionTransformer compressionTransformer;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public int contentView() {
        return R.layout.pager_screen_saver_frg_new;
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

        compressionTransformer = new CompressionTransformer(mContext);
        initBanner();
        showDefaultScreenView();
        initData(null);
        NetworkStateReceiver.registerObserver(netChangeObserver);
    }

    /**
     * 初始化Banner
     */
    private void initBanner() {
        SCREEN_TIME = DeviceConfigUtils.getConfig().getScreenSaverInterval();

        //设置指示器类型，有普通指示器(ORDINARY)、数字指示器(NUMBER)和没有指示器(NONE)三种类型。
        mBanner.setIndicatorStyle(CustomBanner.IndicatorStyle.ORDINARY);

        //设置两个点图片作为翻页指示器，只有指示器为普通指示器(ORDINARY)时有用。
        //第一个参数是指示器的选中的样式，第二个参数是指示器的未选中的样式。
        mBanner.setIndicatorRes(R.mipmap.ic_focus_select, R.mipmap.ic_focus);

        //设置指示器的方向。
        //这个方法跟在布局中设置app:indicatorGravity是一样的。
        mBanner.setIndicatorGravity(CustomBanner.IndicatorGravity.CENTER);

        //设置轮播图自动滚动轮播，参数是轮播图滚动的间隔时间
        //轮播图默认是不自动滚动的，如果不调用这个方法，轮播图将不会自动滚动。
        mBanner.startTurning(SCREEN_TIME);
    }

    /**
     * 加载数据
     *
     * @param sn
     */
    private void initData(final String sn) {

        compositeDisposable.add(Observable.interval(0, Constant.ScreenSaver.REFRESH_SCREENSAVER, TimeUnit.MILLISECONDS)
                .flatMap(new Function<Long, ObservableSource<ScreenSaverResult>>() {
                    @Override
                    public ObservableSource<ScreenSaverResult> apply(@NonNull Long aLong) throws Exception {
                        Map map = new HashMap();
                        map.put("deviceId", BaseParam.getDeviceId());
                        map.put("sn", sn);
                        Logger.e("mapperisNull?", map);
                        return DataInfoModule.getInstance().getScreenSaver(map);
                    }
                })
                .subscribe(new Consumer<ScreenSaverResult>() {
                    @Override
                    public void accept(@NonNull ScreenSaverResult screenSaverResult) throws Exception {
                        filterDataAndShow(screenSaverResult);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {

                    }
                }));
    }

    /**
     * 处理数据、显示ui
     *
     * @param screenSaverResult
     */
    private void filterDataAndShow(ScreenSaverResult screenSaverResult) {
        if (null == screenSaverResult)
            return;

        List<ScreenSaverBean> screenSavers = new ArrayList<>();
        List<ScreenSaverResult.Notices> notices;
        List<ScreenSaverResult.Pictures> pictures;
        List<ScreenSaverResult.Videos> videos;
        notices = screenSaverResult.getNotices();
        pictures = screenSaverResult.getPictures();
        videos = screenSaverResult.getVideos();
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
        if (videos != null && !videos.isEmpty()) {
            for (int i = 0; i < videos.size(); i++) {
                ScreenSaverBean bean = new ScreenSaverBean();
                bean.setType(Constant.ScreenSaverConstant.TYPE_VIDEO);
                bean.setVideo(videos.get(i));
                screenSavers.add(bean);
            }
        }

        //显示ui
        if (!ListUtils.isZero(screenSavers)) {
            showScreenUI(screenSavers);
        } else { //显示默认的轮播图
            showDefaultScreenView();
        }

    }

    /**
     * 显示默认的轮播图
     */
    private void showDefaultScreenView() {
        List<ScreenSaverBean> screenSaveDefault = new ArrayList<>();
        screenSaveDefault.clear();
        ScreenSaverBean bean = new ScreenSaverBean();
        bean.setType(Constant.ScreenSaverConstant.TYPE_PIC);
        bean.setRes(R.mipmap.screensaver_default);
        screenSaveDefault.add(bean);
        showScreenUI(screenSaveDefault);
    }

    /**
     * 显示轮播图
     *
     * @param screenSaverList
     */
    private void showScreenUI(final List<ScreenSaverBean> screenSaverList) {
        mBanner.setPages(new CustomBanner.ViewCreator<ScreenSaverBean>() {
            @Override
            public View createView(Context context, int position) {

                ScreenSaverBean screenSaver = screenSaverList.get(mBanner.getActualPosition(position));
                int type = screenSaver.getType();
                View mView = null;
                switch (type) {
                    case Constant.ScreenSaverConstant.TYPE_TEXT: // 文字
                        ScreenSaverResult.Notices notice = screenSaver.getNotice();
                        mView = LayoutInflater.from(mContext).inflate(R.layout.render_type_text, null);
                        TextView notice_title = (TextView) mView.findViewById(R.id.notice_title);
                        notice_title.setText(notice.getTitle());
                        VerticalMarqueeTextView notice_content = (VerticalMarqueeTextView) mView.findViewById(R.id.notice_content);
                        notice_content.setText(notice.getContent());
                        TextView notice_sign = (TextView) mView.findViewById(R.id.notice_sign);
                        notice_sign.setText(notice.getSignature());
                        TextView notice_date = (TextView) mView.findViewById(R.id.notice_date);
                        notice_date.setText(notice.getNoticeTime());
                        break;
                    case Constant.ScreenSaverConstant.TYPE_PIC: // 图片
                        ScreenSaverResult.Pictures picture = screenSaver.getPicture();
                        ImageView img = (ImageView) LayoutInflater.from(mContext).inflate(R.layout.screen_saver_item, null);
                        if (picture != null && !TextUtils.isEmpty(picture.getUrl())) {
                            Picasso.with(BBTreeApp.getApp()).load(picture.getUrl())
                                    .placeholder(R.mipmap.screensaver_default).error(R.mipmap.screensaver_default)
                                    .transform(compressionTransformer)
                                    .config(Bitmap.Config.RGB_565).into(img);
                        } else {
                            Picasso.with(BBTreeApp.getApp()).load(R.mipmap.screensaver_default)
                                    .placeholder(R.mipmap.screensaver_default)
                                    .error(R.mipmap.screensaver_default)
                                    .transform(compressionTransformer)
                                    .config(Bitmap.Config.RGB_565).into(img);
                        }
                        mView = img;
                        break;
                    default:
                        break;
                }
                return mView;
            }

            @Override
            public void updateUI(Context context, View view, int position, ScreenSaverBean data) {

                ScreenSaverBean data1 = screenSaverList.get(mBanner.getActualPosition(position));
                Logger.i("data setSelectedPosition position " + position);
                Logger.i("data setSelectedPosition" + "::::::notice:" + (null != data1.getNotice() ? data1.getNotice().getContent() : "")
                        + "::::picture" + (null != data1.getPicture() ? data1.getPicture().getTitle() : ""));
                Logger.i("data setSelectedPosition string " + data1.toString());
                initScrollHandle(view);
                updateTabUI(data1);

                // TODO test ad
                if (screenSaverList.size() > 1) {
                    PlayNum num = new PlayNum();
                    num.setAdId(data1.hashCode());
                    AdModule.getInstance().insert(num);
                }
            }
        }, screenSaverList);
    }

    private void initScrollHandle(View view) {
        if (view != null) {
            VerticalMarqueeTextView notice_content = (VerticalMarqueeTextView) view.findViewById(R.id.notice_content);
            if (notice_content != null) {
                notice_content.startMarquee();
                mBanner.stopTurning();
            }
        }
    }

    /**
     * 更新主页面的tab
     *
     * @param data
     */
    private void updateTabUI(ScreenSaverBean data) {
        Logger.i("data setSelectedPosition：" + data.getType() + ":");
        int type = data.getType();
        if (null != mainActivity) {
            mainActivity.setSelectedPosition(type);
        }
    }

    NetworkStateReceiver.NetChangeObserver netChangeObserver = new NetworkStateReceiver.NetChangeObserver() { // 网络连接的监听事件
        @Override
        public void onConnect(NetWorkUtil.NetType type) {
            Logger.i("网络已连接");
            checkVersionUpdate();
        }

        @Override
        public void onDisConnect() {
            Logger.i("网络已断开");
            isCheckVersion(false);
        }
    };

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (this.isAdded()) {
                isCheckVersion(false);
            }
        } else {
            if (this.isAdded() && this.isVisible()) {
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

    @Subscribe
    public void onEventMainThread(ServiceCheckEvent event) {
        if (event.isNewVersion()) {
            isCheckVersion(false);
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        isCheckVersion(false);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        NetworkStateReceiver.removeRegisterObserver(netChangeObserver);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventAsyncScreenSaverCMD(ScreenSaverCMD screenSaverCMD) {
        if (screenSaverCMD.cmd == ScreenSaverCMD.ScreenSaverAction.screenSaveUpdate) {
            initData(screenSaverCMD.sn);
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventMainNetworkQuality(ScreenSaverCircleEvent event) {
        if (event!=null) {
            mBanner.startTurning(SCREEN_TIME);
        }
    }

    /**
     * Function:  图片以宽度1080 压缩
     */
    public static class CompressionTransformer implements Transformation {
        private Context mContext;
        private int sWidth;
        private int sHeight;

        public CompressionTransformer(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            sWidth = mContext.getResources().getInteger(R.integer.screenSaverTargetWidth);
            sHeight = mContext.getResources().getInteger(R.integer.screenSaverTargetHeight);
            return BitmapUtils.decodeBitmap(source, sWidth, sHeight);

        }

        @Override
        public String key() {
            return "square()";
        }

    }

}
