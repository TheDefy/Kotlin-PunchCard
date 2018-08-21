package com.bbtree.cardreader.utils;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/09/22
 * Create Time: 下午5:11
 */
public class CameraInfoUtils {

    /**
     * 获取机器摄像头设备信息
     *
     * @return
     */
    public static String getInfo() {
        String allVideo = ShellUtils.execCommand("cd /dev/ && ls | grep video", true).successMsg;
        String video0 = ShellUtils.execCommand("cat /sys/class/video4linux/video0/name", true).successMsg;
        String video1 = ShellUtils.execCommand("cat /sys/class/video4linux/video1/name", true).successMsg;
        return allVideo + "|" + video0 + "|" + video1;
    }
}
