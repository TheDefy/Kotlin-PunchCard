package com.bbtree.baselib.net;

import com.bbtree.baselib.crypto.AESUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by zzz on 16/01/2017.
 */

public class HttpClient {

    private final OkHttpClient client;
    private Gson gson;
    private static final String param1 = "base";
    private static final String param2 = "param";
    static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private String imei;
    private String deviceId;
    private volatile String secretKey = "0c631d046fff4d3ab4d5d8f4d9aae848";
    private int mTerminalTyp = 1;

    public HttpClient(/*BaseInfos mInfos*/) {
        super();
        gson = new GsonBuilder().serializeNulls().create();
        client = new OkHttpClient.Builder()
//                .addInterceptor(new LoggingInterceptor())

                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        imei = BaseParam.getImei();
        deviceId = BaseParam.getInstance().getDeviceId();
        mTerminalTyp = BaseParam.getMachineType();
    }


    public String postEntity(String url, Object entity) throws IOException {
        if (url.contains("machine/register")) {
            secretKey = "0c631d046fff4d3ab4d5d8f4d9aae848";
        }
        String paramString = gson.toJson(entity);

        RequestParam requestParam = new RequestParam();
        requestParam.setData(encrypt(paramString, secretKey));
        requestParam.setDeviceId(deviceId);
        requestParam.setImei(imei);
        requestParam.setTerminalType(mTerminalTyp);
        mTerminalTyp = BaseParam.getMachineType();

        String body = gson.toJson(requestParam);

        Logger.t(url + " request json").i(body);
        URL myurl = new URL(url);
        Request request = new Request.Builder()
                .removeHeader("User-Agent")
                .addHeader("User-Agent", "BBTreeCardDevice")
                .url(myurl)
                .post(RequestBody.create(MEDIA_TYPE_JSON, body))
                .build();

        Response response = client.newCall(request).execute();
        String result = response.body().string();
        String jsonStr = decrypt(result);
        Logger.t(url + " Response json").i(jsonStr);
        return jsonStr;
    }


    public String postMap(String url, Map map) throws IOException {
        String paramString = gson.toJson(map);
        RequestParam requestParam = new RequestParam();
        requestParam.setData(encrypt(paramString, secretKey));
        requestParam.setDeviceId(deviceId);
        requestParam.setImei(imei);

        String body = gson.toJson(requestParam);
        Logger.t(url + " request json").i(body);
        URL myurl = new URL(url);
        Request request = new Request.Builder()
                .removeHeader("User-Agent")
                .addHeader("User-Agent", "BBTreeCardDevice")
                .url(myurl)
                .post(RequestBody.create(MEDIA_TYPE_JSON, body))
                .build();

        Response response = client.newCall(request).execute();
        String jsonStr = decrypt(response.body().string());
        Logger.t(url + " Response json").i(jsonStr);
        return jsonStr;
    }

    public String postMapNoCrypt(String url, Map map) throws IOException {
        String body = gson.toJson(map);
        Logger.t(url + " request json").i(body);
        URL myurl = new URL(url);
        Request request = new Request.Builder()
                .removeHeader("User-Agent")
                .addHeader("User-Agent", "BBTreeCardDevice")
                .url(myurl)
                .post(RequestBody.create(MEDIA_TYPE_JSON, body))
                .build();

        Response response = client.newCall(request).execute();
        String jsonStr = response.body().string();
        Logger.t(url + " Response json").i(jsonStr);
        return jsonStr;
    }


    private String encrypt(String body, String secret) {
        try {
            return AESUtil.encrypt(body, secret.substring(0, secret.length() - 16), secret.substring(secret.length() - 16, secret.length()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private String decrypt(String body) {
        return AESUtil.decrypt(body, secretKey.substring(0, secretKey.length() - 16), secretKey.substring(secretKey.length() - 16, secretKey.length()));
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
