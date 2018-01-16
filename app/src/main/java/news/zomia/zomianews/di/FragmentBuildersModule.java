package news.zomia.zomianews.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import news.zomia.zomianews.fragments.FeedStoriesFragment;
import news.zomia.zomianews.fragments.FeedsListFragment;

/**
 * Created by Andrey on 13.01.2018.
 */

@Module
public abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract FeedsListFragment contributeFeedsListFragment();

    @ContributesAndroidInjector
    abstract FeedStoriesFragment contributeFeedStoriesFragment();
}