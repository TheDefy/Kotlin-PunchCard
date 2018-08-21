package com.bbtree.cardreader.model;

import android.database.Cursor;

import com.bbtree.baselib.net.ResultObject;
import com.bbtree.baselib.rxjava.RxSchedulers;
import com.bbtree.baselib.rxjava.RxUtils;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.common.Code;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.common.Urls;
import com.bbtree.cardreader.entity.TempReportData;
import com.bbtree.cardreader.entity.dao.CardInfo;
import com.bbtree.cardreader.entity.dao.TempRecord;
import com.bbtree.cardreader.entity.requestEntity.TempConfigResult;
import com.bbtree.cardreader.greendao.gen.CardInfoDao;
import com.bbtree.cardreader.greendao.gen.TempRecordDao;
import com.orhanobut.logger.Logger;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.observers.DisposableObserver;


/**
 * Created by qiujj on 2017/4/1.
 */

public class TempRecordModule {

    static TempRecordModule instance;

    public static TempRecordModule getInstance() {
        if (instance == null) {
            instance = new TempRecordModule();
        }
        return instance;
    }

    private final TempRecordDao mTempRecordDao;

    private TempRecordModule() {
        mTempRecordDao = BBTreeApp.getApp().getDaoSessionInstance().getTempRecordDao();
    }

