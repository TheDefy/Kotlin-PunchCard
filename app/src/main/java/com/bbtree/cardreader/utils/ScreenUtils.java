package com.bbtree.cardreader.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.orhanobut.logger.Logger;

/**
 * Function:获取屏幕信息
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/07/27
 * Create Time: 下午3:42
 */
public class ScreenUtils {
    private static final String TAG = ScreenUtils.class.getSimpleName();

    // 屏幕密度
    public static float density;

    public static void init(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        density = dm.density;
    }

    /**
     * dp转px @param dpValue dp @return int px @throws
     */
    public static int dp2px(float dpValue) {
        return (int) (dpValue * density + 0.5f);
    }
    /**
     * 根据手机的分辨率从 sp 的单位 转成为 px
     *
     * @param context 上下文
     * @param spValue SP值
     * @return 像素值
     */
    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
    /**
     * px 转 dp @param pxValue px @return int dp @throws
     */
    public static int px2dp(float pxValue) {
        return (int) (pxValue / density + 0.5f);
    }

    /**
     * 获取屏幕朝向
     *
     * @param context
     * @return
     */
    public static int getOrientation(Context context) {
        Configuration cf = context.getResources().getConfiguration(); //获取设置的配置信息
        return cf.orientation; //获取屏幕方向
    }

    public static String getScreenSize(Context context) {
        String resolution = "";
        try {
            final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            final Display display = wm.getDefaultDisplay();
            final DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            resolution = metrics.widthPixels + "x" + metrics.heightPixels;
        } catch (Throwable t) {
            Logger.d("Device resolution cannot be determined");
        }
        return resolution;
    }

    public static int[] getScreenSizeNum(Context context) {
        int[] resolution = new int[2];
        try {
            final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            final Display display = wm.getDefaultDisplay();
            final DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            resolution[0] = metrics.widthPixels;
            resolution[1] = metrics.heightPixels;
        } catch (Throwable t) {
            Logger.d("Device resolution cannot be determined");
        }
        return resolution;
    }
}
