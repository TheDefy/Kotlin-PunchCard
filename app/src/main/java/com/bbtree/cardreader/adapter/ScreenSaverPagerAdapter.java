package com.bbtree.cardreader.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.bbtree.baselib.utils.BitmapUtils;
import com.bbtree.baselib.utils.ListUtils;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.R;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.entity.ScreenSaverBean;
import com.bbtree.cardreader.entity.requestEntity.Ad;
import com.bbtree.cardreader.entity.requestEntity.ScreenSaverResult;
import com.bbtree.cardreader.utils.DeviceConfigUtils;
import com.bbtree.cardreader.utils.ScreenUtils;
import com.bbtree.cardreader.view.widget.VerticalMarqueeTextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;

/**
 * Function:屏保轮播定时器Adapter
 */
public class ScreenSaverPagerAdapter extends PagerAdapter {
    private CompressionTransformer compressionTransformer = null;

    private ArrayList<ScreenSaverBean> screenSavers = new ArrayList<>();
    private Context mContext;
    private View mCurrentView;

    public ScreenSaverPagerAdapter(Context context) {
        mContext = context;
        compressionTransformer = new CompressionTransformer(mContext);
    }

    public void setList(ArrayList<ScreenSaverBean> list) {
        if (null != list && !list.isEmpty()) {
            screenSavers = list;
        }
        notifyDataSetChanged();
    }

