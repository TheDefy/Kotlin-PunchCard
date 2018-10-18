package defy.com.punchcard.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class DynamicProxy implements InvocationHandler {

    private ISell iSell;

    public DynamicProxy(ISell iSell) {
        this.iSell = iSell;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (iSell instanceof MianMo) {
            return null;
        } else {
            Object result = method.invoke(iSell, args);
            return result;
        }

    }
}
