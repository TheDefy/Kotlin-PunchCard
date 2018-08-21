package com.bbtree.cardreader.service;

import android.content.Context;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

/**
 * 高德定位
 * Created by HYWW on 2014/6/13.
 */

public class AMapLocationProvider implements AMapLocationListener {
    private AMapLocation myRealLocation;
    private long locateTime;
    //    private LocationManagerProxy mAMapLocationManager;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    private static AMapLocationProvider ourInstance = new AMapLocationProvider();

    public static AMapLocationProvider getInstance() {
        return ourInstance;
    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            aMapLocation.setAccuracy(0);
//            if (aMapLocation.getLatitude() < 1 || aMapLocation.getLongitude() < 1) {
//                return;
//            }
            myRealLocation = aMapLocation;
            locateTime = System.currentTimeMillis();
        }
    }

    public void init(Context context) {
        if (locationClient == null) {
            locationClient = new AMapLocationClient(context);
            locationOption = new AMapLocationClientOption();
        }
//            mAMapLocationManager = LocationManagerProxy.getInstance(context);
    }

    public boolean start() {
        if (locationClient != null) {
            /*
             * mAMapLocManager.setGpsEnable(false);
			 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
			 * API定位采用GPS和网络混合定位方式
			 * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
			 */
            // 设置定位模式为仅设备模式
            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            locationOption.setOnceLocation(true);
            locationClient.setLocationOption(locationOption);
            // 设置定位监听
            locationClient.setLocationListener(this);
            locationClient.startLocation();
            return true;
        } else {
            return false;
        }
    }


    public void destroy() {
        /**
         * 如果AMapLocationClient是在当前Activity实例化的，
         * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
         */
        if (locationClient != null)
            locationClient.onDestroy();
        locationClient = null;
        locationOption = null;
    }

    public AMapLocation getLocation() {
        long now = System.currentTimeMillis();
        if (now - locateTime > 2 * 60 * 1000) {
            myRealLocation = null;
        }
        return myRealLocation;
    }

    public long getLocationTime() {
        return locateTime;
    }

    public AMapLocationClient getLocationManager() {
        return locationClient;
    }
}

