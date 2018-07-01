package news.zomia.zomianews.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import news.zomia.zomianews.fragments.FeedStoriesFragment;
import news.zomia.zomianews.fragments.FeedsListFragment;
import news.zomia.zomianews.fragments.LoginFragment;
import news.zomia.zomianews.fragments.NewFeedFragment;
import news.zomia.zomianews.fragments.StoryViewerFragment;
import news.zomia.zomianews.fragments.ViewPagerFragment;

/**
 * Created by Andrey on 13.01.2018.
 */

@Module
public abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract FeedsListFragment contributeFeedsListFragment();

    @ContributesAndroidInjector
    abstract FeedStoriesFragment contributeFeedStoriesFragment();

    @ContributesAndroidInjector
    abstract LoginFragment contributeLoginFragment();

    @ContributesAndroidInjector
    abstract NewFeedFragment contributeNewFeedFragment();

    @ContributesAndroidInjector
    abstract StoryViewerFragment contributeStoryViewerFragment();

    @ContributesAndroidInjector
    abstract ViewPagerFragment contributeViewPagerFragment();
}