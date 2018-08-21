package com.bbtree.cardreader.model;

import com.bbtree.cardreader.common.Code;
import com.bbtree.cardreader.entity.requestEntity.TempConfigResult;
import com.bbtree.cardreader.utils.SPUtils;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;

import static org.junit.Assert.assertEquals;

/**
 * Created by qiujj on 2017/4/25.
 */
public class TempRecordModuleTest{

    @Test
    public void getSchoolTest(){
        long schoolId = SPUtils.getSchoolId(0L);
        assertEquals(10,schoolId);
    }

    @Test
    public void getTempConfig() throws Exception {
        long schoolId = SPUtils.getSchoolId(0L);
        Map map = new HashMap();
        map.put("schoolId", schoolId);
        TempRecordModule.getInstance().getTempConfig(map)

                .filter(new Predicate<TempConfigResult>() {
                    @Override
                    public boolean test(TempConfigResult resultObject) {
                        assertEquals(resultObject.getCode(),200);
                        if (resultObject.getCode() == Code.SUCCESS) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                })
                .subscribe(new Observer<TempConfigResult>() {
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(TempConfigResult tempConfig) {
//                        assertNotNull(tempConfig);
                    }
                });
    }

}