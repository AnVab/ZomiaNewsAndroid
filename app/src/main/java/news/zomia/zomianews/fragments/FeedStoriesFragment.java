package news.zomia.zomianews.fragments;

import android.app.Activity;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import javax.inject.Inject;

import news.zomia.zomianews.Lists.storyadapter.StoriesAdapter;
import news.zomia.zomianews.R;
import news.zomia.zomianews.customcontrols.RecyclerViewTouchListener;
import news.zomia.zomianews.data.model.Story;
import news.zomia.zomianews.data.util.ListItemClickListener;
import news.zomia.zomianews.data.viewmodel.FeedViewModel;
import news.zomia.zomianews.data.viewmodel.FeedViewModelFactory;
import news.zomia.zomianews.data.viewmodel.StoryViewModel;
import news.zomia.zomianews.data.viewmodel.StoryViewModelFactory;
import news.zomia.zomianews.di.Injectable;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedStoriesFragment extends Fragment implements
        ListItemClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        LifecycleRegistryOwner,
        Injectable {

    private static final String TAG = "ZomiaFStoriesFragment";
    private View rootView;

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Inject
    FeedViewModelFactory feedViewModelFactory;
    public FeedViewModel feedViewModel;

    @Inject
    StoryViewModelFactory storyViewModelFactory;
    public StoryViewModel storyViewModel;

    SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView storiesListView;
    private StoriesAdapter storiesAdapter;

    OnStorySelectedListener onStorySelectedListenerCallback;
    Toolbar toolbar;
    LinearLayoutManager llm;
    public FeedStoriesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.layout_feed_stories, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        boolean showArrow = true;
        boolean showBurger = true;
        Bundle arguments = getArguments();
        if(arguments != null) {
            showArrow = arguments.getBoolean("showArrow", false);
            showBurger = arguments.getBoolean("showBurger", false);
        }

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar_feed_stories_fragment);
        toolbar.setBackground(getContext().getResources().getDrawable(R.drawable.action_bar_color));
        if(showArrow) {
            toolbar.setNavigationIcon(R.drawable.ic_action_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        }
        //Add menu for the toolbar
        toolbar.inflateMenu(R.menu.stories_list_action_menu);
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener);

        if(!showBurger){
            toolbar.getMenu().findItem(R.id.action_settings).setVisible(false);
            toolbar.getMenu().findItem(R.id.logout).setVisible(false);
        }

        return rootView;
    }

    Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch(item.getItemId()){
                case R.id.menu_refresh:
                    onRefresh();
                    return true;

                case android.R.id.home:
                    getActivity().onBackPressed();
                    return true;

                case R.id.action_settings:
                    onStorySelectedListenerCallback.onSettings();
                    return true;

                case R.id.logout:
                    onStorySelectedListenerCallback.onLogOut();
                    return true;
            }
            return true;
        }
    };

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Stories list
        storiesListView = (RecyclerView) view.findViewById(R.id.storiesListView);
        storiesListView.setItemAnimator(null);
        //((DefaultItemAnimator) storiesListView.getItemAnimator()).setSupportsChangeAnimations(false);
        //storiesListView.setItemAnimator(new DefaultItemAnimator());

        llm = new LinearLayoutManager(getActivity());
        storiesListView.setLayoutManager(llm);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                llm.getOrientation());
        dividerItemDecoration.setDrawable(getContext().getResources().getDrawable(R.drawable.stories_list_divider));
        storiesListView.addItemDecoration(dividerItemDecoration);


        storiesListView.addOnItemTouchListener(new RecyclerViewTouchListener(getContext(),
                storiesListView,
                new RecyclerViewTouchListener.OnTouchActionListener() {
                    @Override
                    public void onLeftSwipe(View view, int position) {
                    }

                    @Override
                    public void onRightSwipe(View view, int position) {
                        getActivity().onBackPressed();
                    }

                    @Override
                    public void onClick(View view, int position) {
                        storyViewModel.setCurrentStoryPosition(position);
                        Story selectedStory = (Story) storiesAdapter.getStory(position);
                        onStorySelectedListenerCallback.onStorySelected(selectedStory);
                    }
                }));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        feedViewModel = ViewModelProviders.of(getActivity(), feedViewModelFactory).get(FeedViewModel.class);

        //storiesAdapter = new StoriesAdapter(getActivity(), this,this);
        storiesAdapter = new StoriesAdapter(getActivity());
        storiesAdapter.setHasStableIds(true);
        //Register observer to check if data added to the top of the list then scroll to the new data
        storiesAdapter.registerAdapterDataObserver(adapterDataObserver);

        storiesListView.setAdapter(storiesAdapter);

        storyViewModel = ViewModelProviders.of(getActivity(), storyViewModelFactory).get(StoryViewModel.class);

        storyViewModel.getStories().observe(this, resource -> {
            // update UI
            storiesAdapter.submitList(resource);
        });

        storyViewModel.networkState.observe(this, networkState -> {
            //Add a view to the end of list to show loading status
            //storiesAdapter.setNetworkState(networkState);

            //Update pull-to-swipe indicator
            if(networkState != null) {
                switch (networkState.getStatus()) {
                    case LOADING:
                        swipeRefreshLayout.setRefreshing(true);
                        break;
                    case ERROR:
                        swipeRefreshLayout.setRefreshing(false);
                        //Show error message
                        Toast.makeText(getActivity(), networkState.getMsg(), Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case SUCCESS:
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                }
            }
        });

        feedViewModel.getSelectedFeedId().observe(this, resource -> {
            // update UI
            storyViewModel.setFeedId(resource.getFeedId());
            toolbar.setTitle(resource.getTitle());
        });

    }

    RecyclerView.AdapterDataObserver adapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount)
        {
            if(positionStart == 0)
            {
                llm.scrollToPosition(positionStart);
            }
        }
    };
    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(((AppCompatActivity) getActivity()).getSupportActionBar() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        //Set story as read when we return back from story viewer by the back button press
        if(storyViewModel != null)
            storyViewModel.setCurrentStoryAsRead();

        //storyViewModel.getCurrentStoryListPosition().observe(this, this::ongetCurrentStory);
    }

    @Override
    public void onStop() {
        //Unsubscribe all livedata observers
        //storyViewModel.getCurrentStoryListPosition().removeObservers(this);
        super.onStop();
    }

    /*private void ongetCurrentStory(Integer position) {
        // Update the UI.
        if (position != null) {
            llm.scrollToPositionWithOffset( position, 0);
        }
    }*/

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        storyViewModel.refresh();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = getActivity();

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        if(activity != null) {
            try {
                onStorySelectedListenerCallback = (OnStorySelectedListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnStorySelectedListener");
            }
        }
    }

    @Override
    public void onRetryClick(View view, int position) {
        storyViewModel.refresh();
    }

    // Container Activity must implement this interface
    public interface OnStorySelectedListener {
        public void onStorySelected(Story story);
        public void onSettings();
        public void onLogOut();
    }
}
