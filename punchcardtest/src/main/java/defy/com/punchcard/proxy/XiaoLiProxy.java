package defy.com.punchcard.proxy;

import android.util.Log;

public class XiaoLiProxy implements ISell{

    private ISell iSell;

    public XiaoLiProxy(ISell mianMo) {
        this.iSell = mianMo;
    }

    @Override
    public void sell() {
        Log.e("888888", "我是代理商小李，我在做微商");
        iSell.sell();
    }
}
