package com.bbtree.cardreader;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.bbtree.baselib.base.AppBlockCanaryContext;
import com.bbtree.baselib.base.BaseApp;
import com.bbtree.baselib.net.BaseParam;
import com.bbtree.baselib.utils.FileUtils;
import com.bbtree.baselib.utils.PackageUtils;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.config.DeviceConfig;
import com.bbtree.cardreader.database.MyEncryptedSQLiteOpenHelper;
import com.bbtree.cardreader.greendao.gen.DaoMaster;
import com.bbtree.cardreader.greendao.gen.DaoSession;
import com.bbtree.cardreader.mqtt.MQTTHelper;
import com.bbtree.cardreader.utils.SPUtils;
import com.bbtree.cardreader.utils.ScreenUtils;
import com.bbtree.cardreader.view.activity.SplashActivity;
import com.bbtree.childservice.utils.MD5;
import com.crashlytics.android.Crashlytics;
import com.github.moduth.blockcanary.BlockCanary;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.picasso.Picasso;

import org.greenrobot.greendao.database.Database;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

/**
 * Created by zzz on 16/01/2017.
 */

public class BBTreeApp extends Application implements Thread.UncaughtExceptionHandler {
    private final static String CARAMA_DEGREES = "/.caramaDegrees";
    private final static int NOT_IN_SP = -1;
    private final static int NOT_IN_FILE = -2;
    private static final String MY_PWD = "666";
    private static BBTreeApp app;
    private DeviceConfig deviceConfig;
    private String ossIp; //oss 解析ip地址
    private boolean isDBEncrypt = !BuildConfig.DEBUG;



    /**
     * -1 代表未设置
     * -2 代表文件系统没有
     */
    private static int degrees = -1; //设置摄像头旋转角度
    private DaoSession mDaoSession;

    @Override
    public void onCreate() {
        super.onCreate();


        BaseApp.initialize(this);
        app = this;
        Logger
                .init("BBtree App")                 // default PRETTYLOGGER or use just start()
                .logLevel(true ? LogLevel.FULL : LogLevel.NONE)        // default LogLevel.FULL
                .methodOffset(2);


        if (BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) {

                // This process is dedicated to LeakCanary for heap analysis.
                // You should not start your app in this process.
                return;
            }
            LeakCanary.install(this);
            BlockCanary.install(this, new AppBlockCanaryContext()).start();
        }

//        List<Speaker> allColumns = getDaoSessionInstance().getSpeakerDao().queryBuilder().list();
////        for (CardRecord s : allColumns){
//            Logger.e("allColumns = " + allColumns.size());
////        }

        BaseApp.getInstance().setDebug(BuildConfig.isDebug);
        BaseApp.getInstance().setFactory(BuildConfig.isFactory);

        MQTTHelper.getInstance().initMQTT(this);

        initBugCollect();

        final String processName = PackageUtils.getCurrentProcessName(this);
        if (!TextUtils.equals(processName, getPackageName())) {
            return;
        }
        initAnalytics();
        initPicasso();

