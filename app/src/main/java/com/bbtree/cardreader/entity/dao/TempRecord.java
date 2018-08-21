package com.bbtree.cardreader.entity.dao;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by qiujj on 2017/3/28.
 */
@Entity
public class TempRecord implements Parcelable{
    @SerializedName("tempId")
    @Id
    private String id;
    @SerializedName("recordId")
    private String card_record_id;
    private String mac_id;
    @SerializedName("schoolId")
    private Long school_id;
    private Float temperature;
    @SerializedName("tempUnit")
    private Integer temp_unit;
    @SerializedName("tempTime")
    private long temp_time;
    private Boolean has_sync;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCard_record_id() {
        return card_record_id;
    }

    public void setCard_record_id(String card_record_id) {
        this.card_record_id = card_record_id;
    }

    public String getMac_id() {
        return mac_id;
    }

    public void setMac_id(String mac_id) {
        this.mac_id = mac_id;
    }

    public Long getSchool_id() {
        return school_id;
    }

    public void setSchool_id(Long school_id) {
        this.school_id = school_id;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Integer getTemp_unit() {
        return temp_unit;
    }

    public void setTemp_unit(Integer temp_unit) {
        this.temp_unit = temp_unit;
    }

    public long getTemp_time() {
        return temp_time;
    }

    public void setTemp_time(long temp_time) {
        this.temp_time = temp_time;
    }

    public Boolean getHas_sync() {
        return has_sync;
    }

    public void setHas_sync(Boolean has_sync) {
        this.has_sync = has_sync;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.card_record_id);
        dest.writeString(this.mac_id);
        dest.writeValue(this.school_id);
        dest.writeValue(this.temperature);
        dest.writeValue(this.temp_unit);
        dest.writeLong(this.temp_time);
        dest.writeValue(this.has_sync);
    }

    public TempRecord() {
    }

    protected TempRecord(Parcel in) {
        this.id = in.readString();
        this.card_record_id = in.readString();
        this.mac_id = in.readString();
        this.school_id = (Long) in.readValue(Long.class.getClassLoader());
        this.temperature = (Float) in.readValue(Float.class.getClassLoader());
        this.temp_unit = (Integer) in.readValue(Integer.class.getClassLoader());
        this.temp_time = in.readLong();
        this.has_sync = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    @Generated(hash = 105924705)
    public TempRecord(String id, String card_record_id, String mac_id,
            Long school_id, Float temperature, Integer temp_unit, long temp_time,
            Boolean has_sync) {
        this.id = id;
        this.card_record_id = card_record_id;
        this.mac_id = mac_id;
        this.school_id = school_id;
        this.temperature = temperature;
        this.temp_unit = temp_unit;
        this.temp_time = temp_time;
        this.has_sync = has_sync;
    }

    public static final Creator<TempRecord> CREATOR = new Creator<TempRecord>() {
        @Override
        public TempRecord createFromParcel(Parcel source) {
            return new TempRecord(source);
        }

        @Override
        public TempRecord[] newArray(int size) {
            return new TempRecord[size];
        }
    };
}
