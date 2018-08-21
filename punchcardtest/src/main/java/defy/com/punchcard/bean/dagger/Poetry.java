package defy.com.punchcard.bean.dagger;

import javax.inject.Inject;

/**
 * Created by chenglei on 2017/12/4.
 */

public class Poetry {

    private String mPemo;

    @Inject
    public Poetry() {
        this.mPemo = "生活就像海洋1";
    }

    public Poetry(String mPemo) {
        this.mPemo = mPemo;
    }

    public Poetry(int mPemo) {
        this.mPemo = String.valueOf(mPemo);
    }

    public String getmPemo() {
        return mPemo;
    }
}
