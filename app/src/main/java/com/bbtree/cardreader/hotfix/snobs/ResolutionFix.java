package com.bbtree.cardreader.hotfix.snobs;

import com.bbtree.cardreader.utils.ShellUtils;

/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2016/08/22
 * Create Time: 上午10:36
 */
public class ResolutionFix {
    public static void fix() {
        ShellUtils.execCommand("wm size 1280x720", true);//fix display
    }
}
