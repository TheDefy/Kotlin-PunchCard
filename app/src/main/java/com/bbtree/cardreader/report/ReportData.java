package com.bbtree.cardreader.report;

import android.os.Build;
import java.util.ArrayList;


/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/06/01
 * Time: 下午2:16
 */
public class ReportData  {
    public String DEVICE_ID;
    public String DEVICE_OLD_ID;// 旧设备号

    public String UUID;
    public String SOURCE_PLATFORM;
    public String SOURCE_DEVICE_ID;
    public int APP_VERSION_CODE;//app版本值
    public String APP_VERSION_NAME;//app版本名称
    public String PACKAGE_NAME;//应用包名
    public String FILE_PATH;//

    public long TOTAL_MEM_SIZE;//内存总大小
    public long AVAILABLE_MEM_SIZE;//可用内存大小

    public String CPU_NAME;//cpu 名称
    public String MIN_CPU_FREQ;//最小CPU频率
    public String MAX_CPU_FREQ;//最大CPU频率
    public String CURRENT_CPU_FREQ;//当前CPU频率
    public int CPU_CORE_NUM;//CPU核心数

    public long INTERNAL_STORGE_SIZE;//ROM大小
    public long AVAILABLE_INTERNALSTORGE_SIZE;//可用ROM大小
    public boolean HAS_SDCARD;
    public long TOTAL_SDCARD_SIZE;//SD卡大小
    public long AVAILABLE_SDCARD_SIZE;//可用SD卡大小

    public String LANGUAGE;//语言
    public String COUNTRY;//地区

    public String IP_ADDRESS;//IP地址
    public String MAC_ADDRESS;//MAC地址
    public String HOST_IP_ADDRESS;//网关IP
    public String WIFI_BSSID;//wifi bssid
    public String WIFI_SSID;//wifi ssid
    public int WIFI_SPEED;//wifi 速率
    public int WIFI_RSSI;//wifi 信号质量
    public String IMEI;//IMEI
    public String IMSI;//IMSI

    public String ETH_MAC_ADDRESS;//有线网络的MAC地址；
    public String ETH_IP_ADDRESS;//有线网络的IP地址
    public ArrayList<String> DNS;

    public long CURRENT_TIME_MILLIS = System.currentTimeMillis();
    public long NANO_TIME = System.nanoTime();

    public String BOARD = Build.BOARD;//获取设备基板名称
    public String BOOTLOADER = Build.BOOTLOADER;//获取设备引导程序版本号
    public String BRAND = Build.BRAND;//获取设备品牌
    @SuppressWarnings("deprecation")
    public String CPU_ABI = Build.CPU_ABI;//获取设备指令集名称（CPU的类型）
    @SuppressWarnings("deprecation")
    public String CPU_ABI2 = Build.CPU_ABI2;//获取第二个指令集名称
    public String DEVICE = Build.DEVICE;//获取设备驱动名称
    public String DISPLAY = Build.DISPLAY;//获取设备显示的版本包（在系统设置中显示为版本号）和ID一样
    public String FINGERPRINT = Build.FINGERPRINT;//设备的唯一标识。由设备的多个信息拼接合成。
    public String HARDWARE = Build.HARDWARE;//设备硬件名称,一般和基板名称一样（BOARD）
    public String HOST = Build.HOST;//设备主机地址
    public String ID = Build.ID;//设备版本号
    public String MODEL = Build.MODEL;//获取手机的型号 设备名称
    public String MANUFACTURER = Build.MANUFACTURER;//获取设备制造商
    public String PRODUCT = Build.PRODUCT;//整个产品的名称
    public String RADIO = Build.getRadioVersion();//无线电固件版本号，通常是不可用的 显示unknown
    public String BUILD_TAGS = Build.TAGS;//设备标签。如release-keys 或测试的 test-keys
    public long BUILD_TIME = Build.TIME;//时间
    public String BUILD_TYPE = Build.TYPE;//设备版本类型  主要为"user" 或"eng".
    public String USER = Build.USER;//设备用户名 基本上都为android-build
    public String VERSION_RELEASE = Build.VERSION.RELEASE;//获取系统版本字符串。如4.1.2 或2.2 或2.3等
    public String VERSION_CODENAME = Build.VERSION.CODENAME;//设备当前的系统开发代号，一般使用REL代替
    public String INCREMENTAL = Build.VERSION.INCREMENTAL;//系统源代码控制值，一个数字或者git hash值
    @SuppressWarnings("deprecation")
    public String SDK = Build.VERSION.SDK;//系统的API级别 一般使用下面大的SDK_INT 来查看
    public int SDK_INT = Build.VERSION.SDK_INT;//系统的API级别 数字表示
    //public String VERSION_CODES=Build.VERSION_CODES;//类中有所有的已公布的Android版本号。全部是Int常亮。可用于与SDK_INT进行比较来判断当前的系统版本

    public String CAMERA_INFO;

    public long CONFIG_VERSION;//机器配置文件版本号

    public String SCREEN_SIZE;//分辨率
    public int ORIENTATION;//屏幕朝向
    public String INSTALLER;//应用安装者

    public int BAUD_RATE;
    public String RFID_PROTOCOL;

    public String LONGITUDE; // 经度
    public String LATITUDE;  //纬度
    public long LOCATION_TIME;//
}
