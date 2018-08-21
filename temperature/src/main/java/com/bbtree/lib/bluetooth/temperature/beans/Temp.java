package com.bbtree.lib.bluetooth.temperature.beans;

/**
 * Function:  温度数据对象
 * Created by BBTree Team
 * Author: LiuJihui
 * Create Date: 2015/10/27
 * Create Time: 14:37
 */
public class Temp {
    private float temp;
    private byte[] buf_temp;

    public Temp(float temp, byte[] buf_temp) {
        this.temp = temp;
        this.buf_temp = buf_temp;
    }

    public float getTemp() {
        return temp;
    }

    public byte[] getBuf_temp() {
        return buf_temp;
    }
}
