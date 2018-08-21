package com.bbtree.cardreader.entity;

import com.bbtree.baselib.net.ResultObject;
import com.bbtree.cardreader.entity.requestEntity.SchoolInfo;

/**
 * Created by qiujj on 2017/5/9.
 */

public class DataInfoResult {
    public SchoolInfo schoolInfo;
    public ResultObject deviceConfig;
    public ResultObject cards;

    public DataInfoResult(SchoolInfo schoolInfo, ResultObject deviceConfig, ResultObject cards){
        this.schoolInfo = schoolInfo;
        this.deviceConfig = deviceConfig;
        this.cards = cards;
    }
}
