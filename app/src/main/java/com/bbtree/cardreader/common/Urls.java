package com.bbtree.cardreader.common;

import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.R;

/**
 * Created by zhouyl on 10/04/2017.
 */

public class Urls {

    private static final String AGREEMENT = "http://";
    public static final String HOST = BBTreeApp.getApp().getResources().getString(R.string.baseUrl);
    public static final String PORT = BBTreeApp.getApp().getResources().getString(R.string.port);
    private static final String SERVER = AGREEMENT + HOST + ":" + PORT + "/api/";
    private static final String SERVER2 = AGREEMENT + HOST + ":" + PORT + "/server/";

    /**
     * 获取APPKEY
     */
    public static final String APP_KEY = SERVER + "machine/register";

    /**
     * 获取学校信息
     */
    public static final String SCHOOLINFO = SERVER + "machine/get_school_info";

    /**
     * 获取学校所有卡
     */
    public static final String CARDSPULL = SERVER + "card/get_cards";

    /**
     * 上传学校所有卡给服务器对比
     */
    public static final String CARDSPUSH = SERVER + "card/upload_cards";
    /**
     * 上传图片
     */
    public static final String IMGUPLOAD = SERVER + "punch/upload_img";
    /**
     * 卡数据上报
     */
    public static final String RECORDREPORT = SERVER + "punch";
    /**
     * 心跳
     */
    public static final String HEARTBEAT = SERVER + "keepalive";
    /**
     * 请求需要删除的卡信息
     */
    public static final String CARDSDELETEPULL = SERVER + "card/del";
    /**
     * 请求需要增加的卡信息
     */
    public static final String CARDSADDPULL = SERVER + "card/add";

    /**
     * 获取机器配置信息
     */
    public static final String DEVICECONFIG = SERVER + "machine/get_config";

    /**
     * 检查更新
     */
    public static final String CHECKUPDATE = SERVER2 + "machine/get_version";

    /**
     * 更新广告视频
     */
    public static final String SCREENSAVER = SERVER + "machine/get_ad";

    /**
     * 温度数据上传
     */
    public static final String TEMPREPORT = SERVER + "temp/upload";

    /**
     * 获取测温枪配置信息
     */
    public static final String TEMPCONFIG = SERVER + "temp/get_config";

    /**
     * 校园之星
     */
    public static final String SCHOOLSTAR = SERVER + "school/school_star";

    /**
     * 系统通知
     */
    public static final String SYSTEMNOTICE = SERVER + "machine/system_notice";

    /**
     * 上报失败纪录数量总结
     */
    public static final String FAILRECORDNUMBER = SERVER + "log/fail/report";

    /**
     * 保存音箱信息
     */
    public static final String SPEAKERSAVE = SERVER2 + "machine/speaker/save";

    /**
     * 删除音箱表
     */
    public static final String SPEAKERDELETE = SERVER2 + "machine/speaker/del";

    /**
     * 获取音箱配置
     */
    public static final String SPEAKERCONFIG = SERVER + "machine/get/speaker/config";

    /**
     * 新心跳
     */
    public static final String HEARTBEATNEW = SERVER2 + "machine/heartbeat";

    /**
     * 获取广告接口
     */
    public static final String GETAD = SERVER2 + "ad/getAd";

    /**
     * 上报广告播放次数
     */
    public static final String ADPLAYNUM = SERVER2 + "ad/playNum";

    public static final String MACHINEINFO = SERVER2 + "upload/machineInfo";
}
