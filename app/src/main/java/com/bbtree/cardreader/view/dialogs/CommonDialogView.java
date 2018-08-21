package com.bbtree.cardreader.view.dialogs;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.R;
import com.bbtree.cardreader.utils.ScreenUtils;

/**
 * 公共的dialog view
 */
public class CommonDialogView extends Dialog {

    private TextView tv_title;

    private LinearLayout ll_buttons;

    private String mTitle;

    private String[] buttonStr;

    private View.OnClickListener listener;

    public CommonDialogView(Context mContext, String mTitle, View.OnClickListener listener, String... strings) {
        super(mContext, R.style.ClassSpeakerNoNetDialog);
        this.mTitle = mTitle;
        this.listener = listener;
        this.buttonStr = strings;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_dialog_view);
        setCanceledOnTouchOutside(false);
        init();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void init() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText(mTitle);
        ll_buttons = (LinearLayout) findViewById(R.id.ll_buttons);
        if (buttonStr.length > 0) {
            Button button;
            LinearLayout.LayoutParams buttonParams;
            for (int i = 0; i < buttonStr.length; i++) {
                buttonParams = new LinearLayout.LayoutParams(ScreenUtils.px2dp(BBTreeApp.getApp().getResources().getDimension(R.dimen.class_speaker_setting5_dialog_button_width)),
                        ScreenUtils.px2dp(BBTreeApp.getApp().getResources().getDimension(R.dimen.class_speaker_setting1_button_height)));
                if (i != buttonStr.length - 1) {
                    buttonParams.setMargins(0, 0, ScreenUtils.dp2px(BBTreeApp.getApp().getResources().getDimension(R.dimen.class_speaker_setting1_button_radius)), 0);
                }
                button = new Button(BBTreeApp.getApp());
                button.setText(buttonStr[i]);
                button.setTag(i);
//                button.setTextAppearance(BBTreeApp.getApp(),R.style.TextAppearance.FontPath.Normal);
                button.setBackground(BBTreeApp.getApp().getResources().getDrawable(R.drawable.class_speaker_setting1_button_yes_shape));
                button.setOnClickListener(listener);
                button.setTextColor(BBTreeApp.getApp().getResources().getColor(R.color.white));
                button.setTextSize(ScreenUtils.sp2px(BBTreeApp.getApp(), ScreenUtils.sp2px(BBTreeApp.getApp(), BBTreeApp.getApp().getResources().getDimension(R.dimen.class_speaker_setting1_content_text_size))));
                ll_buttons.addView(button, buttonParams);
            }
        }
    }
}