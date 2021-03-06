package com.bbtree.cardreader.entity.dao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Entity mapped to table "CARD_RECORD".
 */
@Entity
public class CardRecord implements Parcelable {

    @SerializedName("recordId")
    @Id
    private String id;
    @SerializedName("macId")
    private String card_serial_number;
    private String card_holder;
    @SerializedName("punchTime")
    private long record_time;
    /**
     * 卡记录上传成功
     */
    private Boolean has_sync;
    /**
     * 卡记录中 图片url上传成功
     */
    private Boolean has_upload;

    /**
     * 图片上传成功
     */
    @SerializedName("imgUrl")
    private String cloud_url;
    @SerializedName("cardNo")
    private String card_number;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCard_serial_number() {
        return card_serial_number;
    }

    public void setCard_serial_number(String card_serial_number) {
        this.card_serial_number = card_serial_number;
    }

    public String getCard_holder() {
        return card_holder;
    }

    public void setCard_holder(String card_holder) {
        this.card_holder = card_holder;
    }

    public long getRecord_time() {
        return record_time;
    }

    public void setRecord_time(long record_time) {
        this.record_time = record_time;
    }

    public Boolean getHas_sync() {
        return has_sync;
    }

    public void setHas_sync(Boolean has_sync) {
        this.has_sync = has_sync;
    }

    public Boolean getHas_upload() {
        return has_upload;
    }

    public void setHas_upload(Boolean has_upload) {
        this.has_upload = has_upload;
    }

    public String getCloud_url() {
        return cloud_url;
    }

    public void setCloud_url(String cloud_url) {
        this.cloud_url = cloud_url;
    }

    public String getCard_number() {
        return card_number;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.card_serial_number);
        dest.writeString(this.card_holder);
        dest.writeLong(this.record_time);
        dest.writeValue(this.has_sync);
        dest.writeValue(this.has_upload);
        dest.writeString(this.cloud_url);
        dest.writeString(this.card_number);
    }

    public CardRecord() {
    }

    protected CardRecord(Parcel in) {
        this.id = in.readString();
        this.card_serial_number = in.readString();
        this.card_holder = in.readString();
        this.record_time = in.readLong();
        this.has_sync = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.has_upload = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.cloud_url = in.readString();
        this.card_number = in.readString();
    }

    @Keep
    public CardRecord(String id, String cardSerialNumber, String cardHolder, long recordTime,
            Boolean hasSync, Boolean hasUpload, String cloudUrl, String cardNumber) {
        this.id = id;
        this.card_serial_number = cardSerialNumber;
        this.card_holder = cardHolder;
        this.record_time = recordTime;
        this.has_sync = hasSync;
        this.has_upload = hasUpload;
        this.cloud_url = cloudUrl;
        this.card_number = cardNumber;
    }

    public static final Creator<CardRecord> CREATOR = new Creator<CardRecord>() {
        @Override
        public CardRecord createFromParcel(Parcel source) {
            return new CardRecord(source);
        }

        @Override
        public CardRecord[] newArray(int size) {
            return new CardRecord[size];
        }
    };
}
