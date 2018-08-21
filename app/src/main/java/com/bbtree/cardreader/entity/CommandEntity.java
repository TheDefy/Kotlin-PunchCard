package com.bbtree.cardreader.entity;

/**
 * Created by zhouyl on 19/04/2017.
 */

public class CommandEntity extends BaseEntity{
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
