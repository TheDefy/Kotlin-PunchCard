package com.bbtree.cardreader.hotfix.cs3plus;

import android.text.TextUtils;

import com.bbtree.cardreader.utils.ShellUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2016/01/05
 * Time: 下午4:20
 */
public class MemorySizeFix {

    public static boolean fix() {
        final String filePath = "/system/build.prop";
        BufferedReader br = null;
        String line = null;
        StringBuffer buf = new StringBuffer();
        InputStream is = null;
        boolean needReboot = false;
        try {

            is = new FileInputStream(filePath);
            Properties prop = new Properties();
            prop.load(new InputStreamReader(is, "UTF-8"));
            is.close();
            final String heapLimit = "dalvik.vm.heapgrowthlimit";
            final boolean contains = prop.containsKey(heapLimit);
            if (contains) {
                String value = prop.getProperty(heapLimit);
                if (!value.equals("64m")) {
                    return false;
                }
                // 根据文件路径创建缓冲输入流
                br = new BufferedReader(new FileReader(filePath));
                // 循环读取文件的每一行, 对需要修改的行进行修改, 放入缓冲对象中
                while ((line = br.readLine()) != null) {
                    // 此处根据实际需要修改某些行的内容
                    if (line.equals("dalvik.vm.heapgrowthlimit=64m")) {
                        buf.append("dalvik.vm.heapgrowthlimit=96m");
                    } else if (line.startsWith("ro.build.display.id=CS3Plus-eng")) {
                        buf.append("ro.build.display.id=CS3Plus-eng 4.4. 4 KTU84Q eng.EngrZhou.20151209.180030 test-keys");
                    } else if (line.startsWith("ro.build.description=CS3Plus-eng")) {
                        buf.append("ro.build.description=CS3Plus-eng 4.4.4 KTU84Q eng.EngrZhou.20151209.180030 test-keys");
                    } else {// 如果不用修改, 则按原来的内容回写
                        buf.append(line);
                    }
                    buf.append(System.getProperty("line.separator"));
                }

                String text = buf.toString();
                if (!TextUtils.isEmpty(text)) {
                    ShellUtils.execCommand("mount -o remount,rw /system", true);
                    ShellUtils.execCommand("mv " + filePath + " /system/build.prop.bak", true);
                    ShellUtils.execCommand("echo \"" + text + "\" > " + filePath, true);
                    ShellUtils.execCommand("chmod 0644 " + filePath, true);
                }
                needReboot = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            needReboot = false;
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
        return needReboot;
    }

    //        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                InputStream is = null;
//                try {
//                    final String filePath = "/system/build.prop";
//                    is = new FileInputStream(filePath);
//                    Properties prop = new Properties();
//                    prop.load(new InputStreamReader(is, "UTF-8"));
//                    is.close();
//                    final String heapLimit = "dalvik.vm.heapgrowthlimit";
//                    final boolean contains = prop.containsKey(heapLimit);
//                    if (contains) {
//                        String value = prop.getProperty(heapLimit);
//                        String value2 = prop.getProperty("ro.build.date");
//                        LOG.t(TAG).i("ro.build.date:" + value2);
//                        if (value.equals("64m")) {
//                            ShellUtils.execCommand("mount -o remount,rw /system", true);
//                            final String outPath = mContext.getCacheDir().getAbsolutePath() + File.separator + "build.prop";
//                            ShellUtils.execCommand("cp " + filePath + " /system/build.prop.bak", true);
//                            ShellUtils.execCommand("mv " + filePath + " " + outPath, true);
//                            prop.put(heapLimit, "96m");
//                            prop.put("ro.build.display.id", "CS3Plus-eng 4.4.4 KTU84Q eng.EngrZhou.20151209.162235 test-keys");
//                            prop.put("ro.build.description", "CS3Plus-eng 4.4.4 KTU84Q eng.EngrZhou.20151209.162235 test-keys");
//                            LOG.t(TAG).i("out put file SENDERPATH:" + outPath);
//                            OutputStream os = new FileOutputStream(outPath);
//                            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
//                            prop.store(osw, null);
//                            os.close();
//                            ShellUtils.execCommand("cp " + outPath + " " + filePath, true);
//                            ShellUtils.execCommand("chmod 0644 " + filePath, true);
//                            ShellUtils.execCommand("reboot", true);
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
}
