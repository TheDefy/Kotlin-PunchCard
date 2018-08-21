package com.bbtree.baselib.net;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Created by zzz on 11/16/15.
 */
public class GsonParser {
    private final static int SUCCESS = 200;
    private static Gson gson;
    private String setSecretKey;

    public static void main(String[] args) {

        String s = "{\"code\":200,\"message\":null,\"cardInfo\":null}";
    }

    static {
        gson = new Gson();
    }

    public static <T> T parse2Entity(String response, Class<T> clz) {
        if (TextUtils.isEmpty(response)) {
            //TODO
//            return getROwithNull();
            return null;
        } else {
            T o = (T) gson.fromJson(response, clz);
            return o;
        }
    }

    public static ResultObject parseNoClz(String response) {
        if (TextUtils.isEmpty(response)) {
            //TODO
            return getROwithNull();
        } else {
            Map map = gson.fromJson(response, Map.class);
            Integer code = ((Double) map.get("code")).intValue();
            String message = (String) map.get("message");
            checkToken(map);
            if (code != SUCCESS) {
                return getErrorRO(code, message);
            } else {
                ResultObject ro = new ResultObject();
                ro.setMessage(message);
                ro.setCode(code);
                return ro;
            }
        }
    }

    /**
     * 用于解析 data 里面只有一个 key value 的情况。 不用定义 Bean 就可以解析数据。
     *
     * @param response
     * @param key
     * @return
     */
    public static ResultObject parseObject(String response, String key) {
        if (TextUtils.isEmpty(response)) {
            return getROwithNull();
        }
        Map map = gson.fromJson(response, Map.class);
        Integer code = ((Double) map.get("code")).intValue();
        String message = (String) map.get("message");
        checkToken(map);

        if (code != SUCCESS) {
            return getErrorRO(code, message);
        } else {
            ResultObject ro = new ResultObject();
            ro.setMessage(message);
            ro.setObject(map.get(key));
            ro.setCode(code);
            return ro;
        }
    }


    /**
     * 将结果直接处理成 map 返回，适用于没有定义 Bean 的情况。直接拿着 map 操作
     *
     * @param response
     * @return
     */
    public static ResultObject parse2Map(String response) {
        if (TextUtils.isEmpty(response)) {
            return getROwithNull();
        }
        Map map = gson.fromJson(response, Map.class);
        if (map == null)
            return getROwithNull();
        Integer code = ((Double) map.get("code")).intValue();
        String message = (String) map.get("message");
        checkToken(map);
        if (code != SUCCESS) {
            return getErrorRO(code, message);
        } else {
            ResultObject ro = new ResultObject();
            //Map<String, Object> data = (Map<String, Object>) map.get("data");
            ro.setCode(code);
            ro.setObject(map);
            return ro;
        }
    }


    /**
     * 用于解析data 中为 jsonArray 的情况，
     *
     * @param response
     * @param classOfT 需要转成的对象实体，可以直接 new T();
     * @param <T>      返回的对象 T 泛型
     * @return ResultObject
     */
    public static <T> ResultObject parse2List(String response, String key, final Class<T[]> classOfT) {
        if (TextUtils.isEmpty(response)) {
            return getROwithNull();
        }
        Map map = gson.fromJson(response, Map.class);
        if (map == null)
            return getROwithNull();
        Integer code = ((Double) map.get("code")).intValue();
        String message = (String) map.get("message");
        checkToken(map);
        if (code != SUCCESS) {
            return getErrorRO(code, message);
        } else {
            ResultObject ro = new ResultObject();
//            List lists = (List) map.get(key);
//            List<T> list = (List<T>) map.get(key);
//            T t = gson.fromJson(gson.toJson(map.get(key)), new TypeToken<List<T>>() {
//            }.getType());

            String s = gson.toJson(map.get(key));
            T[] arr = new Gson().fromJson(s, classOfT);
            ro.setObject(Arrays.asList(arr));
            ro.setCode(code);
            return ro;
        }
    }

    /**
     * parser model to json
     *
     * @param object
     * @return
     */
    public static String parserToJson(Object object) {
        if (object != null) {
            return gson.toJson(object);
        }

        return null;
    }

    public static <T> List<T> jsonToList(String json, Class<T> classOfT) {
        Type type = new TypeToken<ArrayList<JsonObject>>() {
        }.getType();
        ArrayList<JsonObject> jsonObjects = gson.fromJson(json, type);

        List<T> list = new ArrayList<T>();
        for (JsonObject object : jsonObjects) {
            list.add(gson.fromJson(object, classOfT));
        }

        return list;
    }


    private static ResultObject getErrorRO(int code, String msg) {
        ResultObject ro = new ResultObject();
        ro.setCode(code);
        ro.setMessage(msg);
        return ro;
    }

    private static ResultObject getROwithNull() {
        ResultObject ro = new ResultObject();
        ro.setCode(-1);
        ro.setMessage("网络错误");
        return ro;
    }

    private static void checkToken(Map map) {

    }

    private void setSecretKey(String setSecretKey) {
        this.setSecretKey = setSecretKey;
    }

}
