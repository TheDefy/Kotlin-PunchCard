package com.bbtree.cardreader.hotfix.z2;

import android.text.TextUtils;

import com.bbtree.cardreader.utils.ShellUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2016/08/15
 * Create Time: 下午9:43
 */
public class CameraFix {

    public static boolean fix() {
        final String filePath = "/system/etc/camera.cfg";
        BufferedReader br = null;
        String line = null;
        StringBuffer buf = new StringBuffer();
        try {
            // 根据文件路径创建缓冲输入流
            br = new BufferedReader(new FileReader(filePath));
            // 循环读取文件的每一行, 对需要修改的行进行修改, 放入缓冲对象中
            while ((line = br.readLine()) != null) {
                // 此处根据实际需要修改某些行的内容
                if (line.equals("camera_facing = 1")) {
                    buf.append("camera_facing = 0");
                } else {// 如果不用修改, 则按原来的内容回写
                    buf.append(line);
                }
                buf.append(System.getProperty("line.separator"));
            }
            String text = buf.toString();
            if (!TextUtils.isEmpty(text)) {
                ShellUtils.execCommand("mount -o remount,rw /system", true);
                ShellUtils.execCommand("cp " + filePath + " /cache/camera.cfg.bak", true);
                ShellUtils.execCommand("echo \"" + text + "\" > " + filePath, true);
                ShellUtils.execCommand("chmod 0644 " + filePath, true);
            }
        } catch (IOException e) {
            return false;
        } finally {
            // 关闭流
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                }
            }
        }
        return true;
    }
}
