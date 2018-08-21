package defy.com.punchcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bbtree.baselib.base.BaseActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by chenglei on 2017/12/5.
 */

public class LaunchModeActivity extends BaseActivity {

    private String tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_mode);
        ButterKnife.bind(this);
        Intent intent = this.getIntent();
        tag = intent.getStringExtra("tag");

        Log.e("666666", tag + "onCreate");
    }

    @OnClick(R.id.tv_content)
    void onClick(){
        Intent intent1 = new Intent(this, LaunchModeActivity.class);
        intent1.putExtra("tag", "bt_reset_start2");
        startActivity(intent1);
    }
    @Override
    protected void onStart() {
        super.onStart();

        Log.e("666666", tag + "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.e("666666", tag + "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e("666666", tag + "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.e("666666", tag + "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.e("666666", tag + "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e("666666", tag + "onDestroy");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        tag = intent.getStringExtra("tag");

        Log.e("666666", tag + "onNewIntent");
        Log.e("666666", tag + "onNewIntent" + intent.toString());
    }
}
