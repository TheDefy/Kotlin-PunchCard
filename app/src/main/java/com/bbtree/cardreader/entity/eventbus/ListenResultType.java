package com.bbtree.cardreader.entity.eventbus;

public class ListenResultType {

    /**
     * 1.接end work 并启用迈得看门狗
     * 2.停迈得看门狗，变UI，开监听
     * 3.restart
     */
    private int resultType;

    public int getResultType() {
        return resultType;
    }

    public void setResultType(int resultType) {
        this.resultType = resultType;
    }

    public ListenResultType(int resultType) {
        this.resultType = resultType;
    }
}
