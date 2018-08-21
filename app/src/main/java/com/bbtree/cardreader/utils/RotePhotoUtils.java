package com.bbtree.cardreader.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.text.TextUtils;

import com.bbtree.cardreader.camera.utils.YUVUtils;

import java.io.ByteArrayOutputStream;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/08/12
 * Create Time: 下午7:36
 */
public class RotePhotoUtils {
    private static RotePhotoUtils ourInstance;

    private static Integer angle;
    private int targetSizeWidth;
    private int targetSizeHeight;
    private int cameraPreviewFormat;

    private RotePhotoUtils() {
    }

    public static RotePhotoUtils getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new RotePhotoUtils();
            angle = DeviceConfigUtils.getConfig().getCameraSnapshotAngle();
        }
        return ourInstance;
    }

    public void setParams(int targetSizeWidth, int targetSizeHeight, int cameraPreviewFormat) {
        this.targetSizeWidth = targetSizeWidth;
        this.targetSizeHeight = targetSizeHeight;
        this.cameraPreviewFormat = cameraPreviewFormat;
    }

    /**
     * 竖屏机器物理摄像头多旋转了90
     *
     * @param nowFrame
     * @return
     */
    public byte[] roteYUV(byte[] nowFrame, Context mContext) {
        if (targetSizeWidth == 0 && targetSizeHeight == 0) {
            return nowFrame;
        }
        int width = targetSizeWidth;
        int height = targetSizeHeight;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (nowFrame == null) {
            return null;
        }
        byte[] finalFrame = null;

        if (angle != null) {
            switch (angle) {
                case 0:
                    finalFrame = nowFrame;
                    break;
                case 90:
                    finalFrame = YUVUtils.rotateYUV420Degree90(nowFrame, width, height);
                    break;
                case 180:
                    finalFrame = YUVUtils.rotateYUV420Degree180(nowFrame, width, height);
                    break;
                case 270:
                    finalFrame = YUVUtils.rotateYUV420Degree270(nowFrame, width, height);
                    break;
            }
        } else {
            switch (ScreenUtils.getOrientation(mContext)) {
                case Configuration.ORIENTATION_LANDSCAPE:
                    finalFrame = nowFrame;
                    break;
                case Configuration.ORIENTATION_PORTRAIT:
                    if (TextUtils.equals(ReadPhoneInfo.getPhoneModel(), "CS3Plus")
                            || TextUtils.equals(ReadPhoneInfo.getPhoneModel(), "CS3C")) {
                        finalFrame = YUVUtils.rotateYUV420Degree180(nowFrame, width, height);
                    } else {
                        finalFrame = nowFrame;
                    }
                    break;
            }
        }

//        byte[] finalFrame = nowFrame;
        YuvImage yuvImage = new YuvImage(finalFrame, cameraPreviewFormat, width, height, null);

        // 压缩照片质量
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 50, out);
        byte[] imageBytes = out.toByteArray();
        return imageBytes;
    }
}
