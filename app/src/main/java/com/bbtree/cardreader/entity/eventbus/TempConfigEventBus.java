package com.bbtree.cardreader.entity.eventbus;


import com.bbtree.cardreader.entity.requestEntity.TempConfigResult;

import java.io.Serializable;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/10/31
 * Create Time: 下午4:13
 */
public class TempConfigEventBus implements Serializable {
    public boolean requestSuccess;
    public TempConfigResult result;
}
