package com.bbtree.cardreader.model;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bbtree.baselib.base.AppExecutors;
import com.bbtree.baselib.net.AliOSSClient;
import com.bbtree.baselib.net.ResultObject;
import com.bbtree.baselib.rxjava.RxSchedulers;
import com.bbtree.baselib.rxjava.RxUtils;
import com.bbtree.baselib.utils.FileUtils;
import com.bbtree.baselib.utils.ListUtils;
import com.bbtree.baselib.utils.StringUtils;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.common.CloudStoragePath;
import com.bbtree.cardreader.common.Code;
import com.bbtree.cardreader.common.Urls;
import com.bbtree.cardreader.config.Config;
import com.bbtree.cardreader.contact.MainActivityContract;
import com.bbtree.cardreader.entity.dao.CardRecord;
import com.bbtree.cardreader.greendao.gen.CardRecordDao;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.rx.RxDao;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.bbtree.cardreader.service.RecordPushService.map;
import static com.bbtree.cardreader.service.RecordPushService.queue;
import static com.bbtree.cardreader.service.RecordPushService.save_pic_queue;
import static com.bbtree.cardreader.service.RecordPushService.upload_pic_queue;

/**
 * Created by zhouyl on 30/03/2017.
 */

public class CardRecordModule {

    private CardRecordDao cardRecordDao;
    static CardRecordModule cardRecordModule;
    RxDao<CardRecord, String> rxDao;
    private volatile static ReadWriteLock mLock = new ReentrantReadWriteLock(true);

    private AliOSSClient aliOSSClient;
    private static CompositeDisposable compositeDisposable;  // 用于处理退出广告页时候，定时任务执行。

    public static CardRecordModule getInstance() {
        if (cardRecordModule == null) {
            cardRecordModule = new CardRecordModule();
            compositeDisposable = new CompositeDisposable();

        }
        return cardRecordModule;
    }

    private CardRecordModule() {
        cardRecordDao = BBTreeApp.getApp().getDaoSessionInstance().getCardRecordDao();
        rxDao = cardRecordDao.rx();
        aliOSSClient = new AliOSSClient();

    }

    public void insertRecord(CardRecord cardRecord) {
        rxDao.insertOrReplace(cardRecord)
                .observeOn(Schedulers.from(AppExecutors.getInstance().diskIO()))
                .subscribe();
    }

    public void updateRecord(CardRecord cardRecord) {
        if (cardRecord == null) return;
        rxDao.insertOrReplace(cardRecord)
                .observeOn(Schedulers.from(AppExecutors.getInstance().diskIO()))
                .subscribe();

    }

    public void saveAndReportRecordWithImg(CardRecord cardRecord) {
        if (cardRecord == null) return;
        //插入数据库并上传打卡记录
//        cardRecordDao.insertOrReplaceInTx(cardRecord);

//        long l = System.currentTimeMillis();
//        cardRecordDao.insert(cardRecord);

//        insert(cardRecord);

//        Log.e("insert", String.valueOf(System.currentTimeMillis() - l));

        //上传单条记录
        reportRecordWhitImg(cardRecord);
    }


    public void reportRecordWhitImg(final CardRecord cardRecordWithImg) {

        Observable<CardRecord> uploadRecordWithImgObservable = uploadImg(cardRecordWithImg)
                .flatMap(new Function<CardRecord, ObservableSource<CardRecord>>() {
                    @Override
                    public ObservableSource<CardRecord> apply(CardRecord cardRecord) throws Exception {
                        return uploadCardRecords(Arrays.asList(cardRecord));
                    }
                });
        Observable<CardRecord> cardRecordObservable = uploadCardRecords(Arrays.asList(cardRecordWithImg));

        Observable.concat(uploadRecordWithImgObservable, cardRecordObservable)
                .first(cardRecordWithImg)
                .subscribeOn(Schedulers.from(AppExecutors.getInstance().networkIO()))
                .subscribe();
    }

