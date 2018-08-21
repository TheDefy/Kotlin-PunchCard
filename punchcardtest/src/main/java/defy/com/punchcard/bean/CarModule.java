package defy.com.punchcard.bean;

import dagger.Module;
import dagger.Provides;

/**
 * Created by chenglei on 2017/11/30.
 */

@Module
public class CarModule {

    @Provides
    static Car providerCar(){
        return new Car();
    }
}
