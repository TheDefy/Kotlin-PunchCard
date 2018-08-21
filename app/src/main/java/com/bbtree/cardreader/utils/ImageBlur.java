package com.bbtree.cardreader.utils;

import android.graphics.Bitmap;


public class ImageBlur {
    static {
        System.loadLibrary("ImageBlur");
    }

    public static native void blurIntArray(int[] pImg, int w, int h, int r);

    public static native void blurBitMap(Bitmap bitmap, int r);
}
