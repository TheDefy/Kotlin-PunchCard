package defy.com.punchcard;

import android.os.Bundle;
import android.view.View;

import com.bbtree.baselib.base.BaseFragment;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import defy.com.punchcard.utils.CommDialog;

/**
 * Created by chenglei on 2017/9/21.
 */

public class SeekBarFragment extends BaseFragment {

    private Unbinder bind;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int contentView() {
        return R.layout.item_audio_setting;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        bind = ButterKnife.bind(this, getContentView());
    }

//    @OnClick({R.id.bt_first_start, R.id.bt_reset_start})
//    void startActivity(View view) {
//        switch (view.getId()) {
//            case R.id.bt_first_start:
//
//                Intent intent = new Intent(getActivity(), LaunchModeActivity.class);
//                intent.putExtra("tag", "bt_first_start");
//                startActivity(intent);
//                break;
//            case R.id.bt_reset_start:
//                Intent intent1 = new Intent(getActivity(), LaunchModeActivity.class);
//                intent1.putExtra("tag", "bt_reset_start");
//                startActivity(intent1);
//                break;
//        }
//    }

    @OnClick(R.id.tv_related_class)
    void onClick(View view){
        CommDialog dialogUtils =  new CommDialog(getActivity(), new CommDialog.OnDialogClick() {
            @Override
            public void confirm() {

            }

            @Override
            public void cancel() {

            }

            @Override
            public String content() {
                return null;
            }
        });
        dialogUtils.show(R.layout.dialog_signin);
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
