package news.zomia.zomianews.fragments;


import android.app.Activity;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import news.zomia.zomianews.Lists.ExpandableListAdapter;
import news.zomia.zomianews.R;
import news.zomia.zomianews.data.model.FeedStoriesCount;
import news.zomia.zomianews.data.model.Tag;
import news.zomia.zomianews.data.model.TagFeedPair;
import news.zomia.zomianews.di.Injectable;
import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.service.ZomiaService;
import news.zomia.zomianews.data.viewmodel.FeedViewModel;
import news.zomia.zomianews.data.viewmodel.FeedViewModelFactory;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedsListFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        LifecycleRegistryOwner,
        Injectable {

    private ZomiaService zomiaService;
    private View rootView;

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Inject
    FeedViewModelFactory feedViewModelFactory;

    private FeedViewModel feedViewModel;

    SwipeRefreshLayout swipeRefreshLayout;
    List<String> tagList;
    Map<String, List<Feed>> feedsCollection;
    private Map<Integer, Integer> feedsStoriesCountMap;
    List<Feed> childList;
    ExpandableListView expListView;
    ExpandableListAdapter expListAdapter;

    OnFeedsListListener onFeedsListListenerCallback;

    public FeedsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.layout_feeds_list, container, false);

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

        expListView = (ExpandableListView)  view.findViewById(R.id.feedsExpandableList);

        FloatingActionButton addFeedButton = (FloatingActionButton)view.findViewById(R.id.addFeedButton);
        addFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onFeedsListListenerCallback.onNewFeedAddAction();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        feedViewModel = ViewModelProviders.of(getActivity(), feedViewModelFactory).get(FeedViewModel.class);

        tagList = new ArrayList<String>();
        feedsCollection = new LinkedHashMap<String, List<Feed>>();
        feedsStoriesCountMap = new HashMap<Integer, Integer>();

        expListAdapter = new ExpandableListAdapter(getActivity(), tagList, feedsCollection, feedsStoriesCountMap);
        expListView.setAdapter(expListAdapter);

        //Load feed channels. Update expandable list data.
        feedViewModel.getFeeds().observe(this, resource -> {
            // update UI
            if (resource != null) {

                if(resource.data != null && resource.data.size() > 0) {
                    //For now do nothing
                }

                swipeRefreshLayout.setRefreshing(false);
            }
            else
                swipeRefreshLayout.setRefreshing(false);
        });

        //Load feeds with tags. Update expandable list data.
        feedViewModel.getFeedsWithTags().observe(this, resource -> {
            // update UI
            if (resource != null && resource.data != null) {

                //Add map with tag - feed list pairs
                feedsCollection.clear();
                for(TagFeedPair tagFeedPair: resource.data)
                {
                    //Check if the map has the tag
                    List<Feed> feedList = feedsCollection.get(tagFeedPair.tag.getName());
                    if (feedList != null) {
                        feedList.add(tagFeedPair.feed);
                    } else {
                        // Key might be present
                        if (feedsCollection.containsKey(tagFeedPair.tag.getName())) {
                            // Okay, there's a key but the value is null
                            feedList = new ArrayList<Feed>();
                            feedList.add(tagFeedPair.feed);
                            feedsCollection.put(tagFeedPair.tag.getName(),feedList);
                        } else {
                            // Definitely no such key
                            feedList = new ArrayList<Feed>();
                            feedList.add(tagFeedPair.feed);
                            feedsCollection.put(tagFeedPair.tag.getName(),feedList);
                        }
                    }
                }

                if(expListAdapter != null)
                    expListAdapter.notifyDataSetChanged();

                swipeRefreshLayout.setRefreshing(false);
            }
            else
                swipeRefreshLayout.setRefreshing(false);
        });

        //Load feeds with no tag. Update expandable list data.
        feedViewModel.getFeedsWithNoTag().observe(this, resource -> {
            // update UI
            if (resource != null && resource.data != null) {

                //Check if the map has the tag
                List<Feed> feedList = feedsCollection.get("Undecided");
                if (feedList != null) {
                    feedList.clear();
                    feedList.addAll(resource.data);
                } else {
                    // Key might be present
                    if (feedsCollection.containsKey("Undecided")) {
                        // Okay, there's a key but the value is null
                        feedList = new ArrayList<Feed>();
                        feedList.addAll(resource.data);
                        feedsCollection.put("Undecided",feedList);
                    } else {
                        // Definitely no such key
                        feedList = new ArrayList<Feed>();
                        feedList.addAll(resource.data);
                        feedsCollection.put("Undecided",feedList);
                    }
                }

                if(expListAdapter != null)
                    expListAdapter.notifyDataSetChanged();

                swipeRefreshLayout.setRefreshing(false);
            }
            else
                swipeRefreshLayout.setRefreshing(false);
        });

        //Load tags. Update expandable list data.
        feedViewModel.getTags().observe(this, resource -> {
            // update UI
            if (resource != null && resource.data != null) {

                //Add tags
                tagList.clear();
                //Add default group for feeds with no tags
                tagList.add("Undecided");

                //Add tags
                for(Tag tag: resource.data)
                {
                    tagList.add(tag.getName());
                }

                //Update list
                if(expListAdapter != null)
                    expListAdapter.notifyDataSetChanged();

                swipeRefreshLayout.setRefreshing(false);
            }
            else
                swipeRefreshLayout.setRefreshing(false);
        });

        //Set item onclick listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                final Feed selectedFeed = (Feed) expListAdapter.getChild(groupPosition, childPosition);

                Toast.makeText(getActivity(), selectedFeed.getTitle(), Toast.LENGTH_LONG)
                        .show();

                onFeedsListListenerCallback.onFeedSelected(selectedFeed);

                return true;
            }
        });

        //Load available stories count for all feed channels. Update expandable list data.
        feedViewModel.getFeedStoriesCount().observe(this, resource -> {
            // update UI
            if (resource != null && resource.data != null) {
                //Add stories count to show on the list
                feedsStoriesCountMap.clear();
                for(FeedStoriesCount count: resource.data) {
                    feedsStoriesCountMap.put(count.getFeedId(), count.getStoriesCountTotal());
                }

                expListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        feedViewModel.refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = getActivity();

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        if(activity != null) {
            try {
                onFeedsListListenerCallback = (OnFeedsListListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnFeedsListListener");
            }
        }
    }

    // Container Activity must implement this interface
    public interface OnFeedsListListener {
        public void onFeedSelected(Feed feed);
        public void onNewFeedAddAction();
    }


    @Override
    public void onResume() {
        super.onResume();

        if(feedViewModel != null)
            feedViewModel.refresh();

        //Update list
        if(expListAdapter != null)
            expListAdapter.notifyDataSetChanged();
    }
}
