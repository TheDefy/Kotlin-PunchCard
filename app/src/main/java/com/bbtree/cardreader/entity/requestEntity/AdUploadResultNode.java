package com.bbtree.cardreader.entity.requestEntity;

/**
 * Created by chenglei on 2017/6/8.
 */

public class AdUploadResultNode {
    private String id;// 记录id 1_201701012111 广告ID_时间

    private boolean result;// 执行结果true-成功 false-失败

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
