package com.bbtree.cardreader.hotfix.cs3;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.text.TextUtils;

import com.bbtree.cardreader.utils.ShellUtils;
import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2016/01/05
 * Time: 下午4:22
 */
public class BLEFix {
    private static final String TAG = BLEFix.class.getSimpleName();

    public static boolean fix(Context context) {
        boolean needReboot;
        final String display = Build.DISPLAY;
        Logger.d("Build.DISPLAY:" + display);
        if (display.contains("EngrZhou") && display.contains("CS3") && !display.contains("BLE")) {
            AssetManager assetManager = context.getAssets();
            String[] files = null;
            final String path = "hotfix/cs3";
            final String fileName = "bluetooth.default.so";
            try {
                files = assetManager.list(path);
                if (files == null || files.length < 1) {
                    Logger.d("files:hotfix/cs3 null");
                    return false;
                }
                boolean isFound = false;
                String absPath = null;
                for (String file : files) {
                    // Initialize streams
                    InputStream in = null;
                    OutputStream out = null;
                    Logger.d("files:" + file);
                    if (TextUtils.equals(fileName, file)) {
                        isFound = true;
                        File cacheFile = new File(context.getCacheDir(), fileName);
                        absPath = cacheFile.getAbsolutePath();
                        in = assetManager.open(path + File.separator + file);
                        out = new FileOutputStream(cacheFile);
                        copyFile(in, out);
                        in.close();
                        in = null;
                        out.flush();
                        out.close();
                        out = null;
                    }
                }
                if (!isFound || TextUtils.isEmpty(absPath)) {
                    return false;
                }

                Logger.d("absPath:" + absPath);
                ShellUtils.execCommand("mount -o remount,rw /system", true);
                final String shellCmd = "cp " + absPath + " /system/lib/hw/bluetooth.default.so";
                Logger.d("shellCmd:" + shellCmd);
                ShellUtils.execCommand(shellCmd, true);
                ShellUtils.execCommand("chmod 0644 /system/lib/hw/bluetooth.default.so", true);
                editBuild();
                needReboot = true;
            } catch (IOException e) {
                e.printStackTrace();
                needReboot = false;
            }
            return needReboot;
        } else {
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    private static void editBuild() {
        final String filePath = "/system/build.prop";
        BufferedReader br = null;
        String line = null;
        StringBuffer buf = new StringBuffer();
        try {
            // 根据文件路径创建缓冲输入流
            br = new BufferedReader(new FileReader(filePath));
            // 循环读取文件的每一行, 对需要修改的行进行修改, 放入缓冲对象中
            while ((line = br.readLine()) != null) {
                // 此处根据实际需要修改某些行的内容
                if (line.startsWith("ro.build.display.id=CS3-eng")) {
                    String result = line.replace("eng.EngrZhou", "BLE.EngrZhou");
                    buf.append(result);
                } else if (line.startsWith("ro.build.description=CS3-eng")) {
                    String result = line.replace("eng.EngrZhou", "BLE.EngrZhou");
                    buf.append(result);
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
        } catch (IOException e) {
            e.printStackTrace();
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

    }
}
