package defy.com.punchcard.bean.dagger;

import dagger.Module;
import dagger.Provides;

/**
 * Created by chenglei on 2017/12/4.
 */

@Module
public class PoetryModule {
//    @Provides
//    public Poetry providerIntPoetry(int peo) {
//        return new Poetry(peo);
//    }

    @PoetryScope
    @Provides
    public Poetry providerPoetry(String mPeo) {
        return new Poetry(mPeo);
    }
//
//    @Provides
//    public String providerPoems() {
//        return "只有意志坚强的人，才能到达彼岸";
//    }

//    @Provides
//    public int providerIntPoems() {
//        return 3;
//    }

    @Provides
    public String providerStrPoems(){
        return "参数来自module";
    }
}
