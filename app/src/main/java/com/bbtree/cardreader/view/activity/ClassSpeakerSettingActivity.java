package com.bbtree.cardreader.view.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bbtree.baselib.base.BaseActivity;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.R;
import com.bbtree.cardreader.service.CardListenerService;
import com.bbtree.cardreader.view.fragment.SpeakerSettingCheckStatusFragment;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * 分班播报设置界面
 */
public class ClassSpeakerSettingActivity extends BaseActivity {

    @BindView(R.id.tv_tip)
    TextView tv_tip;
    @BindView(R.id.rl_title)
    RelativeLayout rl_title;
    @BindView(R.id.fl_content)
    FrameLayout fl_content;
    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.rl_back)
    RelativeLayout rl_back;

    private final static String speakerSettingCheckStatusTag = "speakerSettingCheckStatusTag";

    private SpeakerSettingCheckStatusFragment speakerSettingCheckStatusFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        setContentView(R.layout.activity_classspeakersetting);
        ButterKnife.bind(this);
        initViewListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initFragment();
    }

    private void initViewListener() {//初始化view的监听事件
        iv_close.setOnClickListener(onClick);
    }

    private void initFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = getFragmentManager().findFragmentByTag(speakerSettingCheckStatusTag);
        if (fragment == null) {
            if (speakerSettingCheckStatusFragment == null) {
                speakerSettingCheckStatusFragment = new SpeakerSettingCheckStatusFragment();
            }
            transaction.add(R.id.fl_content, speakerSettingCheckStatusFragment, speakerSettingCheckStatusTag);
        } else {
            transaction.show(fragment);
        }
        transaction.commitAllowingStateLoss();
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                default:
                case R.id.iv_close:
                    showTipAndRestart();
                    break;
            }
        }
    };

    /**
     * 配置完成之后重启
     */
    private void showTipAndRestart() {
        if (CardListenerService.outputStream != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.activity_left_in, R.anim.activity_left_out);
            return;
        }

        tv_tip.setText(R.string.sender_checked);
        tv_tip.setVisibility(View.VISIBLE);
        rl_title.setVisibility(View.GONE);
        fl_content.setVisibility(View.GONE);
        rl_back.setBackgroundColor(getResources().getColor(R.color.green));

        Observable.timer(3, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                BBTreeApp.getApp().restartApp(500);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showTipAndRestart();
    }
}
