package com.bbtree.cardreader.entity.requestEntity;

import com.bbtree.cardreader.entity.BaseEntity;

/**
 * Created by zhouyl on 10/04/2017.
 */

public class SchoolInfo extends BaseEntity {
  private long schoolId;//学校ID
  private String schoolName;//学校名称
  private String schoolCode;
  private int[] rfidProtocolV2;//读头协议第二版本，支持多协议
  private int baudRate;//波特率
  private String cityName;//学校所在城市，用于天气预报

  public long getSchoolId() {
    return schoolId;
  }

  public void setSchoolId(long schoolId) {
    this.schoolId = schoolId;
  }

  public String getSchoolName() {
    return schoolName;
  }

  public void setSchoolName(String schoolName) {
    this.schoolName = schoolName;
  }

  public String getSchoolCode() {
    return schoolCode;
  }

  public void setSchoolCode(String schoolCode) {
    this.schoolCode = schoolCode;
  }

  public int[] getRfidProtocolV2() {
    return rfidProtocolV2;
  }

  public void setRfidProtocolV2(int[] rfidProtocolV2) {
    this.rfidProtocolV2 = rfidProtocolV2;
  }

  public int getBaudRate() {
    return baudRate;
  }

  public void setBaudRate(int baudRate) {
    this.baudRate = baudRate;
  }

  public String getCityName() {
    return cityName;
  }

  public void setCityName(String cityName) {
    this.cityName = cityName;
  }
}
