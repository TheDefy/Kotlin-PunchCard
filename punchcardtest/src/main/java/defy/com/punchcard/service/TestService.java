package defy.com.punchcard.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bbtree.baselib.base.AppExecutors;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chenglei on 2017/11/24.
 */

public class TestService extends Service {

    private int count;
    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            AppExecutors.getInstance().mainThread().execute(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(TestService.this, "count:" + count++, Toast.LENGTH_SHORT).show();
                    Log.e("66666", "count:" + count++);
                }
            });
        }
    };

    @Override
    public void onCreate() {
        new Timer().schedule(mTimerTask, 200, 3000);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new TestBinder();
    }

    @Override
    public void onDestroy() {
        mTimerTask.cancel();
        super.onDestroy();
    }

    public String getDataStr() {
        return "hello 您好 这是来自service的info！！！";
    }

    public class TestBinder extends Binder {
        public TestService getTestService() {
            return TestService.this;
        }
    }

}
