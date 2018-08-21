package com.bbtree.cardreader.view.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

/**
 * Created by chenglei on 2017/4/26.
 */

public class DialogUtil {

    private static DialogUtil instance = null;

    public static DialogUtil getInstance() {
        synchronized (DialogUtil.class) {
            if (instance == null) {
                instance = new DialogUtil();
            }
        }
        return instance;
    }

    private Dialog dialog;

    /**
     * 显示一个或两个button的dialog
     *
     * @param mContext
     * @param title    标题
     * @param listener
     * @param strings  button的文本内容
     */
    public void showOneOrTwoButtonDialog(Context mContext, String title, View.OnClickListener listener, String... strings) {
        dialog = new CommonDialogView(mContext, title, listener, strings);
        dialog.show();
    }

    /**
     * 取消dialog
     */
    public void dismiss() {
        if (null != dialog && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
