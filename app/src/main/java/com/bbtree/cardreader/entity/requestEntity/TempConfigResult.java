package com.bbtree.cardreader.entity.requestEntity;

import com.bbtree.cardreader.entity.BaseEntity;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/10/30
 * Create Time: 下午7:58
 */
public class TempConfigResult extends BaseEntity {

    private boolean isOpen;

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}
