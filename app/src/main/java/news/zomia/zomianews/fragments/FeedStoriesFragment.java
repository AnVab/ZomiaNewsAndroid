package news.zomia.zomianews.fragments;

import android.app.Activity;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import news.zomia.zomianews.Lists.storyadapter.StoriesAdapter;
import news.zomia.zomianews.R;
import news.zomia.zomianews.data.model.Story;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.viewmodel.StoryViewModel;
import news.zomia.zomianews.data.viewmodel.StoryViewModelFactory;
import news.zomia.zomianews.di.Injectable;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedStoriesFragment extends Fragment implements
        StoriesAdapter.StoryViewHolder.ClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        LifecycleRegistryOwner,
        Injectable {

    private static final String TAG = "ZomiaFStoriesFragment";
    private View rootView;
    private Integer feedId;

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Inject
    StoryViewModelFactory storyViewModelFactory;
    private StoryViewModel storyViewModel;

    SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView storiesListView;
    private StoriesAdapter storiesAdapter;

    OnStorySelectedListener onStorySelectedListenerCallback;

    public FeedStoriesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.layout_feed_stories, container, false);
        feedId = getArguments().getInt("feedId");


        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        storiesListView = (RecyclerView) view.findViewById(R.id.storiesListView);
        storiesListView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        storiesListView.setLayoutManager(llm);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        storyViewModel = ViewModelProviders.of(getActivity(), storyViewModelFactory).get(StoryViewModel.class);

        storiesAdapter = new StoriesAdapter(getActivity(), this);
        storiesListView.setAdapter(storiesAdapter);

        LiveData<PagedList<Story>> repo = storyViewModel.getStories();
        repo.observe(this, resource -> {

            Log.d("ZOMIA", "UPDATE UI STORIES");
            storiesAdapter.setList(resource);
            /*// update UI
            if (resource != null){// && resource.data != null) {
                storiesAdapter.replace(resource);
                swipeRefreshLayout.setRefreshing(false);
            } else {
                storiesAdapter.replace(Collections.emptyList());
                swipeRefreshLayout.setRefreshing(false);
            }*/
        });

        storyViewModel.setFeedId(feedId);
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onItemClicked(int position) {
        storyViewModel.setCurrentStoryPosition(position);

        Story selectedStory = (Story) storiesAdapter.getStory(position);
        onStorySelectedListenerCallback.onStorySelected(selectedStory);
    }

    @Override
    public boolean onItemLongClicked(int position) {
        //toggleSelection(position);
        return true;
    }

    /**
     * Toggle the selection state of an item.
     *
     * If the item was the last one in the selection and is unselected, the selection is stopped.
     * Note that the selection must already be started (actionMode must not be null).
     *
     * @param position Position of the item to toggle the selection state
     */
    /*private void toggleSelection(int position) {
        storiesAdapter.toggleSelection(position);
    }*/

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

    // Container Activity must implement this interface
    public interface OnStorySelectedListener {
        public void onStorySelected(Story story);
    }
}
