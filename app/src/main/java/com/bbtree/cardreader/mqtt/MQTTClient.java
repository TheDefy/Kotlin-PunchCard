package com.bbtree.cardreader.mqtt;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.orhanobut.logger.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * MQTT 客户端
 */
public class MQTTClient implements MqttCallback {

    private static final String DEBUG_TAG = "MQTTClient";

    /**
     * Handler Thread ID
     */
    private static final String MQTT_THREAD_NAME = "MqttService[" + DEBUG_TAG + "]";
    /**
     * 心跳包时间，毫秒
     */
    public static final int MQTT_KEEP_ALIVE = 5 * 1000;
    /**
     * 消息投放级别 QOS Level 0 (最多一次，有可能重复或丢失。 )
     */
    public static final int MQTT_QOS_0 = 0;
    /**
     * 消息投放级别 QOS Level 1 (至少一次，有可能重复。 )
     */
    public static final int MQTT_QOS_1 = 1;
    /**
     * 消息投放级别 QOS Level 2 (只有一次，确保消息只到达一次（用于比较严格的计费系统）。)
     */
    public static final int MQTT_QOS_2 = 2;

    private static final String MQTT_KEEP_ALIVE_TOPIC_FORAMT = "/users/%s/keepalive"; // Topic format for KeepAlives
    private static final byte[] MQTT_KEEP_ALIVE_MESSAGE = {0}; // 心跳包发送内容
    private static final int MQTT_KEEP_ALIVE_QOS = MQTT_QOS_0; //心跳包的发送级别默认最低
    /**
     * Instance Variable for Keepalive topic
     */
    private MqttTopic mKeepAliveTopic;

    private static final boolean MQTT_CLEAN_SESSION = true; // Start a clean session?

    private static final String MQTT_URL_FORMAT = "tcp://%s:%d"; // 推送url格式组装

    // Note:设备id限制长度为23个 字符
    // An NPE if you go over that limit
    private String mDeviceId;

    private boolean isPrepare = false;
    private boolean mStarted = false; //推送client是否启动
    public static int retryCount = 0;
    // Device ID, Secure.ANDROID_ID
    private Handler mConnHandler;      // Seperate Handler thread for networking

    private String mqttHost;//服务器地址
    private int mqttPort;// 服务器推送端口

    private MqttClient mClient;                    // Mqtt Client

    private ConnectivityManager mConnectivityManager; //网络改变接收器
    private MqttConnectOptions options;  // MQTT的连接设置

    private ConnectionLostListener connectionLostListener;//mqtt连接中断监听
    private EventListener eventListener;//收到消息监听

    /**
     * the global HXSDKHelper instance
     */
    private static MQTTClient me = null;

    protected MQTTClient() {
        me = this;
    }

    /**
     * get global instance
     *
     * @return
     */
    public static MQTTClient getInstance() {
        if (me == null)
            me = new MQTTClient();
        return me;
    }


