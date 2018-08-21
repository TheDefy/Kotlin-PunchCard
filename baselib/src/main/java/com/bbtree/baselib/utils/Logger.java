package com.bbtree.baselib.utils;

import android.os.Environment;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/09/21
 * Create Time: 下午1:21
 */
public class Logger {
    private static final String TAG = Logger.class.getSimpleName();
    private static Logger ourInstance = new Logger();
    private static SimpleDateFormat format;
    private static SimpleDateFormat formatDetails;

    private Logger() {
    }

    public static Logger getInstance() {
        if (format == null) {
            format = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        }
        if (formatDetails == null) {
            formatDetails = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        }
        return ourInstance;
    }

    public void log(String s) {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        if (externalStorageDirectory == null) {
            com.orhanobut.logger.Logger.t(TAG).e("Environment.getExternalStorageDirectory() == null");
            return;
        }
        try {
            File file = new File(externalStorageDirectory.getPath() + File.separator
                    + "LOG" + File.separator + format.format(new Date()) + ".log");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileWriter fw = new FileWriter(file, true);
            String date = formatDetails.format(new Date());
            fw.write(date + " : " + s);
            fw.write("\n");
            fw.close();
        } catch (IOException ex) {
            com.orhanobut.logger.Logger.t(TAG).e(ex.getMessage());
        }

    }
}
