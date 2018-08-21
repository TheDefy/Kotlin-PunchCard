package com.bbtree.lib.bluetooth.temperature.beans;

/**
 * Function: 连接状态 包括正在连接  已连接  已断开
 * Created by BBTree Team
 * Author: LiuJihui
 * Create Date: 2015/10/27
 * Create Time: 14:33
 */
public class Conn {
    private int status;
    private String msg;

    public Conn(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public class ConnStatus {
        /**
         * 断开连接
         */
        public static final int DISCONNECT = 0;
        /**
         * 正在连接
         */
        public static final int CONNECTING = 1;
        /**
         * 连接成功
         */
        public static final int CONNECTED = 2;
    }
}
