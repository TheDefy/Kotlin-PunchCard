package com.bbtree.cardreader.model;

import com.bbtree.baselib.net.BaseParam;
import com.bbtree.baselib.net.GsonParser;
import com.bbtree.baselib.net.ResultObject;
import com.bbtree.baselib.rxjava.RxUtils;
import com.bbtree.baselib.utils.ListUtils;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.common.Code;
import com.bbtree.cardreader.common.Urls;
import com.bbtree.cardreader.config.Config;
import com.bbtree.cardreader.entity.dao.CardInfo;
import com.bbtree.cardreader.greendao.gen.CardInfoDao;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DefaultObserver;


/**
 * Created by qiujj on 2017/4/1.
 */

public class CardInfoModule {
    public static final int ADDCARD = 0;
    public static final int DELETECARD = 1;
    private final CardInfoDao mCardInfoDao;

    static CardInfoModule instance;

    public static CardInfoModule getInstance() {
        if (instance == null) {
            instance = new CardInfoModule();
        }
        return instance;
    }

    private CardInfoModule() {
        mCardInfoDao = BBTreeApp.getApp().getDaoSessionInstance().getCardInfoDao();
    }

    public void curdCards(String sn, final int mode) {
        String url = "";
        HashMap map = new HashMap();
        map.put("deviceId", BaseParam.getDeviceId());
        map.put("sn", sn);
        switch (mode) {
            case ADDCARD:
                url = Urls.CARDSADDPULL;
                break;
            case DELETECARD:
                url = Urls.CARDSDELETEPULL;
                break;
        }
        RxUtils.postMap(url, map)
                .map(RxUtils.getList("cardInfos", CardInfo[].class))
                .filter(new Predicate<ResultObject>() {
                    @Override
                    public boolean test(ResultObject resultObject) throws Exception {
                        return resultObject.getCode() == Code.SUCCESS;
                    }
                })
                .flatMapIterable(new Function<ResultObject, Iterable<CardInfo>>() {
                    @Override
                    public Iterable<CardInfo> apply(ResultObject resultObject) throws Exception {
                        return ((List<CardInfo>) resultObject.getObject());
                    }
                })
                .subscribe(new Observer<CardInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CardInfo value) {
                        switch (mode) {
                            case ADDCARD:
                                if (!ListUtils.isZero(value.getFamily())) {
                                    value.setFamilyString(GsonParser.parserToJson(value.getFamily()));
                                }
                                mCardInfoDao.insertOrReplace(value);
                                break;
                            case DELETECARD:
                                mCardInfoDao.delete(value);
                                break;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public Observable<ResultObject> getCards(String... sn) {
        final long startTime = System.currentTimeMillis();
        Map map = new HashMap();
        if (sn.length > 0) map.put("sn", sn[0]);
        map.put("deviceId", BaseParam.getDeviceId());
        return RxUtils.postMap(Urls.CARDSPULL, map)
                .map(RxUtils.getList("cardInfos", CardInfo[].class))
                .filter(new Predicate<ResultObject>() {
                    @Override
                    public boolean test(ResultObject resultObject) throws Exception {
                        return resultObject.getCode() == Code.SUCCESS;
                    }
                })
                .doOnNext(new Consumer<ResultObject>() {
                    @Override
                    public void accept(ResultObject resultObject) throws Exception {

                        List<CardInfo> cards = (List<CardInfo>) resultObject.getObject();
                        if (!ListUtils.isZero(cards)) {
                            for (CardInfo temp : cards) {
                                temp.setFamilyString(GsonParser.parserToJson(temp.getFamily()));
                            }
                            List<CardInfo> original = mCardInfoDao.queryBuilder().build().list();//拿到现在表中的数据
                            if (!ListUtils.isZero(original)) {
                                mCardInfoDao.deleteAll();
//                                Map<String, CardInfo> mapNew = new HashMap<>();
//                                for (CardInfo cardInfo : cards) {
//                                    mapNew.put(cardInfo.getId(), cardInfo);//遍历出来放到map
//                                }
//                                List<CardInfo> need2Delete = new ArrayList<>();//存储待删除的数据
//                                for (CardInfo cardInfo : original) {
//                                    if (!mapNew.containsKey(cardInfo.getId())) {//判断是否为待删除数据
//                                        need2Delete.add(cardInfo);
//                                    }
//                                }
//                                long need2DelSize = need2Delete.size();
//                                Logger.i(">>>>>>>>>>>>>>>>>>>>need 2 delete data size is:" + need2DelSize
//                                        + " and coast time:" + (System.currentTimeMillis() - startTime));
//                                if (!ListUtils.isZero(need2Delete)) {
//                                    mCardInfoDao.deleteInTx(need2Delete);//删除需要删除的数据
//                                }
                            }
                            insertOrReplace(cards);//更新得到的数据
                            Logger.i("db replace time:" + (System.currentTimeMillis() - startTime));
                        }
                    }
                })
                .retry(2)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public List<CardInfo> getByCardNumber(String cardSerialNumber) {
        return mCardInfoDao.queryBuilder()
                .where(CardInfoDao.Properties.Id
                        .eq(cardSerialNumber)).build().list();
    }

    public void insertOrReplace(List<CardInfo> cards) {
        mCardInfoDao.insertOrReplaceInTx(cards);
    }

    /**
     * 上传学校所有卡给服务器对比
     *
     * @param sn
     */
    public void cardsPush(String... sn) {
        long size = mCardInfoDao.count();
        long times = size / Config.maxCardPushSize;
        int grow = size % Config.maxCardPushSize == 0 ? 0 : 1;
        times += grow;
        long cmdTime = System.currentTimeMillis();
        Logger.i("process cards push server. local cards size:" + size + ",need "
                + times + " times finish it.CMD time is:" + cmdTime);
        for (int i = 0; i < times; i++) {
            List<CardInfo> list = mCardInfoDao.queryBuilder().limit(Config.maxCardPushSize).offset(Config.maxCardPushSize * i).build().list();
            if (!ListUtils.isZero(list)) {
                requestCardsPush(list, cmdTime, sn);
            }
        }
    }

    /**
     * 给服务器本地卡数据
     *
     * @param list
     * @param cmdTime
     * @param sn
     */
    private void requestCardsPush(List<CardInfo> list, long cmdTime, String... sn) {
        Map map = new HashMap();
        map.put("cardList", list);
        map.put("batchNum", cmdTime);
        if (sn.length > 0) map.put("sn", sn[0]);
        RxUtils.postMap(Urls.CARDSPUSH, map)
                .map(RxUtils.getMap())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<ResultObject>() {
                    @Override
                    public void onNext(ResultObject value) {
                        if (value.getCode() == Code.SUCCESS) {
                            Logger.i(">>>CardsPushRequest onSuccess>>>");
                        } else {
                            Logger.i(">>>CardsPushRequest onSuccess but error code is:>>>" + value.getCode());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.i(">>>CardsPushRequest onSuccess but error code is:>>>" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
