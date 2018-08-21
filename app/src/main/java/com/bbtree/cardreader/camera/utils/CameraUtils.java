package com.bbtree.cardreader.camera.utils;

import android.hardware.Camera;
import com.bbtree.cardreader.camera.CameraCfg;
import com.orhanobut.logger.Logger;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zhou on 14-7-31.
 */
@SuppressWarnings("deprecation")
public class CameraUtils {

    private static final String TAG = CameraUtils.class.getSimpleName();

    /**
     * 获取目标分辨率
     *
     * @param mCamera
     * @return
     */
    public static Camera.Size targetSize(Camera mCamera) {
        try {
            //获取摄像头的所有支持的分辨率
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> resolutionList = parameters.getSupportedPreviewSizes();
            if (resolutionList != null && resolutionList.size() > 0) {
                Collections.sort(resolutionList, new ResolutionComparator());
                Camera.Size previewSize = null;
                boolean hasSize = false;
                String targetSize = "\n";
                //如果摄像头支持640*480，那么强制设为640*480
                for (int i = 0; i < resolutionList.size(); i++) {
                    Camera.Size size = resolutionList.get(i);
                    if (size != null) {
                        targetSize += "Width:" + size.width + " * Height:" + size.height + "\n";
                    }
                    if (size != null && size.width == CameraCfg.CameraWidth && size.height == CameraCfg.CameraHeight) {
                        previewSize = size;
                        hasSize = true;
                        break;
                    }
                }
                Logger.i("this camera support:" + targetSize);
                //如果不支持设为中间的那个
                if (!hasSize) {
                    int mediumResolution = resolutionList.size() / 2;
                    if (mediumResolution >= resolutionList.size())
                        mediumResolution = resolutionList.size() - 1;
                    previewSize = resolutionList.get(mediumResolution);
                }
                //获取计算过的摄像头分辨率
                return previewSize;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断是否是多摄像头设备
     *
     * @return
     */
    public static boolean mutiCameraDevice() {
        boolean back = false;
        boolean front = false;
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                back = true;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                front = true;
            }
        }
        return back && front;
    }

    public static class ResolutionComparator implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size size1, Camera.Size size2) {
            if (size1.height != size2.height)
                return size1.height - size2.height;
            else
                return size1.width - size2.width;
        }
    }
}
