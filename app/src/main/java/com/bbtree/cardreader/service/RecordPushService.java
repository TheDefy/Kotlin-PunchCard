package com.bbtree.cardreader.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.bbtree.baselib.base.AppExecutors;
import com.bbtree.baselib.rxjava.RxUtils;
import com.bbtree.cardreader.DataTransfer;
import com.bbtree.cardreader.config.Config;
import com.bbtree.cardreader.entity.TempReportData;
import com.bbtree.cardreader.entity.dao.CardRecord;
import com.bbtree.cardreader.entity.eventbus.SwipeCardInfo;
import com.bbtree.cardreader.model.CardRecordModule;
import com.bbtree.cardreader.model.TempRecordModule;
import com.bbtree.cardreader.utils.RotePhotoUtils;
import com.bbtree.cardreader.utils.SPUtils;
import com.bbtree.cardreader.utils.SnapshotSaver;
import com.orhanobut.logger.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/08/12
 * Create Time: 下午2:53
 */
public class RecordPushService extends Service {

    private static final String TAG = RecordPushService.class.getSimpleName();

    static public LinkedBlockingQueue<SwipeCardInfo> queue = new LinkedBlockingQueue<>(1024);
    //add by baodian
    static public LinkedBlockingQueue<SwipeCardInfo> upload_pic_queue = new LinkedBlockingQueue<>(1024);
    //add by baodian
    Map<String, String> picmap = new HashMap<String, String>();
    static public LinkedBlockingQueue<SwipeCardInfo> save_pic_queue = new LinkedBlockingQueue<>(1024);

    //add by baodian


    CompositeDisposable compositeDisposable = new CompositeDisposable();

    public void doFailTask() {

        Log.e("RecordPushService Fail", "begin");
        if (compositeDisposable != null) {
            compositeDisposable.clear();

            Disposable recordWithImgDisposable = CardRecordModule.getInstance().uploadFailRecordWithImg();
            Disposable uploadFailRecordDisposable = CardRecordModule.getInstance().uploadFailRecord();
            Disposable failTempRecordDisposable = TempRecordModule.getInstance().uploadFailTempRecord();//失败温度数据
            compositeDisposable.add(recordWithImgDisposable);
            compositeDisposable.add(failTempRecordDisposable);
            compositeDisposable.add(uploadFailRecordDisposable);
        }
    }


    //resolve mult process


    private LinkedBlockingQueue<TempReportData> tempQueue = new LinkedBlockingQueue<>();

    static public Map<String, String> map = new HashMap<String, String>();

