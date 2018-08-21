package com.bbtree.baselib.net;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.bbtree.baselib.base.BaseApp;
import com.orhanobut.logger.Logger;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by zhouyl on 08/03/2017.
 */

public class AliOSSClient {
    private static final String BucketName = "card-system";
    private static final String AccessKeyID = "LTAIMVhWYtxTfESX";
    private static final String AccessKeySecret = "lW7Oj6qy8OiexUzPX6DFwfz14wI2tj";
    private OSS oss;
    private static String HOST = "oss-cn-hangzhou.aliyuncs.com";



    private static final String BucketName_HN = "card-system-hn";
    private static String HOST_HUANAN = "oss-cn-shenzhen.aliyuncs.com";
    private OSS oss_huanan;
    private ObjectMetadata metadata;

    public AliOSSClient() {

//        if (BaseApp.getInstance().isDebug()) {
//            Observable.create(new ObservableOnSubscribe<String>() {
//                @Override
//                public void subscribe(ObservableEmitter<String> observableEmitter) throws Exception {
//                    try {
//                        InetAddress name = InetAddress.getByName("card-system.oss-cn-hangzhou.aliyuncs.com");
//                        String hostAddress = name.getHostAddress();
//                        observableEmitter.onNext(hostAddress);
//                    } catch (UnknownHostException e) {
//                        e.printStackTrace();
//                    }
//                }
//            })
//                    .subscribeOn(Schedulers.io())
//                    .subscribeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new DefaultObserver<String>() {
//                        @Override
//                        public void onError(Throwable e) {
//
//                        }
//
//                        @Override
//                        public void onComplete() {
//
//                        }
//
//                        @Override
//                        public void onNext(String s) {
//                            if (!TextUtils.isEmpty(s)) {
//                                AliOSSClient.HOST = s + File.separator + BucketName;
//                                metadata = new ObjectMetadata();
//                                metadata.setHeader("host", "card-system.oss-cn-hangzhou.aliyuncs.com");
//                                creatOss();
//                            }
//                        }
//                    });
//        }
        creatOss();
    }

    private void creatOss() {
        OSSCredentialProvider provider = new OSSPlainTextAKSKCredentialProvider(AccessKeyID, AccessKeySecret);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(3); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
//        if (BaseApp.getInstance().isDebug()) OSSLog.enableLog();
        oss = new OSSClient(BaseApp.getMContext(), HOST, provider, conf);

        oss_huanan = new OSSClient(BaseApp.getMContext(), HOST_HUANAN, provider, conf);
    }


    public Observable<String> upload(final String serverPath, final String localPath) {

        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> subscriber) throws Exception {

                PutObjectRequest request = new PutObjectRequest(BucketName, serverPath, localPath);
                if (metadata != null) {
                    request.setObjectKey(BucketName + "/" + serverPath);
                    request.setMetadata(metadata);
                }
                try {
                    Logger.i(oss.toString() +"---"+request.toString());
                    if (!subscriber.isDisposed()) {
                        PutObjectResult putResult = oss.putObject(request);
                        subscriber.onNext("http://" + "cs.bbtree.com" + File.separator + request.getObjectKey());
                        subscriber.onComplete();
                    }
                } catch (ClientException e) {
                    subscriber.onComplete();

//                    subscriber.onError(e);
                    e.printStackTrace();
                } catch (ServiceException e) {
                    subscriber.onComplete();

//                    subscriber.onError(e);
                    e.printStackTrace();
                }
            }
        });
    }

    public Observable<String> upload_huaNan(final String serverPath, final String localPath) {

        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> subscriber) throws Exception {

                PutObjectRequest request = new PutObjectRequest(BucketName_HN, serverPath, localPath);
                if (metadata != null) {
                    request.setObjectKey(BucketName + "/" + serverPath);
                    request.setMetadata(metadata);
                }
                try {
                    Logger.i(oss_huanan.toString() +"---"+request.toString());
                    if (!subscriber.isDisposed()) {
                        PutObjectResult putResult = oss_huanan.putObject(request);
                        subscriber.onNext("http://" + "cs.bbtree.com" + File.separator + request.getObjectKey());
                        subscriber.onComplete();
                    }
                } catch (ClientException e) {
                    subscriber.onComplete();

//                    subscriber.onError(e);
                    e.printStackTrace();
                } catch (ServiceException e) {
                    subscriber.onComplete();

//                    subscriber.onError(e);
                    e.printStackTrace();
                }
            }
        });
    }
}
