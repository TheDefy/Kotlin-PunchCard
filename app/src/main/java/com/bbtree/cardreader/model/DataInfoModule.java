package com.bbtree.cardreader.model;

import com.bbtree.baselib.net.BaseParam;
import com.bbtree.baselib.net.ResultObject;
import com.bbtree.baselib.rxjava.RxUtils;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.common.Urls;
import com.bbtree.cardreader.entity.requestEntity.Reporter;
import com.bbtree.cardreader.entity.requestEntity.SchoolInfo;
import com.bbtree.cardreader.entity.requestEntity.ScreenSaverResult;
import com.bbtree.cardreader.utils.ShellUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;


/**
 * Created by zhouyl on 10/04/2017.
 */

public class DataInfoModule {

    static DataInfoModule instance;

    public static DataInfoModule getInstance() {
        if (instance == null) {
            instance = new DataInfoModule();
        }
        return instance;
    }

    private DataInfoModule() {

    }

    /**
     * 处理APPKEY
     */
    public Observable<ResultObject> getAppKey() {
        return RxUtils.postEntity(Urls.APP_KEY, Reporter.getInstance().build(BBTreeApp.getApp()))
                .map(RxUtils.getMap())
                .retry(2)
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 获取学校信息
     *
     * @return
     */
    public Observable<SchoolInfo> getSchoolInfo() {
        Map map = new HashMap();
        map.put("deviceId", BaseParam.getDeviceId());
        map.put("protocolLevel", 1);
        return RxUtils.postMap(Urls.SCHOOLINFO, map)
                .map(RxUtils.getObject(SchoolInfo.class))
                .retry(2)
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 获取机器配置信息
     *
     * @param sn
     * @return
     */
    public Observable<ResultObject> getDeviceConfig(String... sn) {
        Map map = new HashMap();
        if (sn.length > 0) map.put("sn", sn[0]);
        map.put("deviceId", BaseParam.getDeviceId());
        return RxUtils.postMap(Urls.DEVICECONFIG, map)
                .map(RxUtils.getObject("config"))
                .retry(2)
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 获取屏保数据(更新广告视频)
     *
     * @param map
     * @return
     */
    public Observable<ScreenSaverResult> getScreenSaver(Map map) {
        return RxUtils.postMap(Urls.SCREENSAVER, map)
                .map(RxUtils.getObject(ScreenSaverResult.class))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void setDns() {
        //给本机指定DNS
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand(
                Arrays.asList("setprop net.eth0.dns1 114.114.114.114",
                        "setprop net.eth1.dns1 114.114.114.114"), true);
        if (commandResult.result != 0) {
        }
    }

    /**
     * int数组转String
     */
    public static String toStringMethod(int[] arr) {
        // 自定义一个字符缓冲区，
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        // 遍历int数组，并将int数组中的元素转换成字符串储存到字符缓冲区中去
        for (int i = 0; i < arr.length; i++) {
            if (i != arr.length - 1)
                sb.append(arr[i] + " ,");
            else
                sb.append(arr[i] + " ]");
        }
        return sb.toString();
    }

}
