package com.bbtree.lib.bluetooth.temperature.utils;

/**
 * 转换工具
 */
public class ChangeUtils {

    /**
     * 蓝牙枪温度转换算法
     *
     * @param value
     * @return
     */
    public static float getTemp(byte[] value) {
        if (value == null)
            return 0f;
        if (value.length == 4) {
            if (value[0] != (byte) 0xff)
                return 0f;
            if ((value[1] ^ value[2]) == value[3]) {
                short fBuff = getShort(new byte[]{value[1], value[2]}, 0);
                return fBuff / (float) 10;
            } else {
                return 0f;
            }
        }

        return 0f;
    }

    // JXB-182 数据解析
    public static String getJXBTemp(byte[] value) {
        if (value == null)
            return null;
        if (value.length == 13) {
            if (value[0] != (byte) 0xfa)
                return "head error";
            //摄氏度c
            int tmp5, tmp6;
            tmp5 = value[5];
            tmp6 = value[6];
            if (value[5] < 0) {
                tmp5 = 256 + value[5];
            }
            if (value[6] < 0) {
                tmp5 = 256 + value[6];
            }
            float cResult = tmp6 * 256 + tmp5;
            return cResult / 10 + "";
        }
        return null;
    }

    /**
     * 通过byte数组取到short
     *
     * @param b
     * @param index 第几位开始取
     * @return
     */
    public static short getShort(byte[] b, int index) {
        return (short) (((b[index + 0] << 8) | b[index + 1] & 0xff));
    }

    /**
     * hex to String
     *
     * @param b byte数组
     * @return
     */
    public static String HexToString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex);
        }
        return sb.toString();

    }
}
