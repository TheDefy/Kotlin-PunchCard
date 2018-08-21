package com.bbtree.cardreader.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import com.bbtree.cardreader.R;
import com.bbtree.cardreader.entity.requestEntity.UpdateInfo;
import com.orhanobut.logger.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static com.bbtree.cardreader.utils.PackageUtil.compareFile;

/**
 * Created by qiujj on 2017/4/21.
 */
public class AppDownloadTask extends AsyncTask<String, Integer, File> {

    private static final String TAG = "AppDownloadTask";
    private Context mContext;
    private int fileLength;

    private boolean isDowning;

    private static int NOTIFICATION_ID = R.string.app_name;
    private UpdateInfo.Update mUpdateInfoResult;

    public AppDownloadTask(Context context, UpdateInfo.Update updateInfoResult) {
        mContext = context;
        this.mUpdateInfoResult = updateInfoResult;
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "CardReader.apk";
        mUpdateInfoResult.setLocalPath(path);
    }

    public boolean isDown() {
        return isDowning;
    }

    @Override
    protected File doInBackground(String... params) {
        // use URLConnection
        isDowning = true;
        try {

            if (PackageUtil.compareFile(mUpdateInfoResult.getDownloadUrl(), mUpdateInfoResult.getMd5Sum())) {
                Logger.d("the file has downloaded just install it!");
                boolean b = PackageUtil.installAPK(mContext,mUpdateInfoResult.getDownloadUrl());
                if(b){
                    Logger.d(TAG, "安装成功！！！");
                    ShellUtils.execCommand("reboot", true);
                    return null;
                }else{
                    Logger.e(TAG,"安装失败！！！");
                    return null;
                }
            } else {
                new File(mUpdateInfoResult.getDownloadUrl()).delete();
            }

            URL url = new URL(mUpdateInfoResult.getDownloadUrl());
            URLConnection connection = url.openConnection();
            connection.connect();

            fileLength = connection.getContentLength();
            Logger.d("apk length=" + fileLength);
            InputStream inputStream = new BufferedInputStream(url.openStream());
            File file = new File(mUpdateInfoResult.getLocalPath());
            OutputStream outputStream = new FileOutputStream(file);

            byte[] data = new byte[1024];
            long total = 0;
            int count;
            while ((count = inputStream.read(data)) != -1) {
                total += count;
                // publishing the progress....
                publishProgress((int) (total * 100 / fileLength));
                outputStream.write(data, 0, count);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return file;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            isDowning = false;
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        int progress = values[0];
        if (progress >= 0) {
            Logger.d("应用下载中，已完成...%d", progress);
        }

    }


    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);

        if (file != null) {
            if (compareFile(mUpdateInfoResult.getLocalPath(), mUpdateInfoResult.getMd5Sum())) {
                Logger.d("the file is download success,it will be install!");
                installApk(mUpdateInfoResult.getLocalPath());
            } else {
                new File(mUpdateInfoResult.getLocalPath()).delete();//删除文件
                Logger.e("wtf! the md5 sum of this file doesn't match the server?!");
            }
        } else {
            Toast.makeText(mContext.getApplicationContext(),R.string.download_apk_file_fail, Toast.LENGTH_SHORT).show();
        }

    }

    private void installApk(final String absPath) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean installSuccess = PackageUtil.installAPK(mContext,absPath);
                if (installSuccess) {
                    Logger.e(TAG,"安装成功-----~~");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ShellUtils.execCommand("reboot", true);
                        }
                    },2000);
                } else {
                    Logger.e(TAG,"安装失败！！！");
                }
            }
        },2000);
    }

}
