package defy.com.punchcard;

import android.os.Bundle;
import android.widget.TextView;

import com.bbtree.baselib.base.BaseActivity;
import com.google.gson.Gson;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import defy.com.punchcard.bean.dagger.Dagger2FragmentComponent;
import defy.com.punchcard.bean.dagger.Poetry;

/**
 * Created by chenglei on 2017/12/5.
 */

public class OtherActivity extends BaseActivity {

    @BindView(R.id.tv_content)
    TextView tv_content;

    @Inject
    Gson gson;
    @Inject
    Poetry poetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dagger2_fragment);
        ButterKnife.bind(this);

        Dagger2FragmentComponent.getInstance().inject(this);

        initView();
    }

    private void initView() {

        tv_content.setText(gson.toJson(poetry) + " poetry:" + poetry);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
