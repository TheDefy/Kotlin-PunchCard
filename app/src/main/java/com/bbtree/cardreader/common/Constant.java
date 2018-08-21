package com.bbtree.cardreader.common;


/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/05/25
 * Time: 下午4:46
 */
public class Constant {

    public static class UserType {
        public final static int Teacher = 0;
        public final static int Student = 1;
    }

    /**
     * 更新器
     */
    public static class Updater {
        public static final String UPDATES_FOLDER = "updater";
    }

    /**
     * 下载状态
     */
    public static class DownloadStatus {
        public static final int PREPARE = 100;//准备
        public static final int START = 101;//开始
        public static final int PAUSE = 102;//暂停
        public static final int CANCEL = 103;//取消
        public static final int STOP = 104;//停止/中断
        public static final int COMPLETE = 105;//完成
        public static final int DOWNLOAD_FAIL = 106;//失败
        public static final int ILLEGA_TYPE = 107;//非法文件类型
        public static final int INSTALL_SUCCESS = 108;//非法文件类型
        public static final int INSTALL_FAIL = 109;//非法文件类型
    }

    /**
     * 网络请求
     */
    public static class NetRequest {
        public static final String UserAgent = "BBTreeCardDevice";
    }

    /**
     * XML存储
     */
    public static class XMLStorage {
        public static final String SECRET_KEY = "new_secret_key";
        public static final String DEVICE_ALIAS = "new_device_alias";
        public static final String SCHOOL_NAME = "school_name";
        public static final String SCHOOL_ID = "school_id";
        public static final String RFID_PROTOCOL_V2 = "rfid_protocol_V2";
        public static final String BAUD_RATE = "baud_rate";
        public static final String DEVICE_CONFIG = "device_config";
        public static final String TEMP_CONFIG = "temp_config";
        public static final String READER_PATH = "READER_PATH";
        public static final String FIRST_INSET_UPAN = "first_inset_upan";
        public static final String FACTORYTIP = "FACTORY_TIP";
    }

    /**
     * 推送打卡记录
     */
    public static class PushCardRecord {
        public static final int MAX_SIZE_PER_REQUEST = 200;
        public static final int MAX_SIZE_PER_IMG_REQUEST = 20;
        public static final int MAX_SHERIFF = 5;//最高派出巡逻官个数
        public static final int PATROL_INTERVAL = 30;//巡逻默认巡逻间隔间隔(分钟)
    }

    public static class CleanDiskPic {
        public static final int MAX_SIZE_PER = 200;//每次删除的最大数量
    }

    /**
     * 播音时间区分格式
     */
    public static class VoiceFormat {
        public static final String TimeFormat = "HH:mm";
        public static final String FamilyName = "#FamilyName#";
        public static final String FirstName = "#FirstName#";
        public static final String FullName = "#FullName#";
        public static final String ClassName = "#ClassName#";
        public static final String SchoolName = "#SchoolName#";
    }

    public static class DBInfo {
        public static final String DB_NAME = "BBTree-Card_encrypted-db";
    }

    public static class WatchDogInfo {
        /**
         * 我们自己的watchdog
         */
        public static final String ACTION_START = "com.bbtree.cardreader.intent.action.ARE_U_OK";

        /**
         * 启用迈德watchdog
         */
        public static final String MAIDE_ENABLE = "android.intent.action.enable_watchdog";
        /**
         * 停用迈德watchdog
         */
        public static final String MAIDE_DISABLE = "android.intent.action.disable_watchdog";
    }

    /**
     * 轮播计时
     */
    public static class ScreenSaver {
        public static final long SCREENSAVER_DELAY = 10 * 1000; //轮播时长
        public static final long REFRESH_SCREENSAVER = 2 * 60 * 60 * 1000; //刷新轮播时长(2小时)
        public static final long SCREENSAVER_INTERVAL = 15 * 1000;//每张轮播切换间隔
    }

    /**
     * 双击返回键退出间隔时间
     */
    public static class KeyPress {
        public static final long BACK_KEY_INTERVAL = 2500;
    }

    /**
     * 磁盘释放
     */
    public static class FreeUpDisk {
        public static final long FREE_UP_DISK_DELAY = 5 * 60 * 1000;//空闲三分钟后开始执行磁盘释放
    }

    public static class NetworkDiagnostic {
        public static final String DOMAIN_NETEASE = "www.163.com";
        public static final String DOMAIN_BAIDU = "www.baidu.com";
    }

