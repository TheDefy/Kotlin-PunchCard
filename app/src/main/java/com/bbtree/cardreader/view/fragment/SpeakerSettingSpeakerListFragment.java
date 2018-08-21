package com.bbtree.cardreader.view.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bbtree.baselib.base.BaseFragment;
import com.bbtree.baselib.net.BaseParam;
import com.bbtree.cardreader.R;
import com.bbtree.cardreader.adapter.SpeakerListAdapter;
import com.bbtree.cardreader.common.Code;
import com.bbtree.cardreader.entity.requestEntity.SpeakerBean;
import com.bbtree.cardreader.entity.requestEntity.SpeakerConfigRequest;
import com.bbtree.cardreader.entity.requestEntity.SpeakerConfigResult;
import com.bbtree.cardreader.entity.requestEntity.SpeakerDeleteRequest;
import com.bbtree.cardreader.entity.requestEntity.SpeakerDeleteResult;
import com.bbtree.cardreader.model.SpeakerModule;
import com.bbtree.cardreader.view.dialogs.DialogUtil;
import com.bbtree.cardreader.utils.SPUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 分班播报配置-音响列表
 */
public class SpeakerSettingSpeakerListFragment extends BaseFragment {

    @BindView(R.id.tv_none)
    TextView tv_none;
    @BindView(R.id.lv_speakers)
    ListView lv_speakers;
    @BindView(R.id.bt_add)
    Button bt_add;

    private final static String speakerSettingFragmentOneTag = "speakerSettingFragmentOneTag";
    private SpeakerSettingFragmentOne speakerSettingFragmentOne;

    private SpeakerListAdapter mAdapter;


    @Override
    public int contentView() {
        return R.layout.fragment_speaker_setting_speaker_list;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        ButterKnife.bind(this, getContentView());
        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    default:
                    case R.id.bt_add://添加新音箱
                        addOrResetSpeaker(new SpeakerBean());
                        break;
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getAllSpeakers();
    }

    /**
     * 获取音响配置
     */
    private void getAllSpeakers() {
        showProgress("", getResources().getString(R.string.speaker_please_wait));
        SpeakerConfigRequest request = new SpeakerConfigRequest();
        request.setDeviceId(BaseParam.getDeviceId());
        request.setSchoolId(SPUtils.getSchoolId(0L));
        request.setSn("");
        SpeakerModule.getInstance().getSpeakerConfig(request).doOnNext(new Consumer<SpeakerConfigResult>() {
            @Override
            public void accept(SpeakerConfigResult speakerConfigResult) throws Exception {
                SpeakerModule.getInstance().saveSpeakerConfigToDB(speakerConfigResult);
            }
        }).subscribe(new Observer<SpeakerConfigResult>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(SpeakerConfigResult value) {
                hideProgress();
                if (value.getCode() == Code.SUCCESS) {
                    List<SpeakerBean> speakers = value.getSpeakers();
                    showListData(speakers);
                } else {
                    showTipDialog();
                }
            }

            @Override
            public void onError(Throwable e) {
                hideProgress();
                showTipDialog();
            }

            @Override
            public void onComplete() {
            }
        });
    }

    /**
     * 更新界面
     *
     * @param speakers
     */
    private void showListData(List<SpeakerBean> speakers) {
        if (speakers.size() > 0) {
            tv_none.setVisibility(View.GONE);
            lv_speakers.setVisibility(View.VISIBLE);
            if (null == mAdapter) {
                mAdapter = new SpeakerListAdapter(mContext, speakers);
                mAdapter.setOnClickListener(adapterClick);
                lv_speakers.setAdapter(mAdapter);
            } else {
                mAdapter.refreshData(speakers);
            }
        }
    }

    private SpeakerListAdapter.ClickListener adapterClick = new SpeakerListAdapter.ClickListener() {
        @Override
        public void deleteSpeaker(final int position, final List<SpeakerBean> mSpeakerList) {
            showProgress("", mContext.getResources().getString(R.string.speaker_please_wait));
            SpeakerBean item = (SpeakerBean) mAdapter.getItem(position);

            SpeakerDeleteRequest request = new SpeakerDeleteRequest();
            request.setId(item.getId());
            request.setSchoolId((int) SPUtils.getSchoolId(0L));
            SpeakerModule.getInstance().speakerDelete(new SpeakerDeleteRequest()).subscribe(new Observer<SpeakerDeleteResult>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(SpeakerDeleteResult value) {
                    hideProgress();
                    if (value.getCode() == Code.SUCCESS) {
                        Toast toast = Toast.makeText(mContext, R.string.delete_success, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        mSpeakerList.remove(position);
                        mAdapter.refreshData(mSpeakerList);
                        if (mSpeakerList.size() == 0) {
                            tv_none.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onError(Throwable e) {
                    hideProgress();
                    Toast toast = Toast.makeText(mContext, R.string.delete_fail, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

                @Override
                public void onComplete() {

                }
            });

        }

        @Override
        public void startSetFragment(int position) {
            SpeakerBean item = (SpeakerBean) mAdapter.getItem(position);
            addOrResetSpeaker(item);
        }
    };

    /**
     * 添加或配置音箱
     *
     * @param mSpeakerBean
     */
    private void addOrResetSpeaker(SpeakerBean mSpeakerBean) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = getFragmentManager().findFragmentByTag(speakerSettingFragmentOneTag);
        if (fragment == null) {
            if (speakerSettingFragmentOne == null) {
                speakerSettingFragmentOne = new SpeakerSettingFragmentOne();
                speakerSettingFragmentOne.setmSpeakerBean(mSpeakerBean);
            }
            transaction.add(R.id.fl_content, speakerSettingFragmentOne, speakerSettingFragmentOneTag);
        } else {
            ((SpeakerSettingFragmentOne) fragment).setmSpeakerBean(mSpeakerBean);
            transaction.show(fragment);
        }
        transaction.commitAllowingStateLoss();
    }

    private void showTipDialog() {
        if (null == getActivity()) {
            return;
        }
        DialogUtil.getInstance().showOneOrTwoButtonDialog(getActivity(), getResources().getString(R.string.speaker_setting5_dialog_title),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch ((int) v.getTag()) {
                            default:
                            case 0://重试
                                getAllSpeakers();
                                DialogUtil.getInstance().dismiss();
                                break;
                        }
                    }
                }, getResources().getString(R.string.speaker_setting5_button_again));
    }

}