    //add by baodian
    public void reportOnlyRecordWhitImg(final CardRecord cardRecordWithImg) {

        Observable<CardRecord> uploadRecordWithImgObservable = uploadImg(cardRecordWithImg)
                .flatMap(new Function<CardRecord, ObservableSource<CardRecord>>() {
                    @Override
                    public ObservableSource<CardRecord> apply(CardRecord cardRecord) throws Exception {
                        return uploadCardRecords(Arrays.asList(cardRecord));
                    }
                });
        //Observable<CardRecord> cardRecordObservable = uploadCardRecords(Arrays.asList(cardRecordWithImg));

        /*Observable.concat(uploadRecordWithImgObservable, cardRecordObservable)
                .first(cardRecordWithImg)*/
        uploadRecordWithImgObservable
                .subscribeOn(Schedulers.from(AppExecutors.getInstance().networkIO()))
                .subscribe(new Observer<CardRecord>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(CardRecord value) {
                        Log.e("upload_pic","success");

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("upload_pic","failed");
                        Logger.t("error").e(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.e("upload_pic","finish");

                    }
                });
    }



    /**
     * 图片失败上传卡记录
     *
     * @param cardRecords
     */
    private void failImgUploadCardRecord(List<CardRecord> cardRecords) {
        uploadCardRecords(cardRecords).subscribe(new DefaultObserver<CardRecord>() {
            @Override
            public void onNext(CardRecord value) {

            }

            @Override
            public void onError(Throwable e) {
                Logger.t("error").e(e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }


    public Observable<CardRecord> reportRecordWhitImg(List<CardRecord> cardRecordWithImg, final String... sn) {
        return uploadImg(cardRecordWithImg)
                .flatMap(new Function<CardRecord, ObservableSource<CardRecord>>() {
                    @Override
                    public ObservableSource<CardRecord> apply(CardRecord cardRecord) throws Exception {
                        return uploadCardRecords(Arrays.asList(cardRecord), sn);
                    }
                });
    }

    public Observable<CardRecord> reportRecord(List<CardRecord> cardRecordWithoutImg, String... sn) {
        return uploadCardRecords(cardRecordWithoutImg, sn);
    }


    /**
     * 批量上传图片，在重试过程中，处理了已经上传过的图片。
     *
     * @param cardRecordWithImg
     * @return
     */
    private Observable<CardRecord> uploadImg(List<CardRecord> cardRecordWithImg) {
        final Map<String, CardRecord> map = new HashMap<>();
        for (CardRecord cardRecord : cardRecordWithImg) {
            map.put(cardRecord.getId(), cardRecord);
        }
        return Observable.fromIterable(cardRecordWithImg)
                .filter(new Predicate<CardRecord>() {
                    @Override
                    public boolean test(CardRecord cardRecord) throws Exception {
                        if (map.get(cardRecord.getId()).getCloud_url() == null) {//在map里面
                            return true;
                        } else {
                            return false;
                        }
                    }
                })
                .flatMap(new Function<CardRecord, Observable<CardRecord>>() {
                    @Override
                    public Observable<CardRecord> apply(CardRecord cardRecord) throws Exception {
                        return uploadImg(cardRecord);
                    }
                })
                .map(new Function<CardRecord, CardRecord>() {
                    @Override
                    public CardRecord apply(CardRecord cardRecord) throws Exception {
                        if (cardRecord.getCloud_url() != null) {
                            map.put(cardRecord.getId(), cardRecord);
                        }
                        return cardRecord;
                    }
                })
                .retry(2);
    }

    /**
     * 上传图片到文件服务器，将图片url更新到卡记录。更新数据库
     */
    public Observable<CardRecord> uploadImg(final CardRecord cardRecord) {
        //change by baodian
        File file = null;//new File(TextUtils.isEmpty(cardRecord.getCard_holder()) ? "" : cardRecord.getCard_holder());
        if( TextUtils.isEmpty(cardRecord.getCard_holder()) == false && cardRecord.getCard_holder().equals(Config.NO_IMG) == false )
        {
            file = new File(cardRecord.getCard_holder());
        }

        if ( /*TextUtils.isEmpty(cardRecord.getCard_holder()) || TextUtils.equals(Config.NO_IMG, cardRecord.getCard_holder())*/file == null
                || !file.exists()) {
            cardRecord.setCloud_url(Config.NO_IMG);
            cardRecord.setHas_upload(true);
            //cardRecord.setHas_sync(false);
//            cardRecordDao.update(cardRecord);
//            update(cardRecord);
//            long l = System.currentTimeMillis();

            rxDao.update(cardRecord)
                    .observeOn(Schedulers.from(AppExecutors.getInstance().diskIO()))
                    .subscribe();
//            Log.e("update", String.valueOf(System.currentTimeMillis() - l));

            return Observable.just(cardRecord);
        }
        String serverPath = CloudStoragePath.getInstance().getServerPath() + file.getName();

        return Observable.concat(aliOSSClient.upload(serverPath, cardRecord.getCard_holder())
                , aliOSSClient.upload_huaNan(serverPath, cardRecord.getCard_holder()))
                .take(1)
                .map(new Function<String, CardRecord>() {
                    @Override
                    public CardRecord apply(String s) throws Exception {
                        if (StringUtils.isUrl(s)) {
                            Logger.i("picture url of alioss:" + s);
                            Log.d("88888","s:"+s);
                            cardRecord.setCloud_url(s);
                            cardRecord.setHas_upload(true);
                            cardRecord.setHas_sync(false);
//                            cardRecordDao.update(cardRecord);
//                            update(cardRecord);
                            rxDao.update(cardRecord).observeOn(Schedulers.from(AppExecutors.getInstance().diskIO())).subscribe();

                        }
                        return cardRecord;
                    }
                });
    }


    /**
     * 打卡记录上传到服务器
     *
     * @param cardRecords
     */
    // private change to public by baodian
    /*private*/public Observable<CardRecord> uploadCardRecords(final List<CardRecord> cardRecords, String... sn) {
        Map map = new HashMap();
        if (sn.length > 0) map.put("sn", sn[0]);
        map.put("cardsRecord", cardRecords);
        return RxUtils.postMap(Urls.RECORDREPORT, map)
                .map(RxUtils.getMap())
                ./*filter(new Predicate<ResultObject>() {
                    @Override
                    public boolean test(ResultObject resultObject) throws Exception {
                        return resultObject.getCode() == Code.SUCCESS;
                    }
                }).flatMap(new Function<ResultObject, ObservableSource<CardRecord>>() {
                    @Override
                    public ObservableSource<CardRecord> apply(ResultObject resultObject) throws Exception {
                        TempClass temp = new TempClass();
                        temp.resultObject = resultObject;
                        temp.cardRecord = cardRecords.get(0);

                        return Observable.fromIterable(cardRecords);
                    }
                })*/
                flatMap(new Function<ResultObject, ObservableSource<TempClass>>() {
                     @Override
                     public ObservableSource<TempClass> apply(ResultObject resultObject) throws Exception {
                        TempClass temp = new TempClass();
                        temp.resultObject = resultObject;
                         if(!ListUtils.isZero(cardRecords))
                        temp.cardRecord = cardRecords.get(0);

                        ArrayList<TempClass> classes = new ArrayList<TempClass>();
                        classes.add(temp);

                        return Observable.fromIterable(classes);
                    }
                })

//                .retry(2)
                .map(new Function<TempClass, CardRecord>() {
                    @Override
                    public CardRecord apply(TempClass temp) throws Exception {
                        if( temp.resultObject.getCode() == Code.SUCCESS ) {
                            Log.e("hahaha","success");
                            temp.cardRecord.setHas_sync(true);
                            rxDao.update(temp.cardRecord).observeOn(Schedulers.from(AppExecutors.getInstance().diskIO())).subscribe();
                        }
                        else
                        {
                            Log.e("hahaha","fail");
                        }

                        return temp.cardRecord;
                    }
                });
    }


    public void deleteImgwithRow() {
        Observable.fromIterable(querySuccessImg()).map(new Function<CardRecord, Object>() {
            @Override
            public Object apply(CardRecord cardRecord) throws Exception {
                String cardHolder = cardRecord.getCard_holder();
                File file = new File(cardHolder);
                if (file.exists()) {
                    try {
                        file.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (cardRecord.getHas_sync()) {
                    cardRecordDao.delete(cardRecord);
                }
                return "";
            }
        }).subscribeOn(Schedulers.io())
                .subscribe(new DefaultObserver<Object>() {
                    @Override
                    public void onNext(@NonNull Object o) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public List<CardRecord> querySuccessImg() {
        return cardRecordDao
                .queryBuilder()
                .where(CardRecordDao.Properties.Has_upload.eq(true), CardRecordDao.Properties.Has_sync.eq(true))
                //.limit(limit)
                //.offset(offset)
                .build()
                .list();
    }


    public void deleteAll() {
        cardRecordDao.deleteAll();
    }


    //private Map<String,String> mapCard;
    /*************************处理失败任务************************/
    private MainActivityContract.Presenter mPresenter;
    public void doFailTask(MainActivityContract.Presenter mPresenter) {
        this.mPresenter = mPresenter;
/*
        try {
            mapCard = mPresenter.getDataTransfer().get_card_map();
        }
        catch(Exception e)
        {

        }
*/
        Log.e("doFailTaskdoFailTask","begin");

        //if( queue.size() == 0 )
        {
            Disposable uploadFailRecordDisposable = CardRecordModule.getInstance().uploadFailRecord();
            compositeDisposable.add(uploadFailRecordDisposable);
        }

        //if( upload_pic_queue.size() == 0 && save_pic_queue.size() == 0 )
        {
            Disposable recordWithImgDisposable = CardRecordModule.getInstance().uploadFailRecordWithImg();
            compositeDisposable.add(recordWithImgDisposable);
        }

        Disposable failTempRecordDisposable = TempRecordModule.getInstance().uploadFailTempRecord();//失败温度数据
        compositeDisposable.add(failTempRecordDisposable);


    }

    public void clearTask() {
        if (compositeDisposable != null) compositeDisposable.clear();
    }


    public Disposable uploadFailRecord(final String... sn) {
        //long count = cardRecordDao.count();
        //final long times = count / Constant.PushCardRecord.MAX_SIZE_PER_REQUEST;
        //return Observable.interval(2, 5, TimeUnit.MINUTES)



        return Observable.interval(20, 30, TimeUnit.SECONDS)
                .map(new Function<Long, List<CardRecord>>() {
                    @Override
                    public List<CardRecord> apply(Long aLong) throws Exception {
                        //int t = (int) (aLong % (times + 3)); //当日预留失败次数
                        /*List<CardRecord> failSync = getFailUpload(Constant.PushCardRecord.MAX_SIZE_PER_REQUEST,
                                t * Constant.PushCardRecord.MAX_SIZE_PER_REQUEST);*/
                        if( queue.size() == 0 ) {

                            List<CardRecord> failSync = getFailUpload(200, 0);
                            return failSync;
                        }
                        else
                        {
                            return new ArrayList<CardRecord>();
                        }
                    }
                })
                .flatMap(new Function<List<CardRecord>, ObservableSource<CardRecord>>() {
                    @Override
                    public ObservableSource<CardRecord> apply(List<CardRecord> cardRecords) throws Exception {

                        for( CardRecord record : cardRecords )
                        {
                            //Log.e("change+",record.getId()+" to " + CardRecordModule.this.mPresenter.getDataTransfer().get_card_holder(record.getId()));
                            /*
                            if( mapCard != null ) {
                                Log.e("change+",record.getId()+" to " + mapCard.get(record.getId()));
                                record.setCard_holder(mapCard.get(record.getId()));
                            }
                            else*/
                            {
                                if( record.getCard_holder() == null )
                                {
                                    /*
                                    if( mPresenter != null && mPresenter.getDataTransfer() != null ) {
                                        String s = CardRecordModule.this.mPresenter.getDataTransfer().get_card_holder(record.getId());
                                        Log.e("change+CardRecordModule", record.getId() + " to " + s);
                                        record.setCard_holder(s);
                                    }
                                    */
                                    String s = null;
                                    synchronized(map) {
                                        s = map.get(record.getId());
                                    }
                                    Log.e("change+CardRecordModule", record.getId() + " to " + s);
                                    record.setCard_holder(s);


                                }
                                else {
                                    Log.e("change+", record.getId() + " to " + record.getCard_holder());
                                }
                            }
                        }


                        Observable<CardRecord> cardRecordObservable = reportRecord(cardRecords, sn);
                        Logger.e("mapperisNull?", cardRecordObservable);
                        return cardRecordObservable;
                    }
                })

                .compose(RxSchedulers.<CardRecord>applyObservableAsync())
                .subscribeWith(new DisposableObserver<CardRecord>() {
                    @Override
                    public void onNext(@NonNull CardRecord cardRecord) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    public Disposable uploadFailRecordWithImg(final String... sn) {
        //long count = cardRecordDao.count();
        //final long times = count / Constant.PushCardRecord.MAX_SIZE_PER_IMG_REQUEST;


        //return Observable.interval(0, 5, TimeUnit.MINUTES)
        return Observable.interval(0, 30, TimeUnit.SECONDS)
                .map(new Function<Long, List<CardRecord>>() {
                    @Override
                    public List<CardRecord> apply(Long aLong) throws Exception {
                        //int t = (int) (aLong % (times + 3)); //当日预留失败次数
                        //List<CardRecord> failSync = getFailSync(Constant.PushCardRecord.MAX_SIZE_PER_IMG_REQUEST, t * Constant.PushCardRecord.MAX_SIZE_PER_IMG_REQUEST);
                        if( upload_pic_queue.size() == 0 && save_pic_queue.size() == 0 ) {
                            List<CardRecord> failSync = getFailSync(50, 0);
                            return failSync;
                        }
                        else
                        {
                            return new ArrayList<CardRecord>();
                        }
                    }
                })
                .flatMap(new Function<List<CardRecord>, ObservableSource<CardRecord>>() {
                    @Override
                    public ObservableSource<CardRecord> apply(List<CardRecord> cardRecords) throws Exception {

                        for( CardRecord record : cardRecords )
                        {
                            //Log.e("change+",record.getId()+" to " + CardRecordModule.this.mPresenter.getDataTransfer().get_card_holder(record.getId()));

                            //record.setCard_holder(CardRecordModule.this.mPresenter.getDataTransfer().get_card_holder(record.getId()));
                            /*
                            if( mapCard != null ) {
                                Log.e("change+",record.getId()+" to " + mapCard.get(record.getId()));
                                record.setCard_holder(mapCard.get(record.getId()));
                            }
                            else*/
                            {
                                if( record.getCard_holder() == null )
                                {
                                    /*
                                    if( mPresenter != null && mPresenter.getDataTransfer() != null  ) {
                                        String s = CardRecordModule.this.mPresenter.getDataTransfer().get_card_holder(record.getId());
                                        Log.e("change+CardRecordModule", record.getId() + " to " + s);
                                        record.setCard_holder(s);
                                    }
                                    */
                                    String s = null;
                                    synchronized(map) {
                                        s = map.get(record.getId());
                                    }
                                    Log.e("change+CardRecordModule", record.getId() + " to " + s);
                                    record.setCard_holder(s);
                                }
                                else {
                                    Log.e("change+", record.getId() + " to " + record.getCard_holder());
                                }
                            }
                        }

                        Observable<CardRecord> cardRecordObservable = reportRecordWhitImg(cardRecords, sn);
                        return cardRecordObservable;
                    }
                })
                .compose(RxSchedulers.<CardRecord>applyObservableAsync())
                .subscribeWith(new DisposableObserver<CardRecord>() {
                    @Override
                    public void onNext(@NonNull CardRecord cardRecord) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }


    /**
     * 图片未上传成功
     *
     * @param limit
     * @param offset
     * @return
     */
    private List<CardRecord> getFailUpload(int limit, int offset) {
        List<CardRecord> result;
        QueryBuilder<CardRecord> qb = cardRecordDao.queryBuilder();
        //change by baodian
        qb.where(CardRecordDao.Properties.Has_sync.eq(false));
        //qb.where(CardRecordDao.Properties.Has_upload.eq(true),CardRecordDao.Properties.Has_sync.eq(false));
        qb.limit(limit);
        //delete by baodian
        //qb.offset(offset);
        result = qb.build().list();
        return result;
    }


    /**
     * 卡记录未上传成功
     *
     * @param limit
     * @param offset
     * @return
     */
    private List<CardRecord> getFailSync(int limit, int offset) {
        List<CardRecord> result;
        QueryBuilder<CardRecord> qb = cardRecordDao.queryBuilder();
        //change by baodian
        qb/*.whereOr(CardRecordDao.Properties.Has_sync.eq(false),
                CardRecordDao.Properties.Has_upload.eq(false))*/.where(CardRecordDao.Properties.Has_upload.eq(false),CardRecordDao.Properties.Card_holder.isNotNull(),CardRecordDao.Properties.Card_holder.notEq(Config.NO_IMG));
        qb.limit(limit);
        //delete by baodian
        //qb.offset(offset);
        result = qb.build().list();
        return result;
    }


    public long getFailSync() {
        long result;
//        result = cardRecordDao.queryBuilder().where(CardRecordDao.Properties.Has_sync.eq(false)).count();

        QueryBuilder<CardRecord> qb = cardRecordDao.queryBuilder();
        qb.whereOr(CardRecordDao.Properties.Has_sync.eq(false),
                CardRecordDao.Properties.Has_upload.eq(false));
        qb.build();
        result = qb.count();
        return result;
    }

    public long getFailUpload() {
        long result;
        //result = cardRecordDao.queryBuilder().where(CardRecordDao.Properties.Has_upload.eq(false)).where(CardRecordDao.Properties.Card_holder.isNotNull(),CardRecordDao.Properties.Card_holder.notEq(Config.NO_IMG)).count();
        result = cardRecordDao.queryBuilder().where(CardRecordDao.Properties.Has_upload.eq(false)/*,CardRecordDao.Properties.Card_holder.isNotNull()*/).count();
        return result;
    }

    public long getFailRecordSync() {
        long result;
        QueryBuilder<CardRecord> qb = cardRecordDao.queryBuilder();
        qb.where(CardRecordDao.Properties.Has_sync.eq(false));
        qb.build();
        result = qb.count();
        return result;
    }


    public long getFailSyncAtOneTime(long timeStart, long timeEnd) {
        long result = 0;
        QueryBuilder<CardRecord> qb = cardRecordDao.queryBuilder();
        qb.whereOr(CardRecordDao.Properties.Has_sync.eq(false),
                qb.and(CardRecordDao.Properties.Has_upload.eq(false), CardRecordDao.Properties.Cloud_url.isNotNull()));
        qb.where(CardRecordDao.Properties.Record_time.between(timeStart, timeEnd));
        qb.build();
        result = qb.count();

        return result;
    }

    public long getFailUploadAtOneTime(long timeStart, long timeEnd) {
        long result = 0;
        QueryBuilder<CardRecord> qb = cardRecordDao.queryBuilder();
        qb.where(CardRecordDao.Properties.Cloud_url.isNull(),
                CardRecordDao.Properties.Record_time.between(timeStart, timeEnd));
        qb.build();
        result = qb.count();
        return result;
    }


    public long getFailRecordSyncAtOneTime(long timeStart, long timeEnd) {
        long result;
        QueryBuilder<CardRecord> qb = cardRecordDao.queryBuilder();
        qb.where(CardRecordDao.Properties.Has_sync.eq(false),
                CardRecordDao.Properties.Record_time.between(timeStart, timeEnd));
        qb.build();
        result = qb.count();
        return result;
    }


    public void getAllDBInfo() {
        List<CardRecord> cardRecords = cardRecordDao.loadAll();

        String s = new Gson().toJson(cardRecords);


        FileUtils.writeFileSdcardFile(FileUtils.getExternalDir(BBTreeApp.getApp()) + "/sdinfo.txt", s);
        Toast.makeText(BBTreeApp.getApp(), "已完成", Toast.LENGTH_SHORT).show();

    }

}
