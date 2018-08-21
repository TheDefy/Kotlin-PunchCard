package com.bbtree.cardreader.view.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bbtree.baselib.base.BaseFragment;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.R;
import com.bbtree.cardreader.entity.ClassSpeakerRequest;
import com.bbtree.cardreader.entity.eventbus.ClassSpeakerResponse;
import com.bbtree.cardreader.service.CardListenerService;
import com.bbtree.cardreader.utils.NetWorkUtil;
import com.bbtree.cardreader.utils.SPUtils;
import com.bbtree.cardreader.utils.SenderPortCheckingUtil;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 分班播报配置-检查发射模块与网络
 */
public class SpeakerSettingCheckStatusFragment extends BaseFragment implements View.OnClickListener {

    private String TAG = SpeakerSettingCheckStatusFragment.class.getSimpleName();

    @BindView(R.id.iv_send_status)
    ImageView iv_send_status;
    @BindView(R.id.tv_send_content)
    TextView tv_send_content;
    @BindView(R.id.iv_net_status)
    ImageView iv_net_status;
    @BindView(R.id.tv_net_content)
    TextView tv_net_content;
    @BindView(R.id.bt_setting)
    Button bt_setting;

    private final static String speakerSettingSpeakerListTag = "speakerSettingSpeakerListTag";
    private SpeakerSettingSpeakerListFragment speakerSettingSpeakerListFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public int contentView() {
        return R.layout.fragment_speaker_setting_check_status;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        ButterKnife.bind(this, getContentView());
        // 默认都为false
        iv_send_status.setSelected(false);
        iv_net_status.setSelected(false);
        tv_send_content.setText(mContext.getResources().getString(R.string.speaker_setting1_send_status_no));
        tv_net_content.setText(mContext.getResources().getString(R.string.speaker_setting1_net_status_no));
        bt_setting.setSelected(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        checkSenderTask();
        checkNetTask();
    }

    /**
     * 检查发射器状态
     */
    private void checkSenderTask() {
        if (CardListenerService.outputStream != null) {
            try {
                CardListenerService.request = new ClassSpeakerRequest(1, 0);
                CardListenerService.outputStream.write(CardListenerService.request.getmCMDBytes());
                Logger.i("播报请求:" + CardListenerService.request.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            resetSenderPath();
        }

    }

    /**
     * 重置发射器路径并检测
     */
    private void resetSenderPath() {
        BBTreeApp.getApp().disableMDWatchDog();
        SPUtils.setSenderPath("");
        SenderPortCheckingUtil.getInstance();
    }

    /**
     * 检查网络状态
     */
    private void checkNetTask() {
        iv_net_status.setSelected(NetWorkUtil.isNetworkAvailable(mContext));
        tv_net_content.setText(NetWorkUtil.isNetworkAvailable(mContext)
                ? mContext.getResources().getString(R.string.speaker_setting1_net_status_yes)
                : mContext.getResources().getString(R.string.speaker_setting1_net_status_no));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ClassSpeakerResponse response) {
        Logger.t(TAG).i("response:" + response.getResultCMD());
        CardListenerService.request = null;
        updateUI();
    }

    /**
     * 更新UI
     */
    private void updateUI() {
        iv_send_status.setSelected(true);
        tv_send_content.setText(mContext.getResources().getString(R.string.speaker_setting1_send_status_yes));
        bt_setting.setSelected(iv_net_status.isSelected());
        bt_setting.setOnClickListener(iv_net_status.isSelected() ? SpeakerSettingCheckStatusFragment.this : null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_setting:
                startSettingSpeakerList();
                break;
        }
    }

    private void startSettingSpeakerList() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = getFragmentManager().findFragmentByTag(speakerSettingSpeakerListTag);
        if (fragment == null) {
            if (speakerSettingSpeakerListFragment == null) {
                speakerSettingSpeakerListFragment = new SpeakerSettingSpeakerListFragment();
            }
            transaction.add(R.id.fl_content, speakerSettingSpeakerListFragment, speakerSettingSpeakerListTag);
        } else {
            transaction.show(fragment);
        }
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CardListenerService.request = null;
        EventBus.getDefault().unregister(this);
    }

}
