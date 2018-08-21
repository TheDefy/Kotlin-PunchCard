package com.bbtree.cardreader.entity.requestEntity;

import java.util.List;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2016/10/27
 * Create Time: 上午11:19
 */
public class FailRecordReportRequest {
    public FailRecordReportRequest() {
    }

    public String deviceId;
    public String sn;
    public List<FailRecordReportItem> items;
    public long allFailItem;
    public long allFailRecord;
    public long allFailUpload;

}
