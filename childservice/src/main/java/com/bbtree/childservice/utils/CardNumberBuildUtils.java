package com.bbtree.childservice.utils;


import android.text.TextUtils;

import com.orhanobut.logger.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/05/20
 * Time: 下午6:58
 */
public class CardNumberBuildUtils {
    private static final int CardByteLength = 4 * 2;
    private static final String TAG = CardNumberBuildUtils.class.getSimpleName();
    private static CardNumberBuildUtils ourInstance = new CardNumberBuildUtils();

    private CardNumberBuildUtils() {
    }

    public static CardNumberBuildUtils getInstance() {
        return ourInstance;
    }

    public Long buildIDCard(byte[] original) {

        final String startMark = "02";
        final String endMark = "0D0A03";
        String hex = HexConver.byte2HexStr(original, original.length);
        String value = findIDCardNumber(hex, startMark, endMark);

        if (!TextUtils.isEmpty(value)) {
            byte[] numberData = HexConver.hexStr2Bytes(value.trim());
            try {
                return Long.valueOf(new String(numberData), 16);
            } catch (NumberFormatException e) {
//                e.printStackTrace();
                Logger.t(TAG).e(e.getMessage());
                return -1L;
            }
        }
        return 0L;
    }

    public Long buildIDCardV2(int protocol, byte[] original) {
        final String startMark = "02";
        final String endMark = "0D0A03";
        String hex = HexConver.byte2HexStr(original, original.length);
        String value = findIDCardNumber(hex, startMark, endMark);
        if (!TextUtils.isEmpty(value)) {
            byte[] numberData = HexConver.hexStr2Bytes(value.trim());
            int nowLength = numberData.length;
            Logger.t(TAG).i(">>>>>>value>>>>>>>>>>" + nowLength);
            int targetLength = Math.abs(protocol);
            if (targetLength < nowLength) {
                byte[] finalData = new byte[targetLength];
                int index = (nowLength - targetLength);
                System.arraycopy(numberData, index, finalData, 0, targetLength);
//                for (int i = index; i < nowLength; i++) {
//                    finalData[i - index] = numberData[i];
//                }
                numberData = finalData;
            }
            if (-protocol == Math.abs(protocol)) {
                reverse2(numberData);
            }
            try {
                return Long.valueOf(new String(numberData), 16);
            } catch (NumberFormatException e) {
//                e.printStackTrace();
                Logger.t(TAG).e(e.getMessage());
                return -1L;
            }
        }
        return 0L;
    }

    public static Long buildHSJ522BT(byte[] original) {
        final String startMark = "20000008";
        final String endMark = "03";
        String hex = HexConver.byte2HexStr(original, original.length);
        if (hex.startsWith(startMark) && hex.endsWith(endMark) && original.length == 14) {
            hex = hex.substring(16, 24);
            StringBuilder sb = new StringBuilder();
            for (int i = hex.length() / 2 - 1; i >= 0; i--) {
                sb.append(hex.charAt(i * 2));
                sb.append(hex.charAt(i * 2 + 1));
            }
            try {
                return Long.valueOf(sb.toString(), 16);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return -1L;
            }
        }
        return 0L;
    }

    public Long buildIQEQ(byte[] original) {
        int length = original.length;
        String hex = HexConver.byte2HexStr(original, length < 4 ? length : 4);
        if (!TextUtils.isEmpty(hex)) {
            try {
                return Long.valueOf(hex, 16);
            } catch (NumberFormatException e) {
//                e.printStackTrace();
                Logger.t(TAG).e(e.getMessage());
                return -1L;
            }
        }
        return 0L;
    }

    public Long buildICCard(byte[] original) {
        final String StartMark = "5A59480230";
        String hex = HexConver.byte2HexStr(original, original.length);
        int index = hex.indexOf(StartMark);
        if (index != -1) {
            int subEnd = CardByteLength + index + StartMark.length() + 1;

            if (subEnd >= hex.length()) {
                return 0L;
            }
            hex = hex.substring(index + StartMark.length(), subEnd);

            byte[] numberData = HexConver.hexStr2Bytes(hex);
            reverse(numberData);
            String hexDesc = HexConver.byte2HexStr(numberData, numberData.length);
            try {
                return Long.valueOf(hexDesc.trim(), 16);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return -1L;
            }
        } else {
            return 0L;
        }
    }

    public Long buildICCardV2(byte[] original) {
        final String StartMark = "5A59480230";
        String hex = HexConver.byte2HexStr(original, original.length);
        if (!hex.contains(StartMark)) {
            return 0L;
        }
        String[] result = null;
        try {
            result = hex.split(StartMark);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }

        int subEnd = CardByteLength;
        for (String s : result) {
            if (TextUtils.isEmpty(s)) {
                continue;
            }
            if (s.length() < subEnd) {
                continue;
            }
            String maybeCardData = s.substring(0, subEnd);
            Logger.i(">>>>>card RX Build:" + maybeCardData);
            byte[] numberData = HexConver.hexStr2Bytes(maybeCardData);
            reverse(numberData);
            String hexDesc = HexConver.byte2HexStr(numberData, numberData.length);
            try {
                return Long.valueOf(hexDesc.trim(), 16);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return -1L;
            }
        }
        return 0L;
    }

    public String findIDCardNumber(String original, String start, String end) {
        Pattern p = Pattern.compile(start + "(\\w+)" + end);
        Matcher m = p.matcher(original);
        while (m.find()) {
            return m.group(1);

        }
        return null;
    }

    private static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    private static void reverse2(byte[] array) {
        if (array == null || array.length % 2 == 1) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i + 1];
            array[i + 1] = tmp;

            tmp = array[j - 1];
            array[j - 1] = array[i];
            array[i] = tmp;
            j -= 2;
            i += 2;
        }
    }

}
