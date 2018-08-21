package com.bbtree.cardreader.utils;

import com.orhanobut.logger.Logger;

import static com.bbtree.cardreader.utils.ShellUtils.execCommand;

/**
 * Function:
 *
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2016/08/15
 * Create Time: 下午9:47
 */
public class DeployWatchDog {
    public static boolean deploy(String sourcePath) {
        execCommand("mount -o remount,rw /system", true);
        execCommand("chmod 777 "+sourcePath, true);

        execCommand("rm -rf /system/app/watchdog*", true);
        execCommand("cp -f " + sourcePath + " /system/app/WatchDog.apk", true);
        execCommand("chmod 777 /system/app/WatchDog.apk", true);
        ShellUtils.CommandResult commandResult4 = execCommand("chmod 0644 /system/app/WatchDog.apk", true);
        Logger.d("DeployWatchDog","------install finish");
        return commandResult4.result == 0;
//        ShellUtils.execCommand("reboot", true);
    }
}
