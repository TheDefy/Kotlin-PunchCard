package com.bbtree.baselib.rxjava;

import android.content.Context;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;


/**
 * Created by zzz on 12/1/15.
 */
public class RetryWithConnectivityIncremental implements Function<Observable<? extends Throwable>,Observable<?>> {
    private final int maxTimeout;
    private final TimeUnit timeUnit;
    private final Observable<Boolean> isConnected;
    private final int startTimeOut;
    private int timeout;

    public RetryWithConnectivityIncremental(Context context, int startTimeOut, int maxTimeout, TimeUnit timeUnit) {
        this.startTimeOut = startTimeOut;
        this.maxTimeout = maxTimeout;
        this.timeUnit = timeUnit;
        this.timeout = startTimeOut;
        isConnected = getConnectedObservable(context);
    }

    @Override
    public Observable<?> apply(Observable<? extends Throwable> observable) throws Exception {
        return null;
    }



    private ObservableTransformer<Boolean, Boolean> attachIncementalTimeout() {
        return new ObservableTransformer<Boolean, Boolean>() {
            @Override
            public Observable<Boolean> apply(Observable<Boolean> observable) {
                return observable.timeout(timeout, timeUnit)
                        .doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                if (throwable instanceof TimeoutException) {
                                    timeout = timeout > maxTimeout ? maxTimeout : timeout + startTimeOut;
                                }
                            }
                        });
            }
        };
    }

    private Observable<Boolean> getConnectedObservable(Context context) {

        return BroadcastObservable.fromConnectivityManager(context)
                .distinctUntilChanged()
                .filter(new Predicate<Boolean>() {
                    @Override
                    public boolean test(Boolean aBoolean) throws Exception {
                        return aBoolean;
                    }
                });

    }


}
