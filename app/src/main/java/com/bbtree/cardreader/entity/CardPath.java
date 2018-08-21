package com.bbtree.cardreader.entity;

public class CardPath {

    public long[] cardNo;
    public String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long[] getCardNo() {
        return cardNo;
    }

    public void setCardNo(long[] cardNo) {
        this.cardNo = cardNo;
    }
}
