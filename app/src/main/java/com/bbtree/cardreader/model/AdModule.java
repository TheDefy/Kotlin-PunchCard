package com.bbtree.cardreader.model;

import com.bbtree.baselib.rxjava.RxUtils;
import com.bbtree.baselib.utils.ListUtils;
import com.bbtree.cardreader.common.Urls;
import com.bbtree.cardreader.entity.dao.PlayNum;
import com.bbtree.cardreader.entity.requestEntity.AdUploadResultNode;
import com.bbtree.cardreader.entity.requestEntity.GetAdResData;
import com.bbtree.cardreader.entity.requestEntity.PlayNumResult;
import com.bbtree.cardreader.greendao.gen.PlayNumDao;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import org.greenrobot.greendao.query.QueryBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.bbtree.cardreader.BBTreeApp.getApp;

/**
 * 广告
 */

public class AdModule {

    static AdModule instance;

    public static AdModule getInstance() {
        if (instance == null) {
            instance = new AdModule();
        }
        return instance;
    }

    private final PlayNumDao mPlayNumDao;

    private AdModule() {
        mPlayNumDao = getApp().getDaoSessionInstance().getPlayNumDao();
    }

    /**
     * 获取广告接口
     *
     * @param map
     * @return
     */
    public Observable<GetAdResData> getAd(Map map) {
        return RxUtils.postEntity(Urls.GETAD, map)
                .map(RxUtils.getObject(GetAdResData.class))
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 上报广告播放次数
     *
     * @param map
     * @return
     */
    public Observable<PlayNumResult> pushPlayNum(Map map) {
        return RxUtils.postEntity(Urls.ADPLAYNUM, map)
                .map(RxUtils.getObject(PlayNumResult.class));
    }

    /**
     * 数据的存储
     *
     * @param playNum
     */
    public void insert(PlayNum playNum) {
        playNum.setId(playNum.getAdId() + "_" + getNowDateShort());
        playNum.setDate(getNowDateShort());
        List<PlayNum> list = mPlayNumDao.queryBuilder().where(PlayNumDao.Properties.Id.eq(playNum.getId())).build().list();
        if (!ListUtils.isZero(list)) {
            PlayNum playNumTemp = list.get(0);
            playNumTemp.setNum(playNumTemp.getNum() + 1);
            mPlayNumDao.insertOrReplace(playNumTemp);
        } else {
            playNum.setNum(1);
            mPlayNumDao.insert(playNum);
        }

        List<PlayNum> list1 = mPlayNumDao.queryBuilder().where(PlayNumDao.Properties.Id.eq(playNum.getId())).build().list();
        if (ListUtils.isZero(list1)) return;
        Integer num = list1.get(0).getNum();
        String date = list1.get(0).getDate();
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(String.valueOf(playNum.getAdId()))
                .putContentType("ad")
                .putContentId(playNum.getId())
                .putCustomAttribute("date", date)
                .putCustomAttribute("Count", num));
    }

    /**
     * 查询表中所有的统计记录
     *
     * @return
     */
    public List<PlayNum> queryAllPlayNumList() {
        QueryBuilder<PlayNum> qb = mPlayNumDao.queryBuilder();
        List<PlayNum> list = qb.list();
        return list;
    }

    /**
     * 删除上传后的数据
     *
     * @param
     * @return
     */
    public void queryAllPlayNList(PlayNumResult result) {
        if (result == null || result.data == null || ListUtils.isZero(result.data.getRsList()))
            return;

        List<AdUploadResultNode> adUploadResultNodes = result.data.getRsList();
        List<String> deleteDataIds = new ArrayList<>();
        for (AdUploadResultNode adUploadResultNodeTemp : adUploadResultNodes) {
            if (adUploadResultNodeTemp.isResult()) {
                deleteDataIds.add(adUploadResultNodeTemp.getId());
            }
        }
        if (ListUtils.isZero(deleteDataIds)) return;
        mPlayNumDao.deleteByKeyInTx(deleteDataIds);
    }


    /**
     * 获取现在时间
     *
     * @return返回短时间格式 yyyyMMddHH
     */
    private String getNowDateShort() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");
        String dateString = formatter.format(System.currentTimeMillis());
        return dateString;
    }

}
