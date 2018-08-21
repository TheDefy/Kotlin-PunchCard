// DataTransfer.aidl
package com.bbtree.cardreader;
import com.bbtree.cardreader.entity.eventbus.SwipeCardInfo;
import com.bbtree.cardreader.entity.TempReportData;

interface DataTransfer {
    void setCameraInfo(int width,int height,int cameraPreviewFormat);
    void transferCardRecord(in SwipeCardInfo swipeCardInfo);
    void transferTempRecord(in TempReportData tempReportData);

    String get_card_holder(in String id);

    Map get_card_map();

    void startTask();
    void overTask();

}
