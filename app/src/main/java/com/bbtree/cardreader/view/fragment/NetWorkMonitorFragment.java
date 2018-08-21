package com.bbtree.cardreader.view.fragment;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.TextView;

import com.bbtree.baselib.base.BaseFragment;
import com.bbtree.baselib.net.BaseParam;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.R;
import com.bbtree.cardreader.common.Constant;
import com.bbtree.cardreader.common.Urls;
import com.bbtree.cardreader.utils.ReadPhoneInfo;
import com.bbtree.cardreader.utils.SPUtils;
import com.bbtree.cardreader.utils.TracertUtil.NetDiagnoListener;
import com.bbtree.cardreader.utils.TracertUtil.NetDiagnoService;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Function:
 * Created by BBTree Team
 * User: EngrZhou
 * Date: 2016/03/16
 * Time: 下午4:04
 */
public class NetWorkMonitorFragment extends BaseFragment implements NetDiagnoListener {

    @BindView(R.id.network_detail_info)
    TextView network_detail_info;

    private StringBuilder networkResult;
    private NetDiagnoService mNetDiagnoService;

    @Override
    public int contentView() {
        return R.layout.net_monitor;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        ButterKnife.bind(this, getContentView());
        networkResult = new StringBuilder();
        mNetDiagnoService = new NetDiagnoService(BBTreeApp.getApp(),
                ReadPhoneInfo.getAppVersionCode(mContext) + "",
                getString(R.string.app_name),
                ReadPhoneInfo.getAppVersionName(mContext),
                BBTreeApp.getApp().getMachineAlias(),
                BaseParam.getDeviceId(),
                Urls.HOST,
                Constant.NetworkDiagnostic.DOMAIN_NETEASE,
                SPUtils.getSchoolName(""),
                this);
        mNetDiagnoService.setIfUseJNICTrace(true);
        mNetDiagnoService.setIfUseJNICConn(true);
        mNetDiagnoService.execute();
    }

    @Override
    public void OnNetDiagnoFinished(String log) {

    }

    @Override
    public void OnNetDiagnoUpdated(String log) {
        networkResult.append(log);
        network_detail_info.setText(networkResult.toString());
    }

    public void show(FragmentManager fragmentManager) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        boolean add = isAdded();
        if (!add) {
            transaction.add(R.id.floating_layer, this);
        }
        transaction.show(this);
        transaction.commitAllowingStateLoss();
    }

    public void removeSelf(FragmentManager fragmentManager) {
        if (!isAdded()) {
            return;
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(this);
        transaction.commitAllowingStateLoss();
    }
}