    public MQTTClient init(Context mContext, String mDeviceId) {
        Logger.t(DEBUG_TAG).i("initMQTT");
        this.mDeviceId = mDeviceId;
        HandlerThread thread = new HandlerThread(MQTT_THREAD_NAME);
        thread.start();
        mConnHandler = new Handler(thread.getLooper());
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Activity.CONNECTIVITY_SERVICE);
        return this;
    }

    public MQTTClient addMqttOptions(MqttConnectOptions options) {
        this.options = options;
        return this;
    }

    public MQTTClient addServerHost(String MQTT_BROKER) {
        this.mqttHost = MQTT_BROKER;
        return this;
    }

    public MQTTClient addServerPort(int MQTT_PORT) {
        this.mqttPort = MQTT_PORT;
        return this;
    }


    public MQTTClient addEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
        return this;
    }

    /**
     * 尝试启动推送服务器，并注册网络改变接收器
     */
    public synchronized boolean start() {
        if (isPrepare) {
            Log.w(DEBUG_TAG, "尝试启动推送服务，但推送服务再启动");
            return false;
        }
        if (mStarted) {
            Log.w(DEBUG_TAG, "尝试启动推送服务，但推送服务已经启动");
            return false;
        }
        connect();
        return true;
    }

    /**
     * 停止推送服务
     */
    public synchronized void stop() {

        if (!mStarted) {
            Log.w(DEBUG_TAG, "试图停止推送服务器但是推送服务并没有运行");
            return;
        }

        if (mClient != null) {
            mConnHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mClient.disconnect();
                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }
//                    mClient = null;
                }
            });
        }
        mStarted = false;
    }

    /**
     * Connects to the broker with the appropriate datastore
     * 连接到推送服务器与适当的数据存储
     */
    private synchronized void connect() {
        isPrepare = true;
        String url = String.format(Locale.US, MQTT_URL_FORMAT, mqttHost, mqttPort);
        Log.i(DEBUG_TAG, "连接推送服务器 设备id：" + mDeviceId + "with URL:" + url);
        try {
            if (mClient == null)
                mClient = new MqttClient(url, mDeviceId, new MemoryPersistence());
        } catch (MqttException e) {
            e.printStackTrace();
        }
        mConnHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (options != null) {
                        mClient.connect(options);
                    } else {
                        mClient.connect();
                    }
                    retryCount = 0;
                    mClient.setCallback(MQTTClient.this);
                    mStarted = true; // Service is now connected
                    isPrepare = false;
                    if (eventListener != null) {
                        eventListener.onStart();
                    }
                } catch (MqttException e) {
                    isPrepare = false;
                    e.printStackTrace();
                }
            }
        });
    }

    //加入某个主题
    public void subscribe(String topicFilters, int qos) throws MqttException {
        if (!isConnected())
            return;
        mClient.subscribe(topicFilters, qos);
    }

    //退出某个主题
    public void unSubscribe(String topicFilters) throws MqttException {
        if (!isConnected())
            return;
        mClient.unsubscribe(topicFilters);
    }

    /**
     * 发送信息
     *
     * @param topicFilters
     * @param msg
     * @return
     * @throws MqttConnectivityException
     * @throws MqttException
     */
    public MqttDeliveryToken publish(String topicFilters, String msg)
            throws MqttConnectivityException, MqttException {
        if (!isConnected())
            throw new MqttConnectivityException();

        MqttTopic mqttTopic = mClient.getTopic(topicFilters);

        MqttMessage message = new MqttMessage(msg.getBytes());
        message.setQos(MQTT_KEEP_ALIVE_QOS);
        message.setRetained(true);
        return mqttTopic.publish(message);
    }


    /**
     * 重新连接如果他是必须的
     */
    public synchronized void reconnectIfNecessary() {
        if (mStarted && mClient == null) {
            connect();
        } else {
            Log.e(DEBUG_TAG, "重新连接没有启动，mStarted:" + String.valueOf(mStarted) + "mClient:" + mClient);
        }
    }

    /**
     * 判断推送服务是否连接
     *
     * @return 如果是连接的则返回true反之false
     */
    public boolean isConnected() {
        if (mStarted && mClient != null && !mClient.isConnected()) {
            Log.e(DEBUG_TAG, "判断推送服务已经断开");
        }

        if (mClient != null) {
            return (mStarted && mClient.isConnected()) ? true : false;
        }

        return false;
    }

    /**
     * 发送保持连接的指定的主题
     *
     * @return MqttDeliveryToken specified token you can choose to wait for completion
     */
    private synchronized MqttDeliveryToken sendKeepAlive()
            throws MqttConnectivityException, MqttException {
        if (!isConnected())
            throw new MqttConnectivityException();

        if (mKeepAliveTopic == null) {
            mKeepAliveTopic = mClient.getTopic(
                    String.format(Locale.US, MQTT_KEEP_ALIVE_TOPIC_FORAMT, mDeviceId));
        }

        Log.e(DEBUG_TAG, "向服务器发送心跳包url：" + mqttHost);

        MqttMessage message = new MqttMessage(MQTT_KEEP_ALIVE_MESSAGE);
        message.setQos(MQTT_KEEP_ALIVE_QOS);

        return mKeepAliveTopic.publish(message);
    }

    /**
     * 发送心跳数据到服务器
     */
    public synchronized void keepAlive() {
        if (isConnected()) {
            try {
                sendKeepAlive();
                return;
            } catch (MqttConnectivityException ex) {
                ex.printStackTrace();
                reconnectIfNecessary();
            } catch (MqttPersistenceException ex) {
                ex.printStackTrace();
                connectionLost(ex);
            } catch (MqttException ex) {
                ex.printStackTrace();
                connectionLost(ex);
            }
        }
    }

    /**
     * 连接丢失回调
     */
    @Override
    public void connectionLost(Throwable cause) {
        stop();
        if (connectionLostListener != null) {
            connectionLostListener.mqttLost();
        }
        Log.e(DEBUG_TAG, "connectionLost");
    }

    /**
     * 收到推送信息
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (eventListener != null) {
            eventListener.onEvent(topic, message);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            Log.w(DEBUG_TAG, "推送回调函数deliveryComplete方法执行---" + new String(token.getMessage().getPayload()));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * MqttConnectivityException Exception class
     */
    public class MqttConnectivityException extends Exception {
        private static final long serialVersionUID = -7385866796799469420L;
    }

    public void setConnectionLostListener(ConnectionLostListener lostListener) {
        this.connectionLostListener = lostListener;
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public interface ConnectionLostListener {
        void mqttLost();
    }

    public interface EventListener {
        void onStart();

        void onEvent(String topic, MqttMessage var1) throws UnsupportedEncodingException;
    }
}
