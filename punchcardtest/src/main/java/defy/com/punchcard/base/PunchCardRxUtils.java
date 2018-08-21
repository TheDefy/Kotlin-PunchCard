package defy.com.punchcard.base;


import com.bbtree.baselib.net.GsonParser;
import com.bbtree.baselib.net.ResultObject;

import java.io.IOException;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by zzz on 16/01/2017.
 */

public class PunchCardRxUtils {

    static PunchCardHttpClient client = new PunchCardHttpClient();

    /**
     * 入参为 map 的请求。
     *
     * @param path
     * @param map
     * @return
     */
    public static Observable<String> postMap(final String path, final Map map) {
        return Observable.just(path)
                .subscribeOn(Schedulers.io())
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        try {
                            return client.postMap(path, map);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return "";
                    }
                });
    }


    /**
     * 入参为 EntityBean 形式的请求
     *
     * @param path
     * @param o
     * @return
     */
    public static Observable<String> postEntity(final String path, final Object o) {
        return Observable.just(path)
                .subscribeOn(Schedulers.io())
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        try {
                            return client.postEntity(path, o);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return "";
                    }
                });
    }

    /**
     * 对应 GsonParser.parseObject
     *
     * @param clz
     * @return
     */
    public static <T> Function<String, T> getObject(final Class<T> clz) {
        return new Function<String, T>() {
            @Override
            public T apply(String s) {
                return (T) GsonParser.parse2Entity(s, clz);
            }
        };
    }

    /**
     * 对应 GsonParser.parseObject
     *
     * @param key
     * @return
     */
    public static Function<String, ResultObject> getObject(final String key) {
        return new Function<String, ResultObject>() {
            @Override
            public ResultObject apply(String s) {
                return GsonParser.parseObject(s, key);
            }
        };
    }

    /**
     * 对应 GsonParser.parseObject
     *
     * @return
     */
    public static Function<String, ResultObject> getMap() {
        return new Function<String, ResultObject>() {
            @Override
            public ResultObject apply(String s) {
                return GsonParser.parse2Map(s);
            }
        };
    }

    /**
     * 对应 GsonParser.parseList
     *
     * @param t
     * @return
     */
    public static <T> Function<String, ResultObject> getList(final String key, final Class<T[]> t) {
        return new Function<String, ResultObject>() {
            @Override
            public ResultObject apply(String s) {
                return GsonParser.parse2List(s, key, t);
            }
        };
    }

    /**
     * 对应 GsonParser.parseObject
     *
     * @return
     */
    public static Function<String, ResultObject> getNoClz() {
        return new Function<String, ResultObject>() {
            @Override
            public ResultObject apply(String s) {
                return GsonParser.parseNoClz(s);
            }
        };
    }

    public static void setSecretKey(String secretKey) {
        client.setSecretKey(secretKey);
    }


}
