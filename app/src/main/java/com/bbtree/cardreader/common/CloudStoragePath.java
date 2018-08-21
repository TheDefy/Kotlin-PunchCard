package com.bbtree.cardreader.common;

import android.text.TextUtils;
import com.bbtree.baselib.utils.StringUtils;
import com.bbtree.cardreader.utils.SPUtils;
import java.math.BigInteger;
import java.util.Date;
import java.util.Random;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/07/13
 * Create Time: 下午9:22
 */
public class CloudStoragePath {
    private static CloudStoragePath ourInstance = new CloudStoragePath();
    private static Random mRandom;

    private CloudStoragePath() {
    }

    public static CloudStoragePath getInstance() {
        mRandom = new Random(1000);
        return ourInstance;
    }

    public String getServerPath() {
        BigInteger bg = new BigInteger(String.valueOf(System.currentTimeMillis() + Math.abs(mRandom.nextLong())), 10);
        StringBuilder schoolID = new StringBuilder(String.valueOf(SPUtils.getSchoolId(0L)));
        if (TextUtils.isEmpty(schoolID.toString())) {
            schoolID.append("Camera");
        }
        schoolID.append("/");
        schoolID.append(StringUtils.formatDate(new Date(), "yyyyMM"));
        schoolID.append("/");
        String filePath = schoolID + bg.toString(32) + "_";
        return filePath;
    }
}
