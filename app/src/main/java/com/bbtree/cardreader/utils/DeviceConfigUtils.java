package com.bbtree.cardreader.utils;

import android.text.TextUtils;

import com.bbtree.baselib.crypto.AESUtil;
import com.bbtree.baselib.net.GsonParser;
import com.bbtree.cardreader.config.DeviceConfig;
import com.orhanobut.logger.Logger;

/**
 * 设备配置获取
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/06/12
 * Time: 上午9:22
 */
public class DeviceConfigUtils {
    /**
     * 拿配置文件
     *
     * @return
     */
    public static DeviceConfig getConfig() {
        String deviceConfigSecret = SPUtils.getDeviceConfig(null);
        String secretKey = SPUtils.getSecretKey(null);
        if (TextUtils.isEmpty(secretKey) || TextUtils.isEmpty(deviceConfigSecret)) {
            return new DeviceConfig();
        }
        try {
            String jsonStr = AESUtil.decrypt(deviceConfigSecret,
                    secretKey.substring(0, secretKey.length() - 16),
                    secretKey.substring(secretKey.length() - 16, secretKey.length()));
            Logger.d(jsonStr);
            if (TextUtils.isEmpty(jsonStr)) {
                return new DeviceConfig();
            }
            return GsonParser.parse2Entity(jsonStr, DeviceConfig.class);
        } catch (Exception e) {
            Logger.i("" + e.getMessage());
        }
        return new DeviceConfig();
    }

}
