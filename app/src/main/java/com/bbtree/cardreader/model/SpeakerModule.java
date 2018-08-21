package com.bbtree.cardreader.model;

import com.bbtree.baselib.net.BaseParam;
import com.bbtree.baselib.rxjava.RxUtils;
import com.bbtree.baselib.utils.FileUtils;
import com.bbtree.baselib.utils.ListUtils;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.common.Code;
import com.bbtree.cardreader.common.Urls;
import com.bbtree.cardreader.entity.dao.Speaker;
import com.bbtree.cardreader.entity.dao.SpeakerConfig;
import com.bbtree.cardreader.entity.requestEntity.ConfigBean;
import com.bbtree.cardreader.entity.requestEntity.SpeakerBean;
import com.bbtree.cardreader.entity.requestEntity.SpeakerConfigRequest;
import com.bbtree.cardreader.entity.requestEntity.SpeakerConfigResult;
import com.bbtree.cardreader.entity.requestEntity.SpeakerDeleteRequest;
import com.bbtree.cardreader.entity.requestEntity.SpeakerDeleteResult;
import com.bbtree.cardreader.entity.requestEntity.SpeakerSaveRequest;
import com.bbtree.cardreader.entity.requestEntity.SpeakerSaveResult;
import com.bbtree.cardreader.greendao.gen.SpeakerConfigDao;
import com.bbtree.cardreader.greendao.gen.SpeakerDao;
import com.bbtree.cardreader.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

import static com.bbtree.cardreader.BBTreeApp.getApp;

/**
 *
 */

public class SpeakerModule {

    static SpeakerModule instance;

    public static SpeakerModule getInstance() {
        if (instance == null) {
            instance = new SpeakerModule();
        }
        return instance;
    }

    private final SpeakerDao mSpeakerDao;
    private final SpeakerConfigDao mSpeakerConfigDao;

    /**
     * 本地音响列表
     */
    private static List<Speaker> localExistSpeakers;
    /**
     * 本地音响配置
     */
    private static SpeakerConfig localExistSpeakerConfig;

    private SpeakerModule() {
        mSpeakerDao = getApp().getDaoSessionInstance().getSpeakerDao();
        mSpeakerConfigDao = BBTreeApp.getApp().getDaoSessionInstance().getSpeakerConfigDao();
    }

    /**
     * 获取音箱配置
     *
     * @param request
     * @return
     */
    public Observable<SpeakerConfigResult> getSpeakerConfig(SpeakerConfigRequest request, String... sn) {
        if (sn.length > 0) request.setSn(sn[0]);
        return RxUtils.postEntity(Urls.SPEAKERCONFIG, request)
                .map(RxUtils.getObject(SpeakerConfigResult.class))
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 删除音箱表
     *
     * @param request
     * @return
     */
    public Observable<SpeakerDeleteResult> speakerDelete(SpeakerDeleteRequest request) {
        return RxUtils.postEntity(Urls.SPEAKERDELETE, request)
                .map(RxUtils.getObject(SpeakerDeleteResult.class))
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 保存音箱信息
     *
     * @param request
     * @return
     */
    public Observable<SpeakerSaveResult> speakerSave(SpeakerSaveRequest request) {
        return RxUtils.postEntity(Urls.SPEAKERSAVE, request)
                .map(RxUtils.getObject(SpeakerSaveResult.class))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void saveSpeakerConfigToDB(SpeakerConfigResult speakerConfigResult) {

        List<SpeakerBean> speakers = speakerConfigResult.getSpeakers();
        if (!ListUtils.isZero(speakers)) {
            List<Speaker> targetList = new ArrayList();
            // 将本地先删除
            mSpeakerDao.deleteAll();
            // 添加服务器端最新列表
            for (SpeakerBean speakerBean : speakerConfigResult.getSpeakers()) {
                Speaker speaker = new Speaker(speakerBean.getId(), speakerBean.getSchoolId(),
                        speakerBean.getName(), speakerBean.getCode(), speakerBean.getClassIds(), speakerBean.getIsAll() == 1,
                        speakerBean.getGroupId(), speakerBean.getNum());
                targetList.add(speaker);
            }
            mSpeakerDao.insertInTx(targetList);
        }
        localExistSpeakers = mSpeakerDao.queryBuilder().list();

        // 保存播放配置
        ConfigBean mConfig = speakerConfigResult.getConfig();
        SpeakerConfig tempSpeakerConfig = new SpeakerConfig(0L, (int) mConfig.getSchoolId(), mConfig.getSpeed(),
                mConfig.getNamePlayNum(), mConfig.getPlayNum(), mConfig.getPlayClass());
        mSpeakerConfigDao.insertOrReplace(tempSpeakerConfig);
        List<SpeakerConfig> list = mSpeakerConfigDao.queryBuilder().list();
        if (list != null && list.size() > 0) {
            localExistSpeakerConfig = list.get(0);
        }

    }

    /**
     * 获取到本地所有音箱
     *
     * @return
     */
    public List<Speaker> queryAllSpeakerList() {
        return mSpeakerDao.queryBuilder().list();
    }

    /**
     * 从本地数据中获取音箱配置
     */
    public void getDBSpeakers() {

        localExistSpeakers = mSpeakerDao.queryBuilder().list();

        List<SpeakerConfig> list = mSpeakerConfigDao.queryBuilder().list();
        if (!ListUtils.isZero(list)) {
            localExistSpeakerConfig = list.get(0);
        }
    }

    public static List<Speaker> getLocalExistSpeakers() {
        return localExistSpeakers;
    }

    public static SpeakerConfig getLocalExistSpeakerConfig() {
        return localExistSpeakerConfig;
    }

    /**
     * 获取音响列表
     *
     * @param sn
     */
    public void getLocalAllSpeakers(String sn) {
        SpeakerConfigRequest request = new SpeakerConfigRequest();
        request.setDeviceId(BaseParam.getDeviceId());
        request.setSchoolId(SPUtils.getSchoolId(0L));
        request.setSn("");
        getSpeakerConfig(request, sn)
                .filter(new Predicate<SpeakerConfigResult>() {
                    @Override
                    public boolean test(SpeakerConfigResult speakerConfigResult) throws Exception {
                        return speakerConfigResult.getCode() == Code.SUCCESS;
                    }
                })
                .doOnNext(new Consumer<SpeakerConfigResult>() {
                    @Override
                    public void accept(SpeakerConfigResult speakerConfigResult) throws Exception {
                        FileUtils.writeFileSdcardFile(FileUtils.getExternalDir(BBTreeApp.getApp()) + "/666", "666");
                        saveSpeakerConfigToDB(speakerConfigResult);
                    }
                });
    }
}
