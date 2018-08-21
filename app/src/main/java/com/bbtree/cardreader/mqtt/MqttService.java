package com.bbtree.cardreader.mqtt;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.bbtree.baselib.base.BaseService;
import com.orhanobut.logger.Logger;

public class MqttService extends BaseService implements MQTTClient.ConnectionLostListener {

    private String TAG = "MqttService";
    public static String MQTT_CLIENT_ID = "MQTT_SERVICE";

    /**
     * Action to start 启动
     */
    private static final String ACTION_START = MQTT_CLIENT_ID + ".START";
    /**
     * Action to stop 停止
     */
    private static final String ACTION_STOP = MQTT_CLIENT_ID + ".STOP";
    /**
     * Action to keep alive used by alarm manager保持心跳闹钟使用
     */
    private static final String ACTION_KEEPALIVE = MQTT_CLIENT_ID + ".KEEPALIVE";
    /**
     * Action to reconnect 重新连接
     */
    private static final String ACTION_RECONNECT = MQTT_CLIENT_ID + ".RECONNECT";
    /**
     * 闹钟
     */
    private AlarmManager mAlarmManager;

    /**
     * Initalizes the DeviceId and most instance variables
     * Including the Connection Handler, Datastore, Alarm Manager
     * and ConnectivityManager.
     * 初始化设备id和请求参数包含连接处理、数据存储、闹钟警报、网络接收器
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mAlarmManager = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
        MQTTClient.getInstance().setConnectionLostListener(this);
    }

    @Override
    public boolean isRestricted() {
        return super.isRestricted();
    }

    /**
     * Service onStartCommand
     * Handles the action passed via the Intent
     * 通过意图处理服务
     *
     * @return START_REDELIVER_INTENT
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        String action = intent.getAction();
        Logger.i("推送服务接收到一个请求" + action);

        if (action == null) {
            Logger.i("推送服务接收到的请求为null！推送服务不执行任何操作");
        } else {
            if (action.equals(ACTION_START)) {
                Logger.i("接收到《启动》推送服务命令");
                MQTTClient.getInstance().start();
//                if(start){
//                    if(hasScheduledKeepAlives()){
//                        stopKeepAlives();
//                    }
//                }
//                startKeepAlives();
            } else if (action.equals(ACTION_STOP)) {
                Logger.i("接收到《停止》推送服务命令");
                MQTTClient.getInstance().stop();
//                if(hasScheduledKeepAlives()){
//                    stopKeepAlives();
//                }
            } else if (action.equals(ACTION_KEEPALIVE)) {
                Logger.i("接收到《发送心跳包》推送服务命令");
//                MQTTClient.getInstance().keepAlive();
            } else if (action.equals(ACTION_RECONNECT)) {
                Logger.i("接收到《重启》推送服务命令");
                MQTTClient.getInstance().reconnectIfNecessary();
            }
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    /**
     * 查询是否已经有一个心跳包的闹钟
     *
     * @return 如果已经有一个心跳包的闹钟则返回true反之false
     */
    private synchronized boolean hasScheduledKeepAlives() {
        Intent i = new Intent();
        i.setClass(this, MqttService.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_NO_CREATE);

        return (pi != null) ? true : false;
    }

    /**
     * 启动心跳包闹钟
     */
    private void startKeepAlives() {
        Intent i = new Intent();
        i.setClass(this, MqttService.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + MQTTClient.MQTT_KEEP_ALIVE,
                MQTTClient.MQTT_KEEP_ALIVE, pi);
    }

    /**
     * 取消已经存在的闹钟
     */
    private void stopKeepAlives() {
        Intent i = new Intent();
        i.setClass(this, MqttService.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        mAlarmManager.cancel(pi);
    }

    /**
     * 启动推送服务
     *
     * @param ctx context to start the service with
     * @return void
     */
    public static void actionStart(Context ctx) {
        Logger.i("actionStart");
        Intent i = new Intent(ctx, MqttService.class);
        i.setAction(ACTION_START);
        ctx.startService(i);
    }

    /**
     * 停止推送服务
     *
     * @param ctx context to start the service with
     * @return void
     */
    public static void actionStop(Context ctx) {
        Intent i = new Intent(ctx, MqttService.class);
        i.setAction(ACTION_STOP);
        ctx.startService(i);
    }

    /**
     * 发送心跳包
     *
     * @param ctx context to start the service with
     * @return void
     */
    public static void actionKeepalive(Context ctx) {
        Intent i = new Intent(ctx, MqttService.class);
        i.setAction(ACTION_KEEPALIVE);
        ctx.startService(i);
    }

    @Override
    public void mqttLost() {
        stopSelf();
//        stopKeepAlives();
        if (MQTTClient.retryCount < 5) {
            MQTTClient.retryCount++;
            actionStart(getApplication());
        }
    }

}