    public void clear() {
        if (null != screenSavers && !screenSavers.isEmpty()) {
            screenSavers.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    //当 view == object 相同
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    //加载view
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (screenSavers.size() <= 0) {
            ImageView imgDef = (ImageView) LayoutInflater.from(mContext).inflate(R.layout.screen_saver_item, null);

            if (Constant.PlatformAdapter.SOFTWINER_EVB.equals(Build.MODEL)) {
                Picasso.with(BBTreeApp.getApp()).load(R.mipmap.screensaver_default)
                        .placeholder(R.mipmap.screensaver_default).error(R.mipmap.screensaver_default)
                        .transform(compressionTransformer)
                        .config(Bitmap.Config.ALPHA_8).into(imgDef);
            } else {
                Picasso.with(BBTreeApp.getApp()).load(R.mipmap.screensaver_default)
                        .placeholder(R.mipmap.screensaver_default).error(R.mipmap.screensaver_default)
                        .transform(compressionTransformer)
                        .config(Bitmap.Config.RGB_565).into(imgDef);
            }
            return imgDef;
        }
        View view = initView(position);
        container.addView(view);
        return view;
    }

    //销毁view
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //Logger.d(">>>destroyItem position " + position);
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    // 初始化view
    private View initView(int position) {
        if (ListUtils.isZero(screenSavers)) {
            return null;
        }
        ScreenSaverBean screenSaver = screenSavers.get(position % screenSavers.size());
        int type = screenSaver.getType();
        View view = null;
        switch (type) {
            case Constant.ScreenSaverConstant.TYPE_TEXT: // 文字
                ScreenSaverResult.Notices notice = screenSaver.getNotice();
                view = LayoutInflater.from(this.mContext).inflate(R.layout.render_type_text, null);
                TextView notice_title = (TextView) view.findViewById(R.id.notice_title);
                notice_title.setText(notice.getTitle());
                VerticalMarqueeTextView notice_content = (VerticalMarqueeTextView) view.findViewById(R.id.notice_content);
                notice_content.setText(notice.getContent());
                TextView notice_sign = (TextView) view.findViewById(R.id.notice_sign);
                notice_sign.setText(notice.getSignature());
                TextView notice_date = (TextView) view.findViewById(R.id.notice_date);
                notice_date.setText(notice.getNoticeTime());

                int messageFont = DeviceConfigUtils.getConfig().getMessageFont();
                float scale = 1.5f;
                switch (messageFont) {
                    case 0:
                        scale = 1;
                        break;
                    case 1:
                        scale = 0.9f;
                        break;
                    case 2:
                        scale = 1.2f;
                        break;
                    case 3:
                        scale = 1.3f;
                        break;

                    default:
                        scale = 1;
                        break;
                }
                float textSize = notice_title.getTextSize();
                notice_title.setTextSize(textSize * scale);
                notice_content.setTextSize(notice_content.getTextSize() * scale);
                notice_date.setTextSize(notice_date.getTextSize() * scale);


                break;
            case Constant.ScreenSaverConstant.TYPE_PIC: // 图片
                ScreenSaverResult.Pictures picture = screenSaver.getPicture();
                ImageView img = (ImageView) LayoutInflater.from(mContext).inflate(R.layout.screen_saver_item, null);
                if (null != picture && !TextUtils.isEmpty(picture.getUrl())) {

                    if (Constant.PlatformAdapter.SOFTWINER_EVB.equals(Build.MODEL)) {
                        Picasso.with(BBTreeApp.getApp()).load(picture.getUrl())
                                .placeholder(R.mipmap.screensaver_default).error(R.mipmap.screensaver_default)
                                .transform(compressionTransformer)
                                .config(Bitmap.Config.ALPHA_8).into(img);
                    } else {
                        Picasso.with(BBTreeApp.getApp()).load(picture.getUrl())
                                .placeholder(R.mipmap.screensaver_default).error(R.mipmap.screensaver_default)
                                .transform(compressionTransformer)
                                .config(Bitmap.Config.RGB_565).into(img);
                    }
                } else {
                    if (Constant.PlatformAdapter.SOFTWINER_EVB.equals(Build.MODEL)) {
                        Picasso.with(BBTreeApp.getApp()).load(R.mipmap.screensaver_default)
                                .placeholder(R.mipmap.screensaver_default)
                                .error(R.mipmap.screensaver_default)
                                .transform(compressionTransformer)
                                .config(Bitmap.Config.ALPHA_8).into(img);
                    } else {
                        Picasso.with(BBTreeApp.getApp()).load(R.mipmap.screensaver_default)
                                .placeholder(R.mipmap.screensaver_default)
                                .error(R.mipmap.screensaver_default)
                                .transform(compressionTransformer)
                                .config(Bitmap.Config.RGB_565).into(img);
                    }
                }
                view = img;
                break;
            case Constant.ScreenSaverConstant.TYPE_ADS: // 广告
                Ad ad = screenSaver.getAd();
                ImageView imgAd = (ImageView) LayoutInflater.from(this.mContext).inflate(R.layout.screen_saver_item, null);
                String url;
                if (ScreenUtils.getOrientation(BBTreeApp.getApp()) == Configuration.ORIENTATION_LANDSCAPE) {
                    url = ad.getPic1();
                } else {
                    url = ad.getPic2();
                }
                RequestCreator loadAd;
                if (TextUtils.isEmpty(url))
                    loadAd = Picasso.with(this.mContext).load(R.mipmap.screensaver_default);
                else
                    loadAd = Picasso.with(this.mContext).load(url);

                if (Constant.PlatformAdapter.SOFTWINER_EVB.equals(Build.MODEL)) {
                    loadAd.placeholder(R.mipmap.screensaver_default)
                            .error(R.mipmap.screensaver_default)
                            .transform(compressionTransformer)
                            .config(Bitmap.Config.ALPHA_8).into(imgAd);
                } else {
                    loadAd.placeholder(R.mipmap.screensaver_default)
                            .error(R.mipmap.screensaver_default)
                            .transform(compressionTransformer)
                            .config(Bitmap.Config.RGB_565).into(imgAd);
                }

                view = imgAd;
                break;
            case Constant.ScreenSaverConstant.TYPE_VIDEO: // 视频
                ScreenSaverResult.Videos video = screenSaver.getVideo();
                view = LayoutInflater.from(this.mContext).inflate(R.layout.screen_saver_item_video, null);
                VideoView mVideo = (VideoView) view.findViewById(R.id.vv_video);
                if (video != null && !TextUtils.isEmpty(video.getUrl()))
                    mVideo.setVideoPath(video.getUrl());
                mVideo.setZOrderMediaOverlay(true);
                break;
        }
        return view;
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

    /**
     * 获取到当前子view
     */
    public View getPrimaryItem() {
        return mCurrentView;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        mCurrentView = (View) object;

    }


    public void release() {
        compressionTransformer = null;
        mContext = null;
    }

}

