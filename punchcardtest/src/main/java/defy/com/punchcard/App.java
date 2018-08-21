package defy.com.punchcard;

import android.app.Application;

import defy.com.punchcard.bean.dagger.ApplicationComponent;
import defy.com.punchcard.bean.dagger.DaggerApplicationComponent;

/**
 * Created by chenglei on 2017/12/5.
 */

public class App extends Application {

    private ApplicationComponent applicationComponent;
    private static App mApp;

    public static App getApp(){
        return mApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;

        applicationComponent = DaggerApplicationComponent.builder().build();
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }
}
