package news.zomia.zomianews.fragments;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import news.zomia.zomianews.R;
import news.zomia.zomianews.customcontrols.DirectionalViewPager;
import news.zomia.zomianews.di.Injectable;

public class ViewPagerFragment  extends Fragment implements
        LifecycleRegistryOwner,
        Injectable {
    public static String ON_ROTATION_CURRENT_PAGE;

    public static final int NUM_PAGES = 3;
    public static final int FEEDS_LIST_PAGE_NUM = 0;
    public static final int FEED_STORIES_PAGE_NUM = 1;
    public static final int STORY_VIEWER_PAGE_NUM = 2;
    private DirectionalViewPager viewPager;
    private PagerAdapter pagerAdapter;

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    public ViewPagerFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_view_pager, container, false);

        viewPager = (DirectionalViewPager) view.findViewById(R.id.viewPager);
        pagerAdapter = new NewsSliderPageAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(NUM_PAGES);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                ((NewsSliderPageAdapter)pagerAdapter).updateCount(position + 1);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if(savedInstanceState != null){
            setCurrentPage(savedInstanceState.getInt(ON_ROTATION_CURRENT_PAGE));
        }

        return view;
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(ON_ROTATION_CURRENT_PAGE, getCurrentPage());
        super.onSaveInstanceState(savedInstanceState);
    }

    public void setCurrentPage(int num)
    {
        if(viewPager != null && num < NUM_PAGES) {
            ((NewsSliderPageAdapter)pagerAdapter).updateCount(num + 1);
            viewPager.setCurrentItem(num);
        }
    }

    public int getCurrentPage()
    {
        if(viewPager != null)
            return viewPager.getCurrentItem();
        else
            return 0;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }


    private class NewsSliderPageAdapter extends FragmentStatePagerAdapter implements DirectionalViewPager.DirectionProvider {

        private int count = 1;
        public NewsSliderPageAdapter(FragmentManager fm) {
            super(fm);
        }

        public void updateCount(int num)
        {
            count = num;
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {

            if(getCount() <= position)
                position = getCount() - 1;
            switch(position)
            {
                case FEEDS_LIST_PAGE_NUM:
                    return new FeedsListFragment();

                case FEED_STORIES_PAGE_NUM:
                    return new FeedStoriesFragment();

                case STORY_VIEWER_PAGE_NUM:
                    return new StoryViewerFragment();

                default:
                    return new FeedsListFragment();
            }
        }

        @Override
        public int getCount() {
            return count;
        }
        @Override
        public int getScrollDirections(int position) {
            if (position == 0) {
                return DirectionalViewPager.DIRECTION_NONE;
            }
            if (position == 1) {
                return DirectionalViewPager.DIRECTION_LEFT;
            }
            if (position == 2) {
                return DirectionalViewPager.DIRECTION_LEFT;
            }

            return DirectionalViewPager.DIRECTION_BOTH;
        }
    }
}
