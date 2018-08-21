package defy.com.punchcard.bean.dagger;

import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by chenglei on 2017/12/5.
 */

@Module
public class ApplicationModule {

    @Singleton
    @Provides
    public Gson providerGson(){
        return new Gson();
    }
}
