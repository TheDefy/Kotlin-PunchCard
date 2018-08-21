package com.bbtree.cardreader.entity.eventbus;

import java.io.Serializable;

/**
 * Function:当次网络质量检测结果
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/09/17
 * Create Time: 下午3:14
 */
public class NetworkQualityResult implements Serializable {

    private int networkResult;

    public int getNetworkResult() {
        return networkResult;
    }

    public void setNetworkResult(int networkResult) {
        this.networkResult = networkResult;
    }

    public NetworkQualityResult(int networkResult) {
        this.networkResult = networkResult;
    }
}
