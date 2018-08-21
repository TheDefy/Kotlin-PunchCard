package com.bbtree.cardreader.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.bbtree.cardreader.utils.NetWorkUtil;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/06/16
 * Time: 下午3:31
 * <p/>
 * 需要开启权限
 * <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
 * <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    public final static String NS_ANDROID_NET_CHANGE_ACTION = "ns.android.net.conn.CONNECTIVITY_CHANGE";
    private String TAG = NetworkStateReceiver.class.getSimpleName();
    private final static String ANDROID_NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static Boolean networkAvailable = false;
    private static NetWorkUtil.NetType netType;
    private static ArrayList<NetChangeObserver> netChangeObserverArrayList = new ArrayList<>();
    private static BroadcastReceiver receiver;

    private static BroadcastReceiver getReceiver() {
        if (receiver == null) {
            receiver = new NetworkStateReceiver();
        }
        return receiver;
    }

    /**
     * 注册网络状态广播
     *
     * @param mContext
     */
    public static void registerNetworkStateReceiver(Context mContext) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(NS_ANDROID_NET_CHANGE_ACTION);
        filter.addAction(ANDROID_NET_CHANGE_ACTION);
        mContext.getApplicationContext()
                .registerReceiver(getReceiver(), filter);
    }

    /**
     * 检查网络状态
     *
     * @param mContext
     */
    public static void checkNetworkState(Context mContext) {
        Intent intent = new Intent();
        intent.setAction(NS_ANDROID_NET_CHANGE_ACTION);
        mContext.sendBroadcast(intent);
    }

    /**
     * 注销网络状态广播
     *
     * @param mContext
     */
    public static void unRegisterNetworkStateReceiver(Context mContext) {
        if (receiver != null) {
            try {
                mContext.getApplicationContext().unregisterReceiver(receiver);
            } catch (Exception e) {
                Logger.i("" + e.getMessage());
            }
        }

    }

    /**
     * 获取当前网络状态，true为网络连接成功，否则网络连接失败
     *
     * @return
     */
    public static Boolean isNetworkAvailable() {
        return networkAvailable;
    }

    public static NetWorkUtil.NetType getAPNType() {
        return netType;
    }

    /**
     * 注册网络连接观察者
     *
     * @param observer observerKey
     */
    public static void registerObserver(NetChangeObserver observer) {
        if (netChangeObserverArrayList == null) {
            netChangeObserverArrayList = new ArrayList<>();
        }
        netChangeObserverArrayList.add(observer);
    }

    /**
     * 注销网络连接观察者
     *
     * @param observer observerKey
     */
    public static void removeRegisterObserver(NetChangeObserver observer) {
        if (netChangeObserverArrayList != null) {
            netChangeObserverArrayList.remove(observer);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        receiver = NetworkStateReceiver.this;
        if (intent.getAction().equalsIgnoreCase(ANDROID_NET_CHANGE_ACTION)
                || intent.getAction().equalsIgnoreCase(
                NS_ANDROID_NET_CHANGE_ACTION)) {
            Logger.i("网络状态改变");
            if (!NetWorkUtil.isNetworkAvailable(context)) {
                Logger.i("没有网络连接");
                networkAvailable = false;
            } else {
                Logger.i("网络连接成功");
                netType = NetWorkUtil.getAPNType(context);
                networkAvailable = true;
            }
            notifyObserver();
        }
    }

    private void notifyObserver() {

        for (int i = 0; i < netChangeObserverArrayList.size(); i++) {
            NetChangeObserver observer = netChangeObserverArrayList.get(i);
            if (observer != null) {
                if (isNetworkAvailable()) {
                    observer.onConnect(netType);
                } else {
                    observer.onDisConnect();
                }
            }
        }

    }

    public interface NetChangeObserver {
        /**
         * 网络连接连接时调用
         */
        void onConnect(NetWorkUtil.NetType type);

        /**
         * 当前没有网络连接
         */
        void onDisConnect();
    }
}
