package com.bbtree.cardreader.entity.eventbus;

import android.os.Parcel;
import android.os.Parcelable;

import com.bbtree.cardreader.entity.dao.CardInfo;
import com.bbtree.cardreader.entity.dao.CardRecord;

/**
 * 用户刷卡时候，通知UI界面的数据
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/06/11
 * Time: 下午3:54
 */
public class SwipeCardInfo implements Parcelable {
    private CardRecord cardRecord;
    private CardInfo cardInfo;
    private byte[] photoByte;//拍照数据使用
    private String path;
    private int degrees;

    public CardRecord getCardRecord() {
        return cardRecord;
    }

    public void setCardRecord(CardRecord cardRecord) {
        this.cardRecord = cardRecord;
    }

    public CardInfo getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(CardInfo cardInfo) {
        this.cardInfo = cardInfo;
    }

    public byte[] getPhotoByte() {
        return photoByte;
    }

    public void setPhotoByte(byte[] photoByte) {
        this.photoByte = photoByte;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDegrees() {
        return degrees;
    }

    public void setDegrees(int degrees) {
        this.degrees = degrees;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.cardRecord, flags);
        dest.writeParcelable(this.cardInfo, flags);
        dest.writeByteArray(this.photoByte);
        dest.writeString(this.path);
        dest.writeInt(this.degrees);
    }

    public SwipeCardInfo() {
    }

    protected SwipeCardInfo(Parcel in) {
        this.cardRecord = in.readParcelable(CardRecord.class.getClassLoader());
        this.cardInfo = in.readParcelable(CardInfo.class.getClassLoader());
        this.photoByte = in.createByteArray();
        this.path = in.readString();
        this.degrees = in.readInt();
    }

    public static final Creator<SwipeCardInfo> CREATOR = new Creator<SwipeCardInfo>() {
        @Override
        public SwipeCardInfo createFromParcel(Parcel source) {
            return new SwipeCardInfo(source);
        }

        @Override
        public SwipeCardInfo[] newArray(int size) {
            return new SwipeCardInfo[size];
        }
    };
}