        ScreenUtils.init(this);

        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 获取数据库存储daoSessino
     *
     * @return
     */
    public DaoSession getDaoSessionInstance() {
        if (mDaoSession == null) {
            synchronized (BBTreeApp.class) {
                if (mDaoSession == null) {
                    MyEncryptedSQLiteOpenHelper helper = new MyEncryptedSQLiteOpenHelper(app, Constant.DBInfo.DB_NAME, null);
                    if (Build.MODEL.equals(Constant.PlatformAdapter.CS3C)
                            || Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V5)
                            || Build.MODEL.equals(Constant.PlatformAdapter.ZBOX_V4)
                            || Build.MODEL.equals(Constant.PlatformAdapter.PS_A210)
                            || Build.MODEL.equals(Constant.PlatformAdapter.SOFTWINER_EVB)) {
                        isDBEncrypt = false;
                    }
                    Database db = isDBEncrypt ? helper.getEncryptedWritableDb(MD5.calculateMD5("BBTREE_CARD")) : helper.getWritableDb();
                    mDaoSession = new DaoMaster(db).newSession();
                }
            }
        }
        return mDaoSession;
    }

    public static BBTreeApp getApp() {
        return app;
    }

    /**
     * 统计分析
     */
    private void initAnalytics() {
//        Countly.sharedInstance().init(this, getResources().getString(R.string.countlyUrl), getResources().getString(R.string.countlyKey));

    }

    /**
     * 错误日志收集系统
     */
    private void initBugCollect() {
        Fabric.with(this, new Crashlytics());

//        CrashReport.CrashHandleCallback crashHandleCallback = new CrashReport.CrashHandleCallback() {
//            @Override
//            public synchronized Map<String, String> onCrashHandleStart(int crashType, String errorType, String errorMessage, String errorStack) {
//
//                FileUtils.writeFileSdcardFile(FileUtils.getExternalDir(BBTreeApp.getApp()) + "/crashTemp", crashType +"/n"+ errorType +"/n"+ errorMessage +"/n"+ errorStack);
//                CrashSaver.record(crashType, errorType, errorMessage, errorStack);
//                if (!BuildConfig.DEBUG) checkRestartStatus();
//                return super.onCrashHandleStart(crashType, errorType, errorMessage, errorStack);
//            }
//        };
//        CrashReport.UserStrategy userStrategy = new CrashReport.UserStrategy(this);
//        userStrategy.setCrashHandleCallback(crashHandleCallback);
//        CrashReport.initCrashReport(this, getResources().getString(R.string.buglyid), BuildConfig.isDebug, userStrategy);
//        CrashReport.putUserData(this, "IMEI", BaseParam.getInstance().getDeviceId());
//        String alias = getMachineAlias();
//        if (TextUtils.isEmpty(alias)) {
//            alias = BaseParam.getInstance().getDeviceId();
//        }
//        CrashReport.setUserId(alias);
    }

    /**
     * Picasso图片加载
     */
    private void initPicasso() {
        Picasso.Builder picassoBuilder = new Picasso.Builder(this);
//        picassoBuilder.downloader(new OkHttp3Downloader(APIFactory.getInstance().getRequestClient()));
        Picasso picasso = picassoBuilder.build();
        picasso.setLoggingEnabled(PackageUtils.isDebuggable(this));
        try {
            Picasso.setSingletonInstance(picasso);
        } catch (IllegalStateException e) {
            // Picasso instance was already set
            // cannot set it after Picasso.with(Context) was already in use
        }
    }

    /**
     * 用于处理摄像头旋转角度问题
     *
     * @param degrees
     */
    public void setDegrees(int degrees) {
        BBTreeApp.degrees = degrees;
        SPUtils.setDegrees(degrees);
        FileUtils.writeFileSdcardFile(FileUtils.getExternalDir(this) + CARAMA_DEGREES, degrees + "");
    }

    public int getDegrees() {
        if (degrees == NOT_IN_SP) {
            int degrees_in_sp = SPUtils.getDegrees(-1);
            degrees = degrees_in_sp;
            if (degrees_in_sp == NOT_IN_SP) {

                String s = FileUtils.readFileSdcardFile(FileUtils.getExternalDir(this) + CARAMA_DEGREES);
                if (!TextUtils.isEmpty(s)) {
                    String replace = s.replace("\n", "");
                    degrees = Integer.valueOf(replace);
                } else {
                    degrees = NOT_IN_FILE;
                }
            }
        } else if (degrees == NOT_IN_FILE) {
            return degrees;
        }
        if (degrees == 0) {
            degrees = SPUtils.getDegrees(-1);
        }
        return degrees;
    }

    public String getMachineAlias() {

        String alias = SPUtils.getDeviceAlias("");
        if (TextUtils.isEmpty(alias)) {
            return BaseParam.getDeviceId();
        }
        return alias;
    }

    /**
     * 应用意外之后重启,每次只能有一个崩溃进来重启
     */
    private synchronized void checkRestartStatus() {
        if (!TextUtils.equals(PackageUtils.getCurrentProcessName(this), getPackageName())) {
            //如果不是主进程崩溃，就不用重启应用了
            return;
        }
        //判断当天是否满足五次崩溃
        String crashInfo = SPUtils.getCrashInfo("");
        Logger.d(crashInfo);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String today = sdf.format(new Date());
        int count = 0;
        if (crashInfo != null) {
            String[] crash = crashInfo.split("-");
            if (crash.length == 2) {
                String date = crash[0];
                String times = crash[1];
                count = Integer.parseInt(times);
                boolean failToday = TextUtils.equals(today, date);
                if (failToday && count > 5) {
                    return;
                } else if (!failToday) {
                    count = 0;
                }
            }
        }
        count++;
        SPUtils.setCrashInfo(today + "-" + count);

        restartApp(5000);
    }

    /**
     * 重启
     *
     * @param timeMillis 重启间隔毫秒数
     */
    public synchronized void restartApp(int timeMillis) {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent restartIntent = PendingIntent.getActivity(this, 0, intent, 0);
        //退出程序
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + timeMillis, restartIntent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 停用迈德看门狗以防系统重启
     */
    public void disableMDWatchDog() {
        sendBroadcast(new Intent(Constant.WatchDogInfo.MAIDE_DISABLE));
    }

    /**
     * 启用迈德看门狗
     */
    public void enableMDWatchDog() {
        sendBroadcast(new Intent(Constant.WatchDogInfo.MAIDE_ENABLE));
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (!BuildConfig.DEBUG) {
            FileUtils.writeFileSdcardFile(FileUtils.getExternalDir(BBTreeApp.getApp()) + "/crash", e.getMessage());
            checkRestartStatus();
            /*Intent intent = new Intent(this, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            android.os.Process.killProcess(android.os.Process.myPid());*/
        }
    }
}
