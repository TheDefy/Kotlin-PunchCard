package com.bbtree.cardreader.view.fragment;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.bbtree.baselib.base.BaseFragment;
import com.bbtree.cardreader.R;
import com.bbtree.cardreader.entity.requestEntity.SpeakerBean;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 分班播报配置1-长按音响设置提示进入"配置模式"
 */
public class SpeakerSettingFragmentOne extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.rl_back)
    RelativeLayout rl_back;
    @BindView(R.id.rl_next)
    RelativeLayout rl_next;

    public void setmSpeakerBean(SpeakerBean mSpeakerBean) {
        this.mSpeakerBean = mSpeakerBean;
    }

    private SpeakerBean mSpeakerBean;

    @Override
    public int contentView() {
        return R.layout.fragment_speaker_setting1;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        ButterKnife.bind(this, getContentView());
        rl_back.setOnClickListener(this);
        rl_next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_back:
                startSettingSpeakerList();
                break;
            case R.id.rl_next:
                startSetting2();
                break;
        }
    }

    private void startSettingSpeakerList() {
        SpeakerSettingSpeakerListFragment fragment = new SpeakerSettingSpeakerListFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_content, fragment);
        transaction.commit();
    }

    private void startSetting2() {
        SpeakerSettingFragmentTwo fragment = new SpeakerSettingFragmentTwo();
        fragment.setmSpeakerBean(mSpeakerBean);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_content, fragment);
        transaction.commit();
    }
}
