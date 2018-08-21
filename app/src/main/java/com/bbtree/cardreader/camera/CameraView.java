package com.bbtree.cardreader.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.TextureView;

import com.bbtree.cardreader.camera.utils.CameraUtils;
import com.bbtree.cardreader.utils.DeviceConfigUtils;
import com.bbtree.cardreader.utils.ScreenUtils;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/05/28
 * Time: 下午1:58
 */
public class CameraView extends TextureView implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {

    private static final String TAG = CameraView.class.getSimpleName();
    private int cameraSelection = Camera.CameraInfo.CAMERA_FACING_FRONT;//前后摄像头标记
    private Camera.Size targetSize;
    private Camera mCamera;//物理摄像头

    private byte[] nowFrame;

    public CameraView(Context context) {
        super(context);
        init(cameraSelection);
    }

    public CameraView(Context context, int cameraSelection) {
        super(context);
        init(cameraSelection);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(cameraSelection);
    }

    public Camera.Size getTargetSize() {
        return targetSize;
    }

    public int cameraPreviewFormat() {
        if (mCamera == null) {
            return ImageFormat.UNKNOWN;
        }
        return mCamera.getParameters().getPreviewFormat();
    }

    private void init(int cameraSelection) {
        mCamera = getCamera(cameraSelection);
        if (mCamera == null) {
            setVisibility(INVISIBLE);
            return;
        }
        targetSize = CameraUtils.targetSize(mCamera);
        if (targetSize == null) {
            return;
        }

        Logger.i("Setting imageWidth: " + targetSize.width + " imageHeight: "
                + targetSize.height + " frameRate: " + CameraCfg.VideoFrameRate);
        Camera.Parameters camParams = mCamera.getParameters();
        camParams.setPreviewSize(targetSize.width, targetSize.height);

        Logger.i("Preview FrameRate original: " + camParams.getPreviewFrameRate());

        camParams.setPreviewFrameRate(CameraCfg.VideoFrameRate);
        Logger.i("Preview FrameRate real    : " + camParams.getPreviewFrameRate());
        //camParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        camParams.setRecordingHint(true);
        List<String> focusModes = camParams.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            camParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        try {
            mCamera.setParameters(camParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setSurfaceTextureListener(this);

//        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//        int rotation = display.getRotation();
//        switch (rotation) {
//            case Surface.ROTATION_0:
//                mCamera.setDisplayOrientation(90);
//                break;
//            case Surface.ROTATION_90:
//                mCamera.setDisplayOrientation(0);
//                break;
//            case Surface.ROTATION_180:
//                mCamera.setDisplayOrientation(270);
//                break;
//            case Surface.ROTATION_270:
//                mCamera.setDisplayOrientation(180);
//                break;
//        }

//        mCamera.setDisplayOrientation(90);//竖屏机器物理摄像头多旋转了90
        //以下代码为适配考勤机而用，不作为通用配置
        Integer angle = DeviceConfigUtils.getConfig().getCameraPreviewAngle();
        if (ScreenUtils.getOrientation(getContext()) != Configuration.ORIENTATION_LANDSCAPE) {
            if (angle == null) {
                if (TextUtils.equals(Build.MODEL, "CS3Plus")
                        || TextUtils.equals(Build.MODEL, "CS3C")) {
                    mCamera.setDisplayOrientation(180);//厂商硬件出厂太乱，此处做不同硬件适配
                } else {
                    mCamera.setDisplayOrientation(0);//厂商硬件出厂太乱，此处做不同硬件适配
                }
            } else {
                mCamera.setDisplayOrientation(angle);//此处等服务器返回
            }
        } else {
            if (angle == null) {
                mCamera.setDisplayOrientation(0);//横屏机器，默认应该90度的
            } else {
                mCamera.setDisplayOrientation(angle);//横屏机器物理摄像头多旋转了90。本来应该是90的，这里要写城180
            }

        }
    }

    private Camera getCamera(int cameraSelection) {
        Camera cameraDevice;//相机设备
        boolean multiDevice = CameraUtils.mutiCameraDevice();
        if (multiDevice) {
            cameraSelection = (cameraSelection == Camera.CameraInfo.CAMERA_FACING_FRONT) ?
                    Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
            cameraDevice = Camera.open(cameraSelection);
        } else {
            try {
                cameraDevice = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            } catch (Exception e) {
//                e.printStackTrace();
                cameraDevice = null;
            }
            try {
                if (cameraDevice == null) {
                    cameraDevice = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                }
            } catch (Exception e) {
//                e.printStackTrace();
                cameraDevice = null;
            }
        }
        return cameraDevice;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            if (mCamera == null) {
                init(cameraSelection);
            }
            mCamera.setPreviewTexture(surface);
            startPreview();
        } catch (IOException exception) {
            stopPreview();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {


    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        try {
            stopPreview();
        } catch (RuntimeException e) {
            // The camera has probably just been released, ignore.
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    /**
     * 获取当前帧，并转换成JPEG字节数据
     *
     * @return
     */
    public byte[] getNowFrame() {
        if (nowFrame == null || nowFrame.length < 1) {
            return null;
        }
        byte[] bytes = new byte[nowFrame.length];
        System.arraycopy(nowFrame, 0, bytes, 0, nowFrame.length);
        return bytes;
    }

    @Override
    public synchronized void onPreviewFrame(byte[] data, Camera camera) {
        nowFrame = data;
        if (mCamera != null) {
            mCamera.addCallbackBuffer(data);
        }
    }

    /**
     * 开启预览
     */
    public void startPreview() {
        try {
            if (mCamera != null) {
                int bufSize = targetSize.width * targetSize.height * ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat()) / 8;
                for (int i = 0; i < 4; i++) {
                    mCamera.addCallbackBuffer(new byte[bufSize]);
                }
                mCamera.setPreviewCallbackWithBuffer(this);
                mCamera.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭预览
     */
    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
        }
    }

    /**
     * 释放摄像头
     */
    public void release() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

}
