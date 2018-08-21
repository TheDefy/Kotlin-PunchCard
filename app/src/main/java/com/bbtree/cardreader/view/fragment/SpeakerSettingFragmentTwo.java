package com.bbtree.cardreader.view.fragment;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bbtree.baselib.base.BaseFragment;
import com.bbtree.baselib.utils.ListUtils;
import com.bbtree.baselib.utils.StringUtils;
import com.bbtree.cardreader.R;
import com.bbtree.cardreader.entity.ClassSpeakerRequest;
import com.bbtree.cardreader.entity.dao.Speaker;
import com.bbtree.cardreader.entity.eventbus.ClassSpeakerResponse;
import com.bbtree.cardreader.entity.requestEntity.SpeakerBean;
import com.bbtree.cardreader.model.SpeakerModule;
import com.bbtree.cardreader.service.CardListenerService;
import com.bbtree.cardreader.utils.SPUtils;
import com.bbtree.cardreader.utils.SenderPortCheckingUtil;
import com.bbtree.cardreader.view.dialogs.DialogUtil;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 分班播报配置2-输入音响信息
 */
public class SpeakerSettingFragmentTwo extends BaseFragment implements View.OnClickListener {

    private final static String TAG = SpeakerSettingFragmentTwo.class.getSimpleName();

    @BindView(R.id.et_code)
    EditText et_code;
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.rl_back)
    RelativeLayout rl_back;
    @BindView(R.id.rl_next)
    RelativeLayout rl_next;

    public void setmSpeakerBean(SpeakerBean mSpeakerBean) {
        this.mSpeakerBean = mSpeakerBean;
    }

    private SpeakerBean mSpeakerBean;

    /**
     * 定时任务用于显示提示框
     */
    private Timer mTimer;

    private boolean isShowingDialog;

    private ClassSpeakerRequest request;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public int contentView() {
        return R.layout.fragment_speaker_setting2;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        ButterKnife.bind(this, getContentView());
        rl_back.setOnClickListener(this);
        rl_next.setOnClickListener(this);

        initViewData();
    }

    /**
     * 只要输入过音箱号和别名进入时就显示
     */
    private void initViewData() {
        if (!TextUtils.isEmpty(mSpeakerBean.getCode())) {
            et_code.setText(mSpeakerBean.getCode());
        }
        if (!TextUtils.isEmpty(mSpeakerBean.getName())) {
            et_name.setText(mSpeakerBean.getName());
        }
        isShowingDialog = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_back:
                startSetting1();
                break;
            case R.id.rl_next:
                checkInput();
                break;
        }
    }

    private void checkInput() {
        rl_next.setOnClickListener(null);
        showProgress("", getResources().getString(R.string.speaker_please_wait));
        String code = et_code.getText().toString();
        String name = et_name.getText().toString();
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(mContext, R.string.speaker_setting4_nocode, Toast.LENGTH_SHORT).show();
            rl_next.setOnClickListener(this);
            hideProgress();
            return;
        }
        if (TextUtils.isEmpty(name)) {
            rl_next.setOnClickListener(this);
            hideProgress();
            Toast.makeText(mContext, R.string.speaker_setting4_noname, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!StringUtils.matchCharactorOrNumber(code)) {
            rl_next.setOnClickListener(this);
            hideProgress();
            Toast.makeText(mContext, R.string.speaker_setting4_illegalcode, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!StringUtils.matchCharactorOrNumberOrChinese(name)) {
            rl_next.setOnClickListener(this);
            hideProgress();
            Toast.makeText(mContext, R.string.speaker_setting4_illegalname, Toast.LENGTH_SHORT).show();
            return;
        }

        List<Speaker> list = SpeakerModule.getInstance().queryAllSpeakerList();
        if (mSpeakerBean.getId() == 0 && !ListUtils.isZero(list)) {
            for (Speaker speaker : list) {
                if (code.equals(speaker.getCode())) {
                    rl_next.setOnClickListener(this);
                    hideProgress();
                    Toast.makeText(mContext, R.string.speaker_setting4_code_exist, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        mSpeakerBean.setCode(code);
        mSpeakerBean.setName(name);

        sendCMD2Speaker();
        startMyTimerTask();
    }

    /**
     * 1秒后无回应则提示
     */
    private void startMyTimerTask() {
        mTimer = new Timer();
        TimerTask mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (null != getActivity()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showTipDialog();
                        }
                    });
                }
            }
        };
        mTimer.schedule(mTimerTask, 2000);
    }

    private void showTipDialog() {
        rl_next.setOnClickListener(this);
        hideProgress();
        DialogUtil.getInstance().showOneOrTwoButtonDialog(mContext, getResources().getString(R.string.speaker_setting4_dialog_title),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogUtil.getInstance().dismiss();
                        isShowingDialog = false;
                    }
                }, getResources().getString(R.string.IKnowIt));
        isShowingDialog = true;
    }

    private void startSetting1() {
        String code = et_code.getText().toString();
        String name = et_name.getText().toString();
        mSpeakerBean.setCode(code);
        mSpeakerBean.setName(name);

        SpeakerSettingFragmentOne fragment = new SpeakerSettingFragmentOne();
        fragment.setmSpeakerBean(mSpeakerBean);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_content, fragment);
        transaction.commit();
    }

    private void sendCMD2Speaker() {
        // 如果是新音箱
        if (mSpeakerBean.getId() == 0) {
            // 获取到本地所有音箱
            List<Speaker> list = SpeakerModule.getInstance().queryAllSpeakerList();
            int maxNumber = 0;
            if (!ListUtils.isZero(list)) {
                for (Speaker tempSpeaker : list) {
                    if (tempSpeaker.getNumber() > maxNumber) {
                        maxNumber = tempSpeaker.getNumber();
                    }
                }
            }
            mSpeakerBean.setGroupId(1);
            mSpeakerBean.setNum(maxNumber + 1);
        }
        String targetText = String.format(getResources().getString(R.string.speaker_setting4_succees), mSpeakerBean.getCode());
        request = new ClassSpeakerRequest(1, 1, SPUtils.getSpeakerPin(0),
                mSpeakerBean.getGroupId(), mSpeakerBean.getNum(), targetText);

        if (CardListenerService.outputStream != null) {
            try {
                CardListenerService.request = request;
                CardListenerService.outputStream.write(CardListenerService.request.getmCMDBytes());
                Logger.i("播报请求:" + request.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            resetSenderPath();
        }
    }

    private void resetSenderPath() {
        if (SenderPortCheckingUtil.getInstance().mOutputStream == null) {
            return;
        }
        try {
            SenderPortCheckingUtil.getInstance().response = request.getResponse();
            SenderPortCheckingUtil.getInstance().mOutputStream.write(request.getmCMDBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startSetting3() {
        if (isShowingDialog) {
            return;
        }
        hideProgress();
        SpeakerSettingFragmentThree fragment = new SpeakerSettingFragmentThree();
        fragment.setmSpeakerBean(mSpeakerBean);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_content, fragment);
        transaction.commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ClassSpeakerResponse response) {
        Logger.i("response:" + response.getResultCMD());
        if (mTimer != null) {
            mTimer.cancel();
        }
        CardListenerService.request = null;
        startSetting3();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        CardListenerService.request = null;
        EventBus.getDefault().unregister(this);
    }

}
