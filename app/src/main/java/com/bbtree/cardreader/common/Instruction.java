package com.bbtree.cardreader.common;

/**
 * Created by zhouyl on 19/04/2017.
 */

public class Instruction {
    public static final int ClearCards = 1;//清空本地卡信息
    public static final int CardsPush = 2;//推送本地现在的所有卡信息给服务器
    public static final int DeleteCards = 3;//删除卡片
    public static final int AddCards = 4;//增加卡片
    public static final int PullFailedRecord = 5;//拉取失败队列
    public static final int InstallAPK = 9;//安装应用
    public static final int ScreenSaverUpdate = 10;//屏保/轮播更新
    public static final int PullDeviceConfig = 12;//拉取設備配置
    public static final int RebootOrShutdown = 13;//关机或者重启
    public static final int CardsPull = 15;//从服务器拉取所有卡
    public static final int QueryUploadFail = 16;//查询未上传成功的条数
    public static final int SwitchTemp = 20;//切换耳温枪功能
    public static final int GetSpeakerConfig = 21;//获取音响列表
    public static final int AdUpdate = 22;//广告更新
}
