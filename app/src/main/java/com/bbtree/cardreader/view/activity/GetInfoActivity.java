package com.bbtree.cardreader.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.bbtree.cardreader.model.CardRecordModule;

/**
 * Created by zhouyl on 14/07/2017.
 */

public class GetInfoActivity extends Activity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CardRecordModule.getInstance().getAllDBInfo();
    }
}
