package defy.com.punchcard.bean.dagger;

import dagger.Component;
import defy.com.punchcard.App;
import defy.com.punchcard.Dagger2Fragment;
import defy.com.punchcard.OtherActivity;

/**
 * Created by chenglei on 2017/12/4.
 */
@PoetryScope
@Component(dependencies = ApplicationComponent.class,
        modules = {GsonModule.class, PoetryModule.class})
public abstract class Dagger2FragmentComponent {

    public abstract void inject(Dagger2Fragment fragment);

    public abstract void inject(OtherActivity activity);

    private static Dagger2FragmentComponent instance;

    public static Dagger2FragmentComponent getInstance() {
        if (instance == null) {
            instance = DaggerDagger2FragmentComponent.builder()
                    .applicationComponent(App.getApp().getApplicationComponent())
                    .build();
        }
        return instance;
    }
}
