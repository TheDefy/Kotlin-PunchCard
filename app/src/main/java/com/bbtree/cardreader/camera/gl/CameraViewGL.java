package com.bbtree.cardreader.camera.gl;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.camera.CameraCfg;
import com.bbtree.cardreader.camera.utils.CameraUtils;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.entity.eventbus.CameraDegressEvent;
import com.bbtree.cardreader.utils.DeviceConfigUtils;
import com.bbtree.cardreader.utils.SPUtils;
import com.bbtree.cardreader.utils.ScreenUtils;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/09/21
 * Create Time: 下午6:33
 */
@SuppressWarnings("deprecation")
public class CameraViewGL extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener, Camera.PreviewCallback {
    private static final String TAG = CameraViewGL.class.getSimpleName();
    private final float[] mSTMatrix = new float[16];
    private Camera.Size targetSize;
    private int cameraSelection = Camera.CameraInfo.CAMERA_FACING_FRONT;//前后摄像头标记
    private Camera mCamera;//物理摄像头
    private boolean isRunning;
    private SurfaceTexture mSurfaceTexture;
    private int mTextureId;
    private FullFrameRect mFullFrameRect;
    private byte[] nowFrame;
    private boolean isPrivewing = false;

    public CameraViewGL(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraViewGL(Context context) {
        super(context);
        init();
    }

    public Camera.Size getTargetSize() {
        return targetSize;
    }

    public Camera getmCamera() {
        return mCamera;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int cameraPreviewFormat() {
        if (mCamera == null) {
            return ImageFormat.UNKNOWN;
        }
        int format = ImageFormat.UNKNOWN;
        try {
            format = mCamera.getParameters().getPreviewFormat();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return format;
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        EventBus.getDefault().register(this);

        setEGLContextClientVersion(2);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        init(cameraSelection);
    }

    private void init(int cameraSelection) {
        if (Build.MODEL.equals(Constant.PlatformAdapter.Cobabys_Z2)
                || Build.MODEL.equals(Constant.PlatformAdapter.Cobabys_M2)) {
            //麻辣隔壁的狗币厂商，写的狗币不标准API，耽误哥哥两天时间
            mCamera = Camera.open();
        } else {
            mCamera = getCamera(cameraSelection);
        }

        if (mCamera == null) {
            previewError();
            return;
        }
        try {
            targetSize = CameraUtils.targetSize(mCamera);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (targetSize == null) {
            previewError();
            return;
        }

        Logger.i("Setting imageWidth: " + targetSize.width + " imageHeight: "
                + targetSize.height + " frameRate: " + CameraCfg.VideoFrameRate);
        Camera.Parameters camParams = mCamera.getParameters();
        camParams.setPreviewSize(targetSize.width, targetSize.height);

        Logger.i("Preview FrameRate original: " + camParams.getPreviewFrameRate());

        //camParams.setPreviewFrameRate(CameraCfg.VideoFrameRate);
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


        // 如果有手动设置过摄像头角度，直接设置手动旋转的
        if (BBTreeApp.getApp().getDegrees() != -1 && BBTreeApp.getApp().getDegrees() != -2) {
            mCamera.setDisplayOrientation(BBTreeApp.getApp().getDegrees());
            return;
        }

//        mCamera.setDisplayOrientation(90);//竖屏机器物理摄像头多旋转了90
        //以下代码为适配考勤机而用，不作为通用配置
        if (null == DeviceConfigUtils.getConfig()) {
            return;
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CameraDegressEvent event) {
        mCamera.setDisplayOrientation(event.getDegress());
    }

    public boolean getPreviewing() {
//        return mCamera.previewEnabled();
        return isPrivewing;
    }


    private Camera getCamera(int cameraSelection) {
        Camera cameraDevice;//相机设备
        boolean multiDevice = CameraUtils.mutiCameraDevice();
        if (multiDevice) {
            cameraSelection = (cameraSelection == Camera.CameraInfo.CAMERA_FACING_FRONT) ?
                    Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
            try {
                cameraDevice = Camera.open(cameraSelection);
                SPUtils.setFrontCamera(cameraSelection == Camera.CameraInfo.CAMERA_FACING_FRONT);
            } catch (Exception e) {
//                e.printStackTrace();
                cameraDevice = null;
            }
        } else {
            try {
                cameraDevice = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                SPUtils.setFrontCamera(true);
            } catch (Exception e) {
//                e.printStackTrace();
                cameraDevice = null;
            }
            try {
                if (cameraDevice == null) {
                    cameraDevice = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                    SPUtils.setFrontCamera(false);
                }
            } catch (Exception e) {
//                e.printStackTrace();
                cameraDevice = null;
            }
        }
        return cameraDevice;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mFullFrameRect = new FullFrameRect(
                new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));
        mTextureId = mFullFrameRect.createTextureObject();

//        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        // start(cameraSelection);
        if (mCamera == null) {
            init(cameraSelection);
        }
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        isRunning = true;
        //setFilter(CameraFilter.FILTER_BLACK_WHITE);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mFullFrameRect.drawFrame(mTextureId, mSTMatrix);
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
    public void onPreviewFrame(byte[] data, Camera camera) {
        nowFrame = data;
        if (mCamera != null) {
            mCamera.addCallbackBuffer(data);
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }

    /**
     * 开启预览
     */
    public void startPreview() {
        if (isPrivewing) {
            return;
        }
        try {
            if (mCamera != null) {
                int bufSize = targetSize.width * targetSize.height * ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat()) / 8;
                for (int i = 0; i < 4; i++) {
                    mCamera.addCallbackBuffer(new byte[bufSize]);
                }
                mCamera.setPreviewCallbackWithBuffer(this);
                mCamera.startPreview();
                isPrivewing = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭预览
     */
    public void stopPreview() {
        isPrivewing = false;
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
        EventBus.getDefault().unregister(this);
    }

    /**
     * 设置不同的滤镜效果
     *
     * @param filter
     */
    public void setFilter(CameraFilter filter) {
        Texture2dProgram.ProgramType programType;
        float[] kernel = null;
        float colorAdj = 0.0f;

        int ordinal = filter.ordinal();
        if (ordinal == CameraFilter.FILTER_NONE.ordinal()) {
            programType = Texture2dProgram.ProgramType.TEXTURE_EXT;
        } else if (ordinal == CameraFilter.FILTER_BLACK_WHITE.ordinal()) {
            programType = Texture2dProgram.ProgramType.TEXTURE_EXT_BW;
        } else if (ordinal == CameraFilter.FILTER_BLUR.ordinal()) {
            programType = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT;
            kernel = new float[]{
                    1f / 16f, 2f / 16f, 1f / 16f,
                    2f / 16f, 4f / 16f, 2f / 16f,
                    1f / 16f, 2f / 16f, 1f / 16f};
        } else if (ordinal == CameraFilter.FILTER_SHARPEN.ordinal()) {
            programType = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT;
            kernel = new float[]{
                    0f, -1f, 0f,
                    -1f, 5f, -1f,
                    0f, -1f, 0f};
        } else if (ordinal == CameraFilter.FILTER_EDGE_DETECT.ordinal()) {
            programType = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT;
            kernel = new float[]{
                    -1f, -1f, -1f,
                    -1f, 8f, -1f,
                    -1f, -1f, -1f};
        } else if (ordinal == CameraFilter.FILTER_EMBOSS.ordinal()) {
            programType = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT;
            kernel = new float[]{
                    2f, 0f, 0f,
                    0f, -1f, 0f,
                    0f, 0f, -1f};
            colorAdj = 0.5f;
        } else {
            return;
        }

        if (programType != mFullFrameRect.getProgram().getProgramType()) {
            mFullFrameRect.changeProgram(new Texture2dProgram(programType));
            // If we created a new program, we need to initialize the texture width/height.
//            mIncomingSizeUpdated = true;
        }

        // Update the filter kernel (if any).
        if (kernel != null) {
            mFullFrameRect.getProgram().setKernel(kernel, colorAdj);
        }

    }

    public enum CameraFilter {
        FILTER_NONE, FILTER_BLACK_WHITE, FILTER_BLUR,
        FILTER_SHARPEN, FILTER_EDGE_DETECT, FILTER_EMBOSS
    }

    private void previewError() {
        setVisibility(INVISIBLE);

        //RecordEvent.record("NoCamera", getContext());

    }
}
