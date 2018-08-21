package com.bbtree.cardreader.config;

import android.os.Build;

/**
 * Function:
 * Created by BBTree Team
 * Author: LiuJihui
 * Create Date: 2015/10/29
 * Create Time: 20:45
 */
public class TempConfig {
    public static int SDK_MODE = 0;

    public static int setSdkMode(int sdkMode) {
        switch (sdkMode) {
            case SdkMode.MODE_NONE:
                SDK_MODE = SdkMode.MODE_NONE;
                break;

            case SdkMode.MODE_BLE:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    SDK_MODE = SdkMode.MODE_CLASSIC;
                } else {
                    SDK_MODE = SdkMode.MODE_BLE;
                }
                break;

            case SdkMode.MODE_CLASSIC:
                SDK_MODE = SdkMode.MODE_CLASSIC;

                break;
        }
        return SDK_MODE;
    }

    public class SdkMode {
        public final static int MODE_NONE = 0;
        public final static int MODE_BLE = 1;
        public final static int MODE_CLASSIC = 2;
    }
}
