package com.bbtree.cardreader.entity.dao;

import android.os.Parcel;
import android.os.Parcelable;

import com.bbtree.cardreader.entity.Family;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Transient;

import java.util.ArrayList;
import java.util.List;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by qiujj on 2017/3/23.
 */

@Entity
public class CardInfo implements Parcelable {
    @SerializedName("macId")
    @Id
    private String id;
    private String name;
    private String familyName;
    private String alias;
    private String avatar;
    private Integer level;
    @SerializedName("isVip")
    private Boolean isVip;
    private String birthday;
    @SerializedName("classId")
    private Integer classId;
    @SerializedName("flowerCount")
    private Integer flowerScore;
    @Transient
    private List<Family> family;
    private String familyString;
    @SerializedName("className")
    private String className;
    @SerializedName("userType")
    private Integer userType;
    @SerializedName("userId")
    private Integer userId;
    @SerializedName("cardLevel")
    private Integer cardLevel;
    @SerializedName("cardNo")
    private String cardNumber;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Boolean getVip() {
        return isVip;
    }

    public void setVip(Boolean vip) {
        isVip = vip;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public Integer getFlowerScore() {
        return flowerScore;
    }

    public void setFlowerScore(Integer flowerScore) {
        this.flowerScore = flowerScore;
    }

    public List<Family> getFamily() {
        return family;
    }

    public void setFamily(List<Family> family) {
        this.family = family;
    }

    public String getFamilyString() {
        return familyString;
    }

    public void setFamilyString(String familyString) {
        this.familyString = familyString;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCardLevel() {
        return cardLevel;
    }

    public void setCardLevel(Integer cardLevel) {
        this.cardLevel = cardLevel;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.familyName);
        dest.writeString(this.alias);
        dest.writeString(this.avatar);
        dest.writeValue(this.level);
        dest.writeValue(this.isVip);
        dest.writeString(this.birthday);
        dest.writeValue(this.classId);
        dest.writeValue(this.flowerScore);
        dest.writeList(this.family);
        dest.writeString(this.familyString);
        dest.writeString(this.className);
        dest.writeValue(this.userType);
        dest.writeValue(this.userId);
        dest.writeValue(this.cardLevel);
        dest.writeString(this.cardNumber);
    }

    public Boolean getIsVip() {
        return this.isVip;
    }

    public void setIsVip(Boolean isVip) {
        this.isVip = isVip;
    }

    public CardInfo() {
    }

    protected CardInfo(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.familyName = in.readString();
        this.alias = in.readString();
        this.avatar = in.readString();
        this.level = (Integer) in.readValue(Integer.class.getClassLoader());
        this.isVip = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.birthday = in.readString();
        this.classId = (Integer) in.readValue(Integer.class.getClassLoader());
        this.flowerScore = (Integer) in.readValue(Integer.class.getClassLoader());
        this.family = new ArrayList<Family>();
        in.readList(this.family, Family.class.getClassLoader());
        this.familyString = in.readString();
        this.className = in.readString();
        this.userType = (Integer) in.readValue(Integer.class.getClassLoader());
        this.userId = (Integer) in.readValue(Integer.class.getClassLoader());
        this.cardLevel = (Integer) in.readValue(Integer.class.getClassLoader());
        this.cardNumber = in.readString();
    }

    @Keep
    public CardInfo(String id, String name, String familyName, String alias,
            String avatar, Integer level, Boolean isVip, String birthday,
            Integer classId, Integer flowerScore, String familyString,
            String className, Integer userType, Integer userId, Integer cardLevel,
            String cardNumber) {
        this.id = id;
        this.name = name;
        this.familyName = familyName;
        this.alias = alias;
        this.avatar = avatar;
        this.level = level;
        this.isVip = isVip;
        this.birthday = birthday;
        this.classId = classId;
        this.flowerScore = flowerScore;
        this.familyString = familyString;
        this.className = className;
        this.userType = userType;
        this.userId = userId;
        this.cardLevel = cardLevel;
        this.cardNumber = cardNumber;
    }

    public static final Creator<CardInfo> CREATOR = new Creator<CardInfo>() {
        @Override
        public CardInfo createFromParcel(Parcel source) {
            return new CardInfo(source);
        }

        @Override
        public CardInfo[] newArray(int size) {
            return new CardInfo[size];
        }
    };
}