    /**
     * 考勤机的平台
     */
    public static class PlatformAdapter {
        public static final String CS3 = "CS3";//A388
        public static final String CS3Plus = "CS3Plus";//龙腾8311板子
        public static final String CS3i = "CS3i";
        public static final String CS3Pagan = "CS3Pagan";//
        public static final String CS3PaganOSK = "CS3PaganOSK";//
        public static final String Donkey = "Donkey";//Pagan4Fuwa
        public static final String CS3C = "CS3C";//A20
        public static final String Squid = "Squid";//CS3C系列，福娃造
        public static final String Snobs = "Snobs";//智趣盒子,双读卡器

        public static final String Scallops = "Scallops";//CS3C系列，福娃造->干贝聊
        public static final String Peas = "Peas";//CS3系列，福娃

        public static final String ZBOX_V5 = "ZBOX V5";//智趣的机器,有些是双读卡器
        public static final String ZBOX_V4 = "ZBOX V4";//智趣的机器,有些是双读卡器
        public static final String Cobabys_Z3T = "中宇商显Z3T";//幼乐宝Z3T
        public static final String Cobabys_Z2 = "Z2";//幼乐宝Z2
        public static final String Cobabys_M2 = "M2";//幼乐宝Z2的板子，一块板子两个机型

        public static final String CS3_DS831_PREFIX = "CS3_DS831";// DS831固件model1
        public static final String CS3DS831_PREFIX = "CS3DS831";// DS831固件model2

        public static final String TUXING_GATE = "A2318";// 土星门闸机
        public static final String TUXING_GATE_DS831 = "SJT1_DS831_0_5";// 土星门闸机DS831版本

        public static final String SOFTWINER_EVB = "SoftwinerEvb";// 土星
        public static final String PS_A210 = "PS-A210";// 土星
    }

    /**
     * 考勤机需要特殊适配的平台
     */
    public static class PlatformFirms {
        public static final String BBTree = "BBTree";//智慧树
        public static final String IQEQ = "IQEQ";//智趣
        public static final String Cobabys = "Cobabys";//幼乐宝
        public static final String Beiliao = "Beiliao";//贝聊
        public static final String UNKNOWN = "unknown";//未知
    }

    /**
     * 包名信息
     */
    public static class PackageNameInfo {
        public static final String WatchDog = "com.bbtree.cardreader.watchdog";
    }

    /**
     * 轮班播报相关
     */
    public static class ClassSpeaker {
        /**
         * 协议密钥1
         */
        public static final int SECRETKEY1 = 0x5A;
        /**
         * 协议密钥2
         */
        public static final int SECRETKEY2 = 0x51;
        /**
         * SharedPreference中存储的PIN值的key
         */
        public static final String PIN_KEY = "CLASS_SPEAKER_PIN";
        /**
         * SharedPreference中存储的发射器端口的key
         */
        public static final String SENDER_KEY = "SENDER_PATH";
        /**
         * 音箱参数表
         */
        public static final String SPEAKERCONFIG = "SpeakerConfig";
        /**
         * 音箱列表表
         */
        public static final String SPEAKER = "Speaker";
    }

    /**
     * 二维码相关
     */
    public static class QRCode {
        /**
         * 二维码baseUrl
         */
        public static final String QRCODEBASEURL = "http://bbtree.com/n?";
        /**
         * 别名
         */
        public static final String QRCODEALIAS = "alia=";
    }

    /**
     * Function:网络质量检测
     */
    public static class NetCheckResult {
        public static final int SUCCESS = 6;
        public static final int BBTREE_ABNORMAL = 5;
        public static final int BAIDU_ABNORMAL = 3;
        public static final int FAIL = 2;
    }

    /**
     * Function:轮播图显示的类型
     */
    public class ScreenSaverConstant {
        public static final int TYPE_NONE = 0;

        public static final int TYPE_TEXT = 1;// 通知 文字

        public static final int TYPE_PIC = 2;// 图片

        public static final int TYPE_VIDEO = 3;// 视频

        public static final int TYPE_CARD = 5;// 打卡

        public static final int TYPE_ADS = 6;// 广告
    }

    public static class CardReaderType {
        public static final int ID = 1;
        public static final int IC = 2;
    }

    public static class UPanConstant {
        public static final String VIDEO_LIST_STR_KEY = "video_list_string_key";
    }

}
