package com.bbtree.cardreader.mqtt;

import android.content.Context;
import android.text.TextUtils;

import com.bbtree.baselib.net.GsonParser;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.common.Instruction;
import com.bbtree.cardreader.entity.eventbus.ScreenSaverCMD;
import com.bbtree.cardreader.entity.eventbus.TempConfigEventBus;
import com.bbtree.cardreader.model.CardInfoModule;
import com.bbtree.cardreader.model.DataInfoModule;
import com.bbtree.cardreader.report.FailRecordReport;
import com.bbtree.cardreader.service.UpdateCheckService;
import com.orhanobut.logger.Logger;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MQTTHelper {

    private final String TAG = "MQTTHelper";

    /**
     * 设备id的前缀
     */
    private static final String DEVICE_ID_FORMAT = "cmd_%s";
    /**
     * 是否在线
     */
    private static final String lineTopic = "/base/cardreader/alive";
    /**
     * 推送消息给服务器
     */
    private static final String toServerTopic = "/cardreader/toserver";
    /**
     * 接收消息topic
     */
    public static String selfToptic = "/issue/machine/%s";

    private Context mContext;

    /**
     * the global HXSDKHelper instance
     */
    private static MQTTHelper me = null;

    protected MQTTHelper() {
        me = this;
    }

    /**
     * get global instance
     *
     * @return
     */
    public static MQTTHelper getInstance() {
        if (me == null){
            me = new MQTTHelper();
        }
        return me;
    }

    public void initMQTT(Context mContext) {
        Logger.i("initMQTT");
        this.mContext = mContext;
        //初始化设备id，长度不能超过23
        String mDeviceId = BBTreeApp.getApp().getMachineAlias();
        selfToptic = String.format(selfToptic, mDeviceId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        // TODO: 2017/2/14 设置username和password,暂时没有
        options.setUserName("admin");
        options.setPassword("public".toCharArray());
        // 设置超时时间
        options.setConnectionTimeout(10);
        // 设置会话心跳时间
        options.setKeepAliveInterval(5);
        //离线设置
        options.setWill(lineTopic, ("0_" + selfToptic).getBytes(), 2, true);

        MQTTClient.getInstance().init(mContext, String.format(DEVICE_ID_FORMAT, mDeviceId))
                .addMqttOptions(options)
                .addServerHost("114.215.202.56")
                .addServerPort(1883);
        initEventListener();
    }

    public void initEventListener() {
        Logger.i("initEventListener");
        MQTTClient.getInstance().setEventListener(new MQTTClient.EventListener() {
            @Override
            public void onStart() {
                try {
                    MQTTClient.getInstance().subscribe(selfToptic, MQTTClient.MQTT_QOS_2);
                    MQTTClient.getInstance().publish(lineTopic, "1_" + selfToptic);

                } catch (MqttException e) {
                    e.printStackTrace();
                    try {
                        MQTTClient.getInstance().publish(lineTopic, "1_" + selfToptic);
                    } catch (MQTTClient.MqttConnectivityException | MqttException e1) {
                        e1.printStackTrace();
                    }
                } catch (MQTTClient.MqttConnectivityException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onEvent(String topic, MqttMessage message) throws UnsupportedEncodingException {
                Logger.i("onEvent", "收到推送信息如下" +
                        "Topic:" + topic +
                        "Message:" + URLDecoder.decode(new String(message.getPayload(), "utf-8"), "utf-8") +
                        "QoS:" + message.getQos());
                // 是自己订阅的内容才做处理
                if (TextUtils.equals(selfToptic, topic)) {
                    try {
                        String msg = URLDecoder.decode(new String(message.getPayload(), "utf-8"), "utf-8");
                        MqttMsg mqttMsg = new GsonParser().parse2Entity(msg, MqttMsg.class);
                        String sn = mqttMsg.getSn();
                        int command = mqttMsg.getCommand();
                        if (TextUtils.isEmpty(sn)) {
                            sn = "";
                        }
                        switch (command) {
                            case Instruction.CardsPull:
                                CardInfoModule.getInstance().getCards(mqttMsg.getSn());
                                break;
                            case Instruction.ClearCards:
                                BBTreeApp.getApp().getDaoSessionInstance().getCardInfoDao().deleteAll();
                                break;
                            case Instruction.DeleteCards:
                                CardInfoModule.getInstance().curdCards(mqttMsg.getSn(), CardInfoModule.DELETECARD);
                                break;
                            case Instruction.AddCards:
                                CardInfoModule.getInstance().curdCards(mqttMsg.getSn(), CardInfoModule.ADDCARD);
                                break;
                            case Instruction.PullDeviceConfig:
                                DataInfoModule.getInstance().getDeviceConfig(mqttMsg.getSn());
                                break;
                            case Instruction.PullFailedRecord:

                                //注释了
                                break;
                            case Instruction.InstallAPK:
                                UpdateCheckService.startCheckUpdate(mContext);
                                break;
                            case Instruction.ScreenSaverUpdate:
                                screenSaveUpdate(mqttMsg);
                                break;
                            case Instruction.CardsPush:
                                break;
                            case Instruction.SwitchTemp:
                                EventBus.getDefault().post(new TempConfigEventBus());
                                break;
                            case Instruction.GetSpeakerConfig:
                                break;
                            case Instruction.QueryUploadFail:
                                FailRecordReport.getInstance().getReport(BBTreeApp.getApp(), mqttMsg.getSn());
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void screenSaveUpdate(MqttMsg commandEntity) {
        ScreenSaverCMD cmd = new ScreenSaverCMD();
        cmd.cmd = ScreenSaverCMD.ScreenSaverAction.screenSaveUpdate;
        cmd.sn = commandEntity.getSn();
        EventBus.getDefault().post(cmd);
    }
}
