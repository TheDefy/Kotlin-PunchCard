package com.bbtree.baselib.rxjava;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;


/**
 * Created by zzz on 12/1/15.
 */
public class BroadcastObservable implements ObservableOnSubscribe<Boolean> {

    private final Context context;

    public static Observable<Boolean> fromConnectivityManager(Context context) {
        return Observable.create(new BroadcastObservable(context))
                .share();
    }

    public BroadcastObservable(Context context) {
        this.context = context;
    }

    @Override
    public void subscribe(final ObservableEmitter<Boolean> subscriber) throws Exception {

        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                subscriber.onNext(isConnectedToInternet());
            }
        };
        context.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        subscriber.setDisposable(unsubscribeInUiThread(new Action() {
            @Override
            public void run() {
                context.unregisterReceiver(receiver);
            }
        }));


    }


    private boolean isConnectedToInternet() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    private static Disposable unsubscribeInUiThread(final Action unsubscribe) {
        return Disposables.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    unsubscribe.run();
                } else {
                    final Scheduler.Worker inner = AndroidSchedulers.mainThread().createWorker();
                    inner.schedule(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                unsubscribe.run();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            inner.dispose();
                        }
                    });
                }
            }
        });
    }


}
