package defy.com.punchcard;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.bbtree.baselib.base.BaseFragment;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import defy.com.punchcard.proxy.DynamicProxy;
import defy.com.punchcard.proxy.ISell;
import defy.com.punchcard.proxy.MianMo;
import defy.com.punchcard.proxy.Shoes;
import defy.com.punchcard.proxy.XiaoLiProxy;

/**
 * Created by chenglei on 2017/9/21.
 */

public class LearnFragment extends BaseFragment {

    @Nullable
    @BindView(R.id.tv_content)
    TextView tv_content;

    private Unbinder bind;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        doReflectMethod();
//        doProxy();

        int[] arrays = new int[]{1, 2, 3, 2, 2, 2};


        doRepeatCount(arrays);

//        doToBinary(1>>>16);
//        doToBinary(5);
//        doToBinary(3);

//        int i = 5 & 5;
//        doToBinary(i);

        getAPPMemory();
    }

    private void getAPPMemory() {
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        int largeMemoryClass = activityManager.getLargeMemoryClass();
        Log.e("888888", "largeMemoryClass:" + largeMemoryClass);

        int memoryClass = activityManager.getMemoryClass();
        Log.e("888888", "memoryClass:" + memoryClass);
    }

    private void doToBinary(int i) {
        Log.e("888888", i + "的二进制:" + Integer.toBinaryString(i));
        Log.e("888888", i + "的长度:" + Integer.toBinaryString(i).length());
    }

    private void doRepeatCount(int[] arrays) {

        Map<Integer, Integer> map = new HashMap<>();
        int i = 0;
        int length = arrays.length;
        while (length-- != 0) {

            Integer count = map.get(arrays[i]);
            if (count == null)
                map.put(arrays[i], 1);
            else
                map.put(arrays[i], count + 1);

            i++;
        }

        Log.e("888888", map.toString());

    }

    private void doProxy() {
        ISell mianMo = new MianMo();
        final ISell shoes = new Shoes();

        InvocationHandler handler1 = new DynamicProxy(mianMo);

        ISell mianMoSell = (ISell) Proxy.newProxyInstance(ISell.class.getClassLoader(), new Class[]{ISell.class}, handler1);
        mianMoSell.sell();

        InvocationHandler handler = new DynamicProxy(shoes);
        ISell shoesSell = (ISell) Proxy.newProxyInstance(ISell.class.getClassLoader(), new Class[]{ISell.class}, handler);
        shoesSell.sell();
    }

    private void doReflectMethod() {
        ISell iSell = new MianMo();
        ISell xiaoLiProxy = new XiaoLiProxy(iSell);
        xiaoLiProxy.sell();

        Class<LearnFragment> learnFragmentClass = LearnFragment.class;
        try {
            Method sum = learnFragmentClass.getDeclaredMethod("sum", new Class[]{int.class, int.class});
            try {
                sum.invoke(null, 1, 2);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private static int sum(int a, int b) {
        Log.e("888888", "sum: " + (a + b));
        return a + b;
    }


    @Override
    public int contentView() {
        return R.layout.learn_fragment;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        bind = ButterKnife.bind(this, getContentView());
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
