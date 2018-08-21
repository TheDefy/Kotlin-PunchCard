package com.bbtree.baselib.net;

/**
 * Created by zzz on 11/16/15.
 */
public class ResultObject {
    private String message;
    private int code;
    private Object object;


    public ResultObject() {
        super();
    }
    public ResultObject(String message, int code) {
        super();
        this.message = message;
        this.code = code;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
