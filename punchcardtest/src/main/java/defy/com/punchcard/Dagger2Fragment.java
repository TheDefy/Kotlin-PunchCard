package defy.com.punchcard;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.bbtree.baselib.base.BaseFragment;
import com.google.gson.Gson;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import defy.com.punchcard.bean.dagger.Dagger2FragmentComponent;
import defy.com.punchcard.bean.dagger.Poetry;

/**
 * Created by chenglei on 2017/9/21.
 */

public class Dagger2Fragment extends BaseFragment {

    @Nullable
    @BindView(R.id.tv_content)
    TextView tv_content;

    private Unbinder bind;

    @Inject
    Poetry mPoetry;

    @Inject
    Gson mGson;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Dagger2FragmentComponent.getInstance().inject(this);
    }


    @Override
    public int contentView() {
        return R.layout.dagger2_fragment;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        bind = ButterKnife.bind(this, getContentView());
        String s = mPoetry.getmPemo();
        String s1 = mGson.toJson(mPoetry);
//        tv_content.setText(s1);

        tv_content.setText(mGson.toJson(mPoetry) + " poetry:" + mPoetry);
    }

    @OnClick(R.id.bt_other_activity)
    void goToOtherActivity(){
        Intent intent = new Intent(mContext, OtherActivity.class);
        mContext.startActivity(intent);
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
