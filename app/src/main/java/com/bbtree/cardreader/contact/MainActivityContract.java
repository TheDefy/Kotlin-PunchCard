
package com.bbtree.cardreader.contact;

import android.graphics.Bitmap;

import com.bbtree.baselib.base.IBasePresenter;
import com.bbtree.baselib.base.IBaseView;
import com.bbtree.cardreader.DataTransfer;
import com.bbtree.cardreader.TTSWorker;
import com.bbtree.cardreader.entity.eventbus.ScreenSaverCMD;
import com.bbtree.cardreader.entity.eventbus.SwipeCardInfo;
import com.bbtree.cardreader.entity.eventbus.TTSInfo;
import com.bbtree.cardreader.entity.requestEntity.Ad;

import java.util.List;

/**
 * Description : MainActivity接口
 */
public interface MainActivityContract {
    interface View extends IBaseView<Presenter> {
        /**
         * 显示设备信息
         *
         * @param
         */
        void showMachineInfo(String appVersion, String deviceAlias);

        /**
         * 显示二维码
         *
         * @param qrCode
         */
        void showQRCode(Bitmap qrCode);

        /**
         * 显示学校的名称
         *
         * @param schoolName
         */
        void showSchoolName(String schoolName);

        /**
         * 显示温枪的ui
         *
         * @param open
         */
        void showTempUI(boolean open);

        /**
         * 显示fragment
         *
         * @param b
         */
        void showFragmentBySchoolId(boolean b);

        /**
         * 重置屏保轮训定时器
         */
        void resetScreenLoop();

        /**
         * 显示卡号
         *
         * @param temp
         */
        void showCardNo(String temp);

        /**
         * 显示体温
         *
         * @param temp
         */
        void showNowTemp(String temp);

        /**
         * 设置温度的icon
         *
         * @param imageResource
         */
        void showTempIcon(int imageResource);

        /**
         * 获取照片的字节数组
         *
         * @return
         */
        byte[] takePhotoBytes();

        /**
         * 未上传记录数
         *
         * @param recordCount  未上传记录数
         * @param pictureCount 未上传照片数
         */
        void showNoUploaded(String recordCount, String pictureCount);

        /**
         * 选中icon
         *
         * @param position
         */
        void setSelectedPosition(int position);

        /**
         * 显示广告
         *
         * @param ads
         */
        void showScreenUI(List<Ad> ads);

        /**
         * 显示默认的广告
         */
        void showDefaultImg();

        /**
         * 温枪测试 显示main ui
         */
        void showMainUi();
    }

    interface Presenter extends IBasePresenter<View> {
        //add by baodian
        public DataTransfer getDataTransfer();


        /**
         * 初始化
         */
        void start();

        /**
         * 获取所有音箱和配置
         */
        void getAllSpeakers();

        /**
         * 设置语音播报的Stub
         *
         * @param ttsWorker
         */
        void setSpeakerStub(TTSWorker ttsWorker);

        /**
         * 设置上传数据的Stub
         *
         * @param mDataTransfer
         */
        void setTransferStub(DataTransfer mDataTransfer);

        /**
         * 开启卡号监听
         */
        void startCardListener();

        /**
         * 异步处理打卡信息
         *
         * @param swipeCardInfo
         */
        void onEventAsyncSwipeCardInfo(SwipeCardInfo swipeCardInfo);

        /**
         * 分班播报
         *
         * @param swipeCardInfo
         */
        void classSpeakerBroadcast(SwipeCardInfo swipeCardInfo);

        /**
         * 朗读
         *
         * @param ttsInfo
         */
        void onEventBackgroundTTSInfo(TTSInfo ttsInfo);

        /**
         * 获取温枪配置
         */
        void getTempConfig();

        /**
         * 测量温度
         *
         * @param open
         */
        void measureTemp(boolean open);

        /**
         * 初始化固定广告数据
         */
        void initAdData();

        /**
         * 广告指令
         *
         * @param screenSaverCMD
         */
        void onEventAsyncAd(ScreenSaverCMD screenSaverCMD);

        /**
         * 记录广告次数
         *
         * @param ad
         * @param isPushNum
         */
        void playNum(Ad ad, boolean isPushNum);

        /**
         * 上传固定广告轮播次数
         */
        void pushPlayNumApi();
    }

    interface Model {
    }

}
