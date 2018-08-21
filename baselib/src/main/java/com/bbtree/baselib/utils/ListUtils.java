package com.bbtree.baselib.utils;

import java.util.List;

/**
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2015/06/09
 * Time: 下午4:50
 */
public class ListUtils {
    /**
     * 判断长度是否为0或空
     *
     * @param list
     * @return
     */
    public static boolean isZero(List list) {
        if (list == null || list.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }
}
