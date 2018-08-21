package com.bbtree.cardreader.entity;

/**
 * Created by zhouyl on 13/04/2017.
 */

public abstract class BaseEntity {
    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