    /**
     * 获取测温枪配置信息
     *
     * @param map
     * @return
     */
    public Observable<TempConfigResult> getTempConfig(Map map) {
        return RxUtils.postMap(Urls.TEMPCONFIG, map)
                .map(RxUtils.getObject(TempConfigResult.class))
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 温度数据上传
     *
     * @param tempRecords
     */
    public void uploadTemRecord(List<TempReportData> tempRecords, String... sn) {
        uploadTemRecordComm(tempRecords, sn)
                .subscribe(new DefaultObserver<ResultObject>() {
                    @Override
                    public void onNext(ResultObject value) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 保存本地数据库
     *
     * @param record
     */
    public void saveTempRecord(TempRecord record) {
        mTempRecordDao.insert(record);
    }


    //-------------------处理失败的温度纪录--------------------

    public Disposable uploadFailTempRecord(final String... sn) {
        final long sum = getFailSync();
        final int needSheriff = (int) (sum % Constant.PushCardRecord.MAX_SIZE_PER_REQUEST > 0 ?
                sum / Constant.PushCardRecord.MAX_SIZE_PER_REQUEST + 1 :
                sum / Constant.PushCardRecord.MAX_SIZE_PER_REQUEST);
        return Observable.interval(3, Constant.PushCardRecord.PATROL_INTERVAL, TimeUnit.MINUTES)
                .map(new Function<Long,  List<TempReportData>>() {
                    @Override
                    public List<TempReportData> apply(Long aLong) throws Exception {
                        int l = (int) (aLong % (needSheriff + 3));
                            final List<TempReportData> list = getFailRecordWithCardInfo(Constant.PushCardRecord.MAX_SIZE_PER_REQUEST,
                                    l * Constant.PushCardRecord.MAX_SIZE_PER_REQUEST);
                        return list ;
                    }
                })
                .flatMap(new Function<List<TempReportData>, Observable<ResultObject>>() {
                    @Override
                    public Observable<ResultObject> apply(final List<TempReportData> tempReportDatas) throws Exception {
                        Observable<ResultObject> resultObjectObservable = uploadTemRecordComm(tempReportDatas, sn);
                        Logger.e("mapperisNull?",resultObjectObservable);
                        return resultObjectObservable;
                    }
                })
                .compose(RxSchedulers.<ResultObject>applyObservableAsync())
                .subscribeWith(new DisposableObserver<ResultObject>() {
                    @Override
                    public void onNext(@NonNull ResultObject resultObject) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private Observable<ResultObject> uploadTemRecordComm(final List<TempReportData> tempRecords, String... sn) {
        Map map = new HashMap();
        map.put("tempRecords", tempRecords);
        if (sn.length > 0) map.put("sn", sn[0]);
        return RxUtils.postMap(Urls.TEMPREPORT, map)
                .map(RxUtils.getObject(ResultObject.class))
                .subscribeOn(AndroidSchedulers.mainThread())
                .filter(new Predicate<ResultObject>() {
                    @Override
                    public boolean test(ResultObject resultObject) {
                        return resultObject.getCode() == Code.SUCCESS;
                    }
                })
                .doOnNext(new Consumer<ResultObject>() {
                    @Override
                    public void accept(ResultObject resultObject) throws Exception {
                        for (TempReportData tempReportData : tempRecords) {
                            TempRecord record = tempReportData.getTempNode();
                            if (record == null) {
                                continue;
                            }
                            record.setHas_sync(true);
                            mTempRecordDao.update(record);
                        }
                    }
                });
    }

    private long getFailSync() {
        long result;
        try {
            QueryBuilder<TempRecord> qb = mTempRecordDao.queryBuilder();
            qb.where(TempRecordDao.Properties.Has_sync.eq(false));
            qb.build();
            result = qb.count();
        } finally {
        }
        return result;
    }

    public List<TempReportData> getFailRecordWithCardInfo(int limit, int offset) {
        //select * from (select  * from TEMP_RECORD as temp where temp.HAS_SYNC == 1 ) as fail inner join CARD_INFO as info on fail.MAC_ID = info._id;
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder
                .append("select * from (")
                .append("select ")
                .append(TempRecordDao.Properties.Id.columnName)
                .append(" as TempId ,")
                .append(TempRecordDao.Properties.Card_record_id.columnName)
                .append(",")
                .append(TempRecordDao.Properties.Mac_id.columnName)
                .append(",")
                .append(TempRecordDao.Properties.School_id.columnName)
                .append(",")
                .append(TempRecordDao.Properties.Temperature.columnName)
                .append(",")
                .append(TempRecordDao.Properties.Temp_unit.columnName)
                .append(",")
                .append(TempRecordDao.Properties.Temp_time.columnName)
                .append(",")
                .append(TempRecordDao.Properties.Has_sync.columnName)
                .append(" from ")
                .append(TempRecordDao.TABLENAME)
                .append(" AS temp ")
                .append("where temp.")
                .append(TempRecordDao.Properties.Has_sync.columnName)
                .append(" != 1 ")
                .append(" limit ")
                .append(limit)
                .append(" offset ")
                .append(offset)
                .append(" ) ")
                .append("as fail ")
                .append(" inner join ")
                .append(CardInfoDao.TABLENAME)
                .append(" as info on fail.")
                .append(TempRecordDao.Properties.Mac_id.columnName)
                .append(" = ")
                .append("info.")
                .append(CardInfoDao.Properties.Id.columnName);
        Logger.i(">>>getFailRecordWithCardInfo>>>" + sqlBuilder.toString());
        try {
            Database db = mTempRecordDao.getDatabase();
            Cursor cursor = db.rawQuery(sqlBuilder.toString(), null);
            if (cursor == null) {
                return null;
            } else {
                List<TempReportData> list = new ArrayList<>();

                while (cursor.moveToNext()) {
                    TempReportData tempReportData = new TempReportData();
                    String cardInfoID = cursor.getString(cursor.getColumnIndex(CardInfoDao.Properties.Id.columnName));
                    String cardInfoName = cursor.getString(cursor.getColumnIndex(CardInfoDao.Properties.Name.columnName));
                    String cardInfoFamilyName = cursor.getString(cursor.getColumnIndex(CardInfoDao.Properties.FamilyName.columnName));
                    String cardInfoAlias = cursor.getString(cursor.getColumnIndex(CardInfoDao.Properties.Alias.columnName));
                    String cardInfoAvatar = cursor.getString(cursor.getColumnIndex(CardInfoDao.Properties.Avatar.columnName));
                    int cardInfoLevel = cursor.getInt(cursor.getColumnIndex(CardInfoDao.Properties.Level.columnName));
                    int cardInfoIsVip = cursor.getInt(cursor.getColumnIndex(CardInfoDao.Properties.IsVip.columnName));
                    long cardInfoBirthday = cursor.getLong(cursor.getColumnIndex(CardInfoDao.Properties.Birthday.columnName));
                    int cardInfoClassId = cursor.getInt(cursor.getColumnIndex(CardInfoDao.Properties.ClassId.columnName));
                    int cardInfoFlowerScore = cursor.getInt(cursor.getColumnIndex(CardInfoDao.Properties.FlowerScore.columnName));
                    String cardInfoClassName = cursor.getString(cursor.getColumnIndex(CardInfoDao.Properties.ClassName.columnName));
                    int cardInfoUserType = cursor.getInt(cursor.getColumnIndex(CardInfoDao.Properties.UserType.columnName));
                    int cardInfoUserID = cursor.getInt(cursor.getColumnIndex(CardInfoDao.Properties.UserId.columnName));
                    int cardInfoCardLevel = cursor.getInt(cursor.getColumnIndex(CardInfoDao.Properties.CardLevel.columnName));
                    String cardInfoCardNumber = cursor.getString(cursor.getColumnIndex(CardInfoDao.Properties.CardNumber.columnName));
                    CardInfo cardInfo = new CardInfo(cardInfoID, cardInfoName, cardInfoFamilyName, cardInfoAlias, cardInfoAvatar, cardInfoLevel,
                            cardInfoIsVip == 1, String.valueOf(cardInfoBirthday), cardInfoClassId, cardInfoFlowerScore, null, cardInfoClassName,
                            cardInfoUserType, cardInfoUserID, cardInfoCardLevel, cardInfoCardNumber);
                    tempReportData.setCardInfoNode(cardInfo);

                    String tempId = cursor.getString(cursor.getColumnIndex(TempRecordDao.Properties.Id.columnName));
                    String tempCardRecordId = cursor.getString(cursor.getColumnIndex(TempRecordDao.Properties.Card_record_id.columnName));
                    String tempMacId = cursor.getString(cursor.getColumnIndex(TempRecordDao.Properties.Mac_id.columnName));
                    long tempSchoolId = cursor.getLong(cursor.getColumnIndex(TempRecordDao.Properties.School_id.columnName));
                    float temperature = cursor.getFloat(cursor.getColumnIndex(TempRecordDao.Properties.Temperature.columnName));
                    int tempUnit = cursor.getInt(cursor.getColumnIndex(TempRecordDao.Properties.Temp_unit.columnName));
                    long tempTime = cursor.getLong(cursor.getColumnIndex(TempRecordDao.Properties.Temp_time.columnName));
                    int tempHasSync = cursor.getInt(cursor.getColumnIndex(TempRecordDao.Properties.Has_sync.columnName));
                    TempRecord tempRecord = new TempRecord(tempId, tempCardRecordId, tempMacId, tempSchoolId, temperature, tempUnit, new Date(tempTime).getTime(), tempHasSync == 1);
                    tempReportData.setTempNode(tempRecord);
                    list.add(tempReportData);
                }
                try {
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return list;
            }
        } finally {
        }

    }
}
