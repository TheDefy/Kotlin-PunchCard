package com.bbtree.cardreader.hotfix.zbox;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.text.TextUtils;

import com.bbtree.baselib.utils.PackageUtils;
import com.bbtree.cardreader.utils.ShellUtils;
import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Function:IQEQ转换智慧树
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2016/07/21
 * Create Time: 上午10:51
 */
public class IQEQ2BBtree {

    private static final String TAG = IQEQ2BBtree.class.getSimpleName();
    private static final String HotfixVersion = "_HotfixV1";

    public static boolean fix(Context context) {
        PackageUtils.uninstallSilent(context, "com.zhiqu.witbabydoor");
        PackageUtils.uninstallSilent(context, "com.zq.protectdoorschoolapp");
        ShellUtils.execCommand("wm size 1280x720", true);//fix display
        //(/system/custom_apk)
        final String display = Build.DISPLAY;
        Logger.d("Build.DISPLAY:" + display);
        int version = findVersionNumber(display);
        switch (version) {
            case 0:
                fixPreinstallAPK();
                fixBootLogo(context, version);
                return true;
            default:
                return false;
        }
    }

    private static void fixPreinstallAPK() {
        ShellUtils.execCommand("mount -o remount,rw /system", true);
        ShellUtils.execCommand("rm -rf /system/custom_apk/*", true);
        //// TODO: 16-7-21 初始化看门狗、工厂包
        ShellUtils.execCommand("rm -rf /system/app/ProtectDoorSchoolApp*", true);
        ShellUtils.execCommand("rm -rf /system/app/zhiquBabyDoor*", true);
        ShellUtils.execCommand("rm -rf /data/data/com.zhiqu.witbabydoor", true);
        ShellUtils.execCommand("rm -rf /data/data/com.zq.protectdoorschoolapp", true);
    }

    /**
     * 找出目前的补丁版本号
     *
     * @param original
     * @return
     */
    private static int findVersionNumber(String original) {
        Pattern p = Pattern.compile("(\\w*)(HotfixV)(\\d*)");
        Matcher m = p.matcher(original);
        while (m.find()) {
            return Integer.parseInt(m.group(3));
        }
        return 0;
    }

    private static boolean fixBootLogo(Context context, int version) {

        final String path = "hotfix/iqeq";
        final String fileName = "bootanimation.zip";
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        String absPath = null;
        boolean isFound = false;
        try {
            files = assetManager.list(path);
            if (files == null || files.length < 1) {
                Logger.d("files:hotfix/iqeq null");
                return false;
            }

            for (String file : files) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!isFound || TextUtils.isEmpty(absPath)) {
            return false;
        }
        ShellUtils.execCommand("mount -o remount,rw /system", true);
        final String shellCmd = "cp " + absPath + " /system/media/" + fileName;
        Logger.d("shellCmd:" + shellCmd);
        ShellUtils.execCommand(shellCmd, true);
        ShellUtils.execCommand("chmod 0644 /system/media/" + fileName, true);
        editBuild(version);
        return true;

    }


    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private static void editBuild(int version) {
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
                if (line.startsWith("ro.build.display.id=ZBOX_V")) {
                    final String nowVersion = "_HotfixV" + version;
                    line = line.replaceAll(nowVersion, "");
                    String result = line + HotfixVersion;
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
