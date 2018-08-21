package com.bbtree.cardreader.report;

import android.content.Context;

import com.bbtree.baselib.net.BaseParam;
import com.bbtree.baselib.rxjava.RxUtils;
import com.bbtree.cardreader.common.Urls;
import com.bbtree.cardreader.entity.requestEntity.FailRecordReportItem;
import com.bbtree.cardreader.entity.requestEntity.FailRecordReportRequest;
import com.bbtree.cardreader.model.CardRecordModule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.observers.DefaultObserver;


/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2016/10/26
 * Create Time: 下午5:29
 */
public class FailRecordReport {
    private String TAG = FailRecordReport.class.getSimpleName();
    private volatile static FailRecordReport ourInstance;

    public static FailRecordReport getInstance() {
        if (ourInstance == null) {
            synchronized (FailRecordReport.class) {
                if (ourInstance == null) {
                    ourInstance = new FailRecordReport();
                }
            }
        }
        return ourInstance;
    }

    public FailRecordReport() {
    }

    public void getReport(Context cxt, String sn) {
        long failAll = CardRecordModule.getInstance().getFailSync();
        long failUpload = CardRecordModule.getInstance().getFailUpload();
        long failRecord = CardRecordModule.getInstance().getFailRecordSync();


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        int statistics = 4;
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        List<FailRecordReportItem> list = new ArrayList<>();
        for (int i = 0; i < statistics; i++) {
            long endTime = calendar.getTimeInMillis();
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            long startTime = calendar.getTimeInMillis();
            long failItem = CardRecordModule.getInstance().getFailSyncAtOneTime(startTime, endTime);
            long failUploadItem = CardRecordModule.getInstance().getFailUploadAtOneTime(startTime, endTime);
            long failRecordItem = CardRecordModule.getInstance().getFailRecordSyncAtOneTime(startTime, endTime);
            FailRecordReportItem item = new FailRecordReportItem();
            item.time = startTime;
            item.failItem = failItem;
            item.failUpload = failUploadItem;
            item.failRecord = failRecordItem;
            list.add(item);
        }
        FailRecordReportRequest request = new FailRecordReportRequest();
        request.items = list;
        request.allFailItem = failAll;
        request.allFailRecord = failRecord;
        request.allFailUpload = failUpload;
        request.sn = sn;
        request.deviceId = BaseParam.getDeviceId();

        RxUtils.postEntity(Urls.FAILRECORDNUMBER,request)
                .subscribe(new DefaultObserver<String>() {
                    @Override
                    public void onNext(String value) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}