    private DataTransfer.Stub stub = new DataTransfer.Stub() {
        @Override
        public void setCameraInfo(int width, int height, int cameraPreviewFormat) throws RemoteException {
            RotePhotoUtils.getInstance(RecordPushService.this).setParams(width, height, cameraPreviewFormat);
        }

        @Override
        public void transferCardRecord(SwipeCardInfo swipeCardInfo) throws RemoteException {
            try {
                //add by baodian
                CardRecordModule.getInstance().insertRecord(swipeCardInfo.getCardRecord());

                queue.put(swipeCardInfo);
                //add by baodian
                save_pic_queue.add(swipeCardInfo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void transferTempRecord(TempReportData tempReportData) throws RemoteException {
            try {
                tempQueue.put(tempReportData);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String get_card_holder(String id) throws RemoteException {

            Log.e("push", id + " to " + map.get(id) + "count:" + map.size());
            return map.get(id);
        }

        @Override
        public Map get_card_map() throws RemoteException {
            return map;
        }

        @Override
        public void startTask() throws RemoteException {
            doFailTask();
        }

        @Override
        public void overTask() throws RemoteException {
            if (compositeDisposable != null) {
                compositeDisposable.clear();
            }
        }


    };

    @Override
    public void onCreate() {
        super.onCreate();

        if (map == null) {
            map = new HashMap<String, String>();
        }
        RxUtils.setSecretKey(SPUtils.getSecretKey(null));
        int core = Runtime.getRuntime().availableProcessors();
        if (core <= 2) {//小于等于双核的，走单线程
            core = 1;
        }
        SaverRunnable[] saverWorker = new SaverRunnable[core];
        TempPushRunnable[] tempReportWorker = new TempPushRunnable[1];
        ExecutorService saverThreadPool = Executors.newFixedThreadPool(/*saverWorker.length + */tempReportWorker.length);
        /*
        for (SaverRunnable worker : saverWorker) {
            worker = new SaverRunnable();
            saverThreadPool.execute(worker);
        }
        */
        for (TempPushRunnable worker : tempReportWorker) {
            worker = new TempPushRunnable();
            saverThreadPool.execute(worker);
        }

        ExecutorService saverThreadPool2 = Executors.newFixedThreadPool(saverWorker.length);
/*
        SaverRunnable worker = new SaverRunnable();
        saverThreadPool2.execute(worker);
*/

        for (SaverRunnable worker : saverWorker) {
            worker = new SaverRunnable();
            saverThreadPool2.execute(worker);
        }

        UploadRunnable uploadworker = new UploadRunnable();
        ExecutorService saverThreadPool3 = Executors.newFixedThreadPool(1);
        saverThreadPool3.execute(uploadworker);

        SavePicRunnable savepic = new SavePicRunnable();
        ExecutorService saverThreadPool4 = Executors.newFixedThreadPool(1);
        saverThreadPool4.execute(savepic);


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    //    ArrayList<Long> useList = new ArrayList<>();
    private class SavePicRunnable implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if (save_pic_queue == null) {
                    return;
                }

                try {

                    final SwipeCardInfo swipeCardInfo = save_pic_queue.take();

                    synchronized (picmap) {
                        String filePath = picmap.get(swipeCardInfo.getCardRecord().getId());

                        if (filePath == null) {

                            if (swipeCardInfo.getPhotoByte() != null) {
                                byte[] imageBytes = RotePhotoUtils.getInstance(RecordPushService.this).roteYUV(swipeCardInfo.getPhotoByte(), RecordPushService.this);
                                filePath = SnapshotSaver.getInstance().save(RecordPushService.this, imageBytes, swipeCardInfo);
                                Logger.t(TAG).i(">>>>>>savePhotoFile>>>>>>:" + filePath);

                                Log.e("SavePicRunnable", swipeCardInfo.getCardRecord().getId() + ":" + filePath);

                                if (TextUtils.isEmpty(filePath) == false) {
                                    synchronized (map) {
                                        map.put(swipeCardInfo.getCardRecord().getId(), filePath);
                                    }
                                    picmap.put(swipeCardInfo.getCardRecord().getId(), filePath);

                                    swipeCardInfo.getCardRecord().setCard_holder(filePath);
                                    swipeCardInfo.getCardRecord().setHas_upload(false);
                                } else {
                                    save_pic_queue.offer(swipeCardInfo);
                                    continue;
                                }
                            } else {
                                swipeCardInfo.getCardRecord().setCard_holder(Config.NO_IMG);
                                swipeCardInfo.getCardRecord().setHas_upload(true);
                            }

                            CardRecordModule.getInstance().updateRecord(swipeCardInfo.getCardRecord());

                        }
                    }

                } catch (Exception e) {

                }


            }
        }


    }


    private class UploadRunnable implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if (upload_pic_queue == null) {
                    return;
                }

                try {
                    final SwipeCardInfo swipeCardInfo = upload_pic_queue.take();

                    String filePath = null;
                    synchronized (picmap) {
                        filePath = picmap.get(swipeCardInfo.getCardRecord().getId());

                        if (filePath == null) {

                            if (swipeCardInfo.getPhotoByte() != null) {
                                byte[] imageBytes = RotePhotoUtils.getInstance(RecordPushService.this).roteYUV(swipeCardInfo.getPhotoByte(), RecordPushService.this);
                                filePath = SnapshotSaver.getInstance().save(RecordPushService.this, imageBytes, swipeCardInfo);
                                Logger.t(TAG).i(">>>>>>savePhotoFile>>>>>>:" + filePath);

                                if (TextUtils.isEmpty(filePath) == false) {
                                    Log.e("UploadRunnable", swipeCardInfo.getCardRecord().getId() + ":" + filePath);
                                    synchronized (map) {
                                        map.put(swipeCardInfo.getCardRecord().getId(), filePath);
                                    }
                                    picmap.put(swipeCardInfo.getCardRecord().getId(), filePath);
                                    swipeCardInfo.getCardRecord().setCard_holder(filePath);
                                } else {
                                    save_pic_queue.offer(swipeCardInfo);
                                    continue;
                                }


                            } else {
                                swipeCardInfo.getCardRecord().setCard_holder(Config.NO_IMG);
                                swipeCardInfo.getCardRecord().setHas_upload(true);
                            }

                        } else {
                            swipeCardInfo.getCardRecord().setCard_holder(filePath);
                        }

                        CardRecordModule.getInstance().updateRecord(swipeCardInfo.getCardRecord());
                    }


                    //CardRecordModule.getInstance().saveAndReportRecordWithImg(swipeCardInfo.getCardRecord());
                    if (filePath != null) {
                        //upload_pic_queue.add(swipeCardInfo);
                        CardRecordModule.getInstance().reportOnlyRecordWhitImg(swipeCardInfo.getCardRecord());
                    }

                } catch (Exception e) {

                }
            }
        }

    }


