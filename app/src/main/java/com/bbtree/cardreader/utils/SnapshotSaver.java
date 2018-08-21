package com.bbtree.cardreader.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.bbtree.baselib.utils.BitmapUtils;
import com.bbtree.cardreader.entity.eventbus.SwipeCardInfo;
import com.bbtree.cardreader.model.CardRecordModule;
import com.orhanobut.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zhouyl on 01/04/2017.
 */

public class SnapshotSaver {
    private String TAG = "SnapshotSaver";
    private static final long CheckMemoryInterval = 30000;//存储容量检查最短间隔时间
    private final String FileStart = "zhs_card_";
    private final String FileEnd = ".jpg";
    private final String DirName = "pic";
    private long lastCheckTime = 0;//存储容量检查时间
    private static SnapshotSaver mInstance;
    //    private Map<String,SoftReference<Bitmap>> mSoftReferenceMap = new HashMap<>();
    private SwipeCardInfo mSwipeCardInfo;
    private static final int maxFileSize = 100;

    public static SnapshotSaver getInstance() {
        if (mInstance == null) {
            synchronized (SnapshotSaver.class) {
                if (mInstance == null) {
                    mInstance = new SnapshotSaver();
                }
            }
        }
        return mInstance;
    }

    public String save(Context ctx, byte[] imageBytes, SwipeCardInfo swipeCardInfo) {
        File picDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        this.mSwipeCardInfo = swipeCardInfo;
        if (picDir != null) {
            return computeStorageRuleInMemory(ctx, picDir, imageBytes);
        } else {
            return saveInternalStorage(ctx, imageBytes);
        }
    }

    /**
     * 保存在内部存储
     *
     * @param cxt
     * @return
     */
    private String saveInternalStorage(Context cxt, byte[] imageBytes) {
        String dirStr = cxt.getFilesDir().getAbsolutePath() + File.separator + DirName;
        File dir = new File(dirStr);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return computeStorageRuleInMemory(cxt, dir, imageBytes);
    }

    /**
     * SD卡储存
     *
     * @param cxt
     * @param dir
     * @param imageBytes
     * @return
     */
    private String computeStorageRuleInMemory(Context cxt, final File dir, byte[] imageBytes) {
        String path = dir.getAbsolutePath();
        Logger.i("picture url of local:" + path);
        Logger.d("memory :" + (System.currentTimeMillis() - lastCheckTime) + "ms");
        if ((System.currentTimeMillis() - lastCheckTime) > CheckMemoryInterval) {//达到30s以上才会再次检测容量
            long availableExternalMemorySize = ReadPhoneInfo.getAvailableExternalMemorySize();
            long availableSize = availableExternalMemorySize / 1024 / 1024;
            Logger.d("memory available external memory size:" + availableSize + "MB");
            if (availableSize < 500) {
                CardRecordModule.getInstance().deleteImgwithRow();
            }
            lastCheckTime = System.currentTimeMillis();
        }
        String filePath = path + File.separator + FileStart + System.currentTimeMillis() + FileEnd;
        return saveFile(filePath, imageBytes);
    }

    /**
     * 保存文件
     *
     * @param absPath
     * @return
     */
    private String saveFile(final String absPath, byte[] imageBytes) {
//        mSoftReferenceMap.put(absPath,new SoftReference<>(BitmapUtils.Bytes2Bitmap(imageBytes)));
//        int degrees = BBTreeApp.getApp().getDegrees();
        if (imageBytes == null || imageBytes.length < 0) return "";
        int degrees = mSwipeCardInfo.getDegrees();
        // 需要旋转
        if (degrees != 0) {
            if (SPUtils.isFrontCamera() && (degrees == 90 || degrees == 270)) {
                degrees += 180;
            }
            Bitmap resultBitmap = BitmapUtils.rotateBitmapAngle(BitmapUtils.Bytes2Bitmap(imageBytes), degrees);
            if (resultBitmap == null) {
                return null;
            }
            imageBytes = compressBitmap(resultBitmap);
            Logger.d("resultData:" + imageBytes.length);
            resultBitmap.recycle();
        } else {
            imageBytes = compressBitmap(BitmapUtils.Bytes2Bitmap(imageBytes));
        }
//        mSoftReferenceMap.remove(absPath);
        return writeBitmap(absPath, imageBytes);
    }

    @NonNull
    private String writeBitmap(String absPath, byte[] resultData) {
        Logger.d("resultData:" + resultData.length);
        File file = new File(absPath);
        FileOutputStream fop = null;
        if (!file.exists()) {
            try {
                file.createNewFile();
                fop = new FileOutputStream(file);
                fop.write(resultData);
                fop.flush();
                fop.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fop != null) {
                        fop.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return absPath;
    }

    public byte[] compressBitmap(Bitmap bitmap) {
        //进行有损压缩
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
        int baosLength = baos.toByteArray().length;
        Logger.d("baosLength:" + baosLength);
        while (baosLength / 1024 > maxFileSize) {
            baos.reset();
            options = Math.max(0, options - 10);
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            baosLength = baos.toByteArray().length;
            Logger.d("baosLength:" + baosLength);
            if (options == 0)
                break;
        }
        return baos.toByteArray();
    }

    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
            System.gc();
        }
    }

}
