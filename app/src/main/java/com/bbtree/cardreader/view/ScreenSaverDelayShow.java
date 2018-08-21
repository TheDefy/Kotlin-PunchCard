package com.bbtree.cardreader.view;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;

import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.contact.MainActivityContract;
import com.bbtree.cardreader.entity.eventbus.PauseMusicEvent;
import com.bbtree.cardreader.service.FreeUpDiskService;
import com.bbtree.cardreader.view.activity.MainActivity;
import com.bbtree.cardreader.view.fragment.ScreenSaverPagerFragment;

import org.greenrobot.eventbus.EventBus;

/**
 * Function: 屏幕保护(轮播广告)延迟显示机制类
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/08/18
 * Create Time: 上午10:36
 */
public class ScreenSaverDelayShow {

    private static final String screenSaverTag = "screenSaverTag";
    private static final int MSG_SHOW = 100;// 显示屏保
    private static final int FREE_UP_DISK = 200;// 显示屏保
    private Fragment imageScreenSaverFrg;
    private Activity mContext;
    private FragmentManager fragmentManager;
    private int layoutResID;

    //add by baodian
    private MainActivityContract.Presenter mPresenter;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg == null) {
                return false;
            }
            switch (msg.what) {
                case MSG_SHOW:
                    try {
                        if (mContext instanceof MainActivity) {
                            ((MainActivity) mContext).setShowCamera(View.INVISIBLE);
                        }
                        PauseMusicEvent pauseMusicEvent = new PauseMusicEvent();
                        pauseMusicEvent.type = PauseMusicEvent.PauseMusicType.pause;
                        pauseMusicEvent.setPause(false);
                        EventBus.getDefault().post(pauseMusicEvent);

                        showImageScreenSaverFrg();
                    } catch (Exception e) {

                    }
                    break;
                case FREE_UP_DISK:
                    FreeUpDiskService.startWork(mContext);
                    break;
                default:
                    break;
            }
            return false;
        }
    });


    /**
     * @param activity 依附的activity
     * @param resID    依附的布局资源id
     */
    public ScreenSaverDelayShow(Activity activity, int resID, long delayTime, MainActivityContract.Presenter presenter) {
        mContext = activity;
        layoutResID = resID;
        imageScreenSaverFrg = new ScreenSaverPagerFragment();
        fragmentManager = mContext.getFragmentManager();
        resetDelayTime(delayTime);

        mPresenter = presenter;
    }

    /**
     * 重置
     */
    public void resetDelayTime(long delayTime) {
        hideScreenSaverFrg();
        mHandler.removeMessages(MSG_SHOW);
        mHandler.sendEmptyMessageDelayed(MSG_SHOW, delayTime); //开启一个新的定时
        mHandler.removeMessages(FREE_UP_DISK);
        mHandler.sendEmptyMessageDelayed(FREE_UP_DISK, Constant.FreeUpDisk.FREE_UP_DISK_DELAY);//空闲时段执行磁盘释放

        //打卡时暂停音乐播放
        PauseMusicEvent pauseMusicEvent = new PauseMusicEvent();
        pauseMusicEvent.type = PauseMusicEvent.PauseMusicType.pause;
        pauseMusicEvent.setPause(true);
        pauseMusicEvent.setDelayTime(Integer.MAX_VALUE);
        EventBus.getDefault().post(pauseMusicEvent);
        //CardRecordModule.getInstance().clearTask();
        //add by baodian
        if (mPresenter != null) {
            try {
                mPresenter.getDataTransfer().overTask();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止
     */
    public void stop() {
        hideScreenSaverFrg();
        mHandler.removeMessages(MSG_SHOW);
    }

    /**
     * 隐藏轮播
     */
    private void hideScreenSaverFrg() {
        if (imageScreenSaverFrg != null && imageScreenSaverFrg.isAdded()) {
            if (imageScreenSaverFrg instanceof ScreenSaverPagerFragment) {
                ((ScreenSaverPagerFragment) imageScreenSaverFrg).releaseVideoView();
            }
            FragmentTransaction transaction = fragmentManager.beginTransaction();
//            transaction.hide(imageScreenSaverFrg);
            // TODO: 2017/8/9 重置轮播图顺序的方法 transaction.remove(imageScreenSaverFrg); imageScreenSaverFrg = null;
            transaction.remove(imageScreenSaverFrg);
            imageScreenSaverFrg = null;
            transaction.commitAllowingStateLoss();
        }
    }

    /**
     * 显示轮播frg
     */
    private void showImageScreenSaverFrg() throws RemoteException {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = fragmentManager.findFragmentByTag(screenSaverTag);
        if (fragment == null) {
            if (imageScreenSaverFrg == null) {
                imageScreenSaverFrg = new ScreenSaverPagerFragment();
            }
            transaction.add(layoutResID, imageScreenSaverFrg, screenSaverTag);
        } else {
            transaction.show(fragment);
        }
        if (mContext.isDestroyed()) {
            return;
        }
        transaction.commitAllowingStateLoss();
        //CardRecordModule.getInstance().doFailTask(mPresenter);
        if (mPresenter != null) {
            try {
                mPresenter.getDataTransfer().startTask();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void release() {
        mContext = null;
        imageScreenSaverFrg = null;
    }
}
