package defy.com.punchcard.proxy;

import android.util.Log;

public class Shoes implements ISell {

    @Override
    public void sell() {
        Log.e("888888", "::::::卖鞋子!有需要的联系");
    }
}
