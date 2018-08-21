package com.bbtree.cardreader.entity.eventbus;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/08/24
 * Create Time: 下午4:48
 */
public class ScreenSaverCMD {
    public ScreenSaverAction cmd;
    public String sn;//命令序号

    public enum ScreenSaverAction {
        screenSaveUpdate,
        adUpdate
    }
}
