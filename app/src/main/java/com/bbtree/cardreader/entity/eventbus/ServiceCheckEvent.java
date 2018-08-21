package com.bbtree.cardreader.entity.eventbus;

/**
 * Created by bbtree on 2017/3/20.
 */

public class ServiceCheckEvent {
    private boolean isNewVersion;

    public boolean isNewVersion() {
        return isNewVersion;
    }

    public void setNewVersion(boolean newVersion) {
        isNewVersion = newVersion;
    }

    public ServiceCheckEvent(boolean newVersion){
        this.isNewVersion = newVersion;
    }
}