    private class SaverRunnable implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if (queue == null) {
                    return;
                }
                try {
                    final SwipeCardInfo swipeCardInfo = queue.take();
                    //add by baodian
                    Observable<CardRecord> cardRecordObservable = CardRecordModule.getInstance().uploadCardRecords(Arrays.asList(swipeCardInfo.getCardRecord()));

                    cardRecordObservable
                            .subscribeOn(Schedulers.from(AppExecutors.getInstance().networkIO()))
                            .subscribe(new Observer<CardRecord>() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {

                                }

                                @Override
                                public void onNext(CardRecord value) {
                                    Log.e("upload_only_record", "success");
                                    swipeCardInfo.setCardRecord(value);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e("upload_only_record", "failed");
                                    Logger.t("error").e(e.getMessage());
                                }

                                @Override
                                public void onComplete() {
                                    Log.e("upload_only_record", "begin_pic");
                                    upload_pic_queue.add(swipeCardInfo);


/*
                                    String filePath = null;
                                    if (swipeCardInfo.getPhotoByte() != null) {
                                        byte[] imageBytes = RotePhotoUtils.getInstance(RecordPushService.this).roteYUV(swipeCardInfo.getPhotoByte(), RecordPushService.this);
                                        filePath = SnapshotSaver.getInstance().save(RecordPushService.this, imageBytes, swipeCardInfo);
                                        Logger.t(TAG).i(">>>>>>savePhotoFile>>>>>>:" + filePath);

                                        if( map == null )
                                        {
                                            map = new HashMap<String,String>();
                                        }

                                        Log.e("eeeeeeeeeeeeeeeeee",swipeCardInfo.getCardRecord().getId()+":"+filePath);
                                        map.put(swipeCardInfo.getCardRecord().getId(),filePath);
                                        swipeCardInfo.getCardRecord().setCard_holder(filePath);
                                    } else {
                                        swipeCardInfo.getCardRecord().setCard_holder(Config.NO_IMG);
                                    }

                                    CardRecordModule.getInstance().updateRecord(swipeCardInfo.getCardRecord());

                                    //CardRecordModule.getInstance().saveAndReportRecordWithImg(swipeCardInfo.getCardRecord());
                                    if( filePath != null )
                                    {
                                        upload_pic_queue.add(swipeCardInfo);
                                    }
*/
                                }
                            });
/*
                    String filePath = null;
                    if (swipeCardInfo.getPhotoByte() != null) {
                        byte[] imageBytes = RotePhotoUtils.getInstance(RecordPushService.this).roteYUV(swipeCardInfo.getPhotoByte(), RecordPushService.this);
                        filePath = SnapshotSaver.getInstance().save(RecordPushService.this, imageBytes, swipeCardInfo);
                        Logger.t(TAG).i(">>>>>>savePhotoFile>>>>>>:" + filePath);

                        if( map == null )
                        {
                            map = new HashMap<String,String>();
                        }

                        Log.e("eeeeeeeeeeeeeeeeee",swipeCardInfo.getCardRecord().getId()+":"+filePath);
                        map.put(swipeCardInfo.getCardRecord().getId(),filePath);
                        swipeCardInfo.getCardRecord().setCard_holder(filePath);
                    } else {
                        swipeCardInfo.getCardRecord().setCard_holder(Config.NO_IMG);
                    }
                    //保存照片信息到数据库，并上传照片，之后再次上报Ø

//                    useList.add(System.currentTimeMillis());
//                    Log.e("useList", useList.size() + "");

//                    CardRecordModule.getInstance().insertRecord(swipeCardInfo.getCardRecord());
                    //CardRecordModule.getInstance().insertRecord(swipeCardInfo.getCardRecord());

                    CardRecordModule.getInstance().updateRecord(swipeCardInfo.getCardRecord());

                    CardRecordModule.getInstance().saveAndReportRecordWithImg(swipeCardInfo.getCardRecord());

 */


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class TempPushRunnable implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if (tempQueue == null) {
                    return;
                }
                try {
                    TempReportData reportData = tempQueue.take();
                    if (reportData != null) {
//                        // TODO test
//                        CardInfo cardInfoNode = reportData.getCardInfoNode();
//                        cardInfoNode.setFamily(null);
//                        cardInfoNode.setFamilyString(null);
//                        reportData.setCardInfoNode(cardInfoNode);
                        TempRecordModule.getInstance().uploadTemRecord(Arrays.asList(reportData));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}