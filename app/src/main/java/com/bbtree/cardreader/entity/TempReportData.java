package com.bbtree.cardreader.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.bbtree.cardreader.entity.dao.CardInfo;
import com.bbtree.cardreader.entity.dao.TempRecord;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/10/31
 * Create Time: 上午12:08
 */
public class TempReportData implements Parcelable {
    private TempRecord tempNode;
    private CardInfo cardInfoNode;

    public TempRecord getTempNode() {
        return tempNode;
    }

    public void setTempNode(TempRecord tempNode) {
        this.tempNode = tempNode;
    }

    public CardInfo getCardInfoNode() {
        return cardInfoNode;
    }

    public void setCardInfoNode(CardInfo cardInfoNode) {
        this.cardInfoNode = cardInfoNode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.tempNode, flags);
        dest.writeParcelable(this.cardInfoNode, flags);
    }

    public TempReportData() {
    }

    protected TempReportData(Parcel in) {
        this.tempNode = in.readParcelable(TempRecord.class.getClassLoader());
        this.cardInfoNode = in.readParcelable(CardInfo.class.getClassLoader());
    }

    public static final Creator<TempReportData> CREATOR = new Creator<TempReportData>() {
        @Override
        public TempReportData createFromParcel(Parcel source) {
            return new TempReportData(source);
        }

        @Override
        public TempReportData[] newArray(int size) {
            return new TempReportData[size];
        }
    };
}
