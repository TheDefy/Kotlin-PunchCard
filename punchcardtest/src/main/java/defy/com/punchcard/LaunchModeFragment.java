package defy.com.punchcard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bbtree.baselib.base.BaseFragment;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by chenglei on 2017/9/21.
 */

public class LaunchModeFragment extends BaseFragment {

    private Unbinder bind;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int contentView() {
        return R.layout.launch_mode_fragment;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        bind = ButterKnife.bind(this, getContentView());
    }

    @OnClick({R.id.bt_first_start, R.id.bt_reset_start})
    void startActivity(View view) {
        switch (view.getId()) {
            case R.id.bt_first_start:

                Intent intent = new Intent(getActivity(), LaunchModeActivity.class);
                intent.putExtra("tag", "bt_first_start");
                startActivity(intent);
                break;
            case R.id.bt_reset_start:
                Intent intent1 = new Intent(getActivity(), LaunchModeActivity.class);
                intent1.putExtra("tag", "bt_reset_start");
                startActivity(intent1);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        bind.unbind();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
