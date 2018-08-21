package com.bbtree.cardreader.mqtt;

/**
 * Created by qiujj on 2017/5/4.
 */

public class MqttMsg {
    private String sn;//指令标号
    private int command;//指令

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }
}
