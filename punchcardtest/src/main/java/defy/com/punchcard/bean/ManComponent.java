package defy.com.punchcard.bean;

import dagger.Component;

/**
 * Created by chenglei on 2017/11/30.
 */

@Component(modules = CarModule.class)
public interface ManComponent {
    void injectMan(Man man);
}
