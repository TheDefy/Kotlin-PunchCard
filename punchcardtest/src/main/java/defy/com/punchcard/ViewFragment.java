package defy.com.punchcard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.bbtree.baselib.base.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import defy.com.punchcard.service.TestService;
import defy.com.punchcard.widget.MyView;

/**
 * Created by chenglei on 2017/9/21.
 */

public class ViewFragment extends BaseFragment {

    @Nullable
    @BindView(R.id.my_view)
    MyView myView;

    private Unbinder bind;

//    private TestService testService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(mContext, TestService.class);
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//            testService = ((TestService.TestBinder) iBinder).getTestService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    public int contentView() {
        return R.layout.my_view_fragment;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        bind = ButterKnife.bind(this, getContentView());
    }

    @OnClick(R.id.bt_get_service_data)
    void showToast() {
//        if (testService==null) return;
//        String dataStr = testService.getDataStr();
//        Toast.makeText(mContext, dataStr, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        bind.unbind();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mContext.unbindService(mServiceConnection);
        super.onDestroy();
    }
}
