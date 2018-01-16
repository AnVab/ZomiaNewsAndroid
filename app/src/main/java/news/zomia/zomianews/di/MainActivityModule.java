package news.zomia.zomianews.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import news.zomia.zomianews.MainActivity;

/**
 * Created by Andrey on 13.01.2018.
 */

/* Bindings definitions for sub-components so that Dagger can inject them */
@Module
public abstract class MainActivityModule {
    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract MainActivity contributeMainActivity();
}
