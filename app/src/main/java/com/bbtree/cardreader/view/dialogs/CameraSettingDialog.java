package com.bbtree.cardreader.view.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.entity.eventbus.CameraDegressEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * 调整摄像头角度
 */
public class CameraSettingDialog extends AlertDialog {

    private Context context;
    private String[] degreesItems = new String[]{"顺时针旋转90度", "顺时针旋转180度", "顺时针旋转270度"};

    public CameraSettingDialog(Context context) {
        super(context);
        this.context = context;
    }

    public void build() {
        Builder builder = new Builder(context);
        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        })
                .setPositiveButton("重置", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BBTreeApp.getApp().setDegrees(0);
                        EventBus.getDefault().post(new CameraDegressEvent(0));
                    }
                })
                .setTitle("摄像头顺时针旋转角度\n设置完成需要重启机器！😊")
                .setItems(degreesItems, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("CameraSettingDialog", degreesItems[which]);
                        switch (which) {
                            case 0:
                                BBTreeApp.getApp().setDegrees(90);
                                EventBus.getDefault().post(new CameraDegressEvent(90));
                                break;
                            case 1:
                                BBTreeApp.getApp().setDegrees(180);
                                EventBus.getDefault().post(new CameraDegressEvent(180));
                                break;
                            case 2:
                                BBTreeApp.getApp().setDegrees(270);
                                EventBus.getDefault().post(new CameraDegressEvent(270));
                                break;
                            default:
                                break;
                        }
                    }
                })
                .show();
    }
}
