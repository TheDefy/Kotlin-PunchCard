package com.bbtree.cardreader.view.fragment;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bbtree.baselib.base.BaseFragment;
import com.bbtree.cardreader.R;
import com.bbtree.cardreader.common.Code;
import com.bbtree.cardreader.entity.requestEntity.SpeakerBean;
import com.bbtree.cardreader.entity.requestEntity.SpeakerSaveRequest;
import com.bbtree.cardreader.entity.requestEntity.SpeakerSaveResult;
import com.bbtree.cardreader.model.SpeakerModule;
import com.bbtree.cardreader.view.dialogs.DialogUtil;
import com.bbtree.cardreader.utils.NetWorkUtil;
import com.bbtree.cardreader.utils.SPUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


/**
 * 分班播报配置3-完成配置
 */
public class SpeakerSettingFragmentThree extends BaseFragment implements View.OnClickListener{

    @BindView(R.id.rl_next)
    RelativeLayout rl_next;
    @BindView(R.id.rl_back)
    RelativeLayout rl_back;

    public void setmSpeakerBean(SpeakerBean mSpeakerBean) {
        this.mSpeakerBean = mSpeakerBean;
    }

    private SpeakerBean mSpeakerBean;

    @Override
    public int contentView() {
        return R.layout.fragment_speaker_setting3;
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
                startSetting2();
                break;
            case R.id.rl_next:
                rl_next.setOnClickListener(null);
                completeSetting();
                break;
        }
    }

    private void startSetting2() {
        SpeakerSettingFragmentTwo fragment = new SpeakerSettingFragmentTwo();
        fragment.setmSpeakerBean(mSpeakerBean);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_content, fragment);
        transaction.commit();
    }

    private void completeSetting() {
        if (!NetWorkUtil.isNetworkAvailable(mContext)) {
            showTipDialog();
            rl_next.setOnClickListener(this);
            return;
        }
        saveSpeaker();
    }

    private void showTipDialog() {
        DialogUtil.getInstance().showOneOrTwoButtonDialog(mContext, getResources().getString(R.string.speaker_setting5_dialog_title), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch ((int) v.getTag()) {
                            case 0://返回列表
                                startSettingSpeakerList();
                                DialogUtil.getInstance().dismiss();
                                break;
                            case 1://重试
                                completeSetting();
                                DialogUtil.getInstance().dismiss();
                                break;
                        }
                    }
                }, getResources().getString(R.string.speaker_setting5_button_list),
                getResources().getString(R.string.speaker_setting5_button_again));
    }

    /**
     * 保存音响到服务器
     */
    private void saveSpeaker() {
        showProgress("", getResources().getString(R.string.speaker_please_wait));
        SpeakerSaveRequest request = new SpeakerSaveRequest();
        request.setSchoolId(SPUtils.getSchoolId(0L));
        // 音响号
        request.setCode(mSpeakerBean.getCode());
        request.setName(mSpeakerBean.getName());
        request.setIsAll(mSpeakerBean.getIsAll());
        if (mSpeakerBean.getId() != 0) {
            request.setId(String.valueOf(mSpeakerBean.getId()));
        }
        request.setClassIds(mSpeakerBean.getClassIds());
        request.setGroupId(mSpeakerBean.getGroupId());
        request.setNum(mSpeakerBean.getNum());
        SpeakerModule.getInstance().speakerSave(request).subscribe(new Observer<SpeakerSaveResult>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(SpeakerSaveResult value) {
                hideProgress();
                if (value.getCode() == Code.SUCCESS) {
                    Toast.makeText(getActivity(), R.string.speaker_setting5_finish, Toast.LENGTH_SHORT).show();
                    startSettingSpeakerList();
                } else {
                    saveFailed();
                }
            }

            @Override
            public void onError(Throwable e) {
                hideProgress();
                saveFailed();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void startSettingSpeakerList() {
        SpeakerSettingSpeakerListFragment fragment = new SpeakerSettingSpeakerListFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_content, fragment);
        transaction.commit();
    }

    private void saveFailed() {
        if (getActivity() == null) {
            return;
        }
        showTipDialog();
        rl_next.setOnClickListener(SpeakerSettingFragmentThree.this);
    }

}
