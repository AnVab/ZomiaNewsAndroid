package news.zomia.zomianews.fragments;


import android.app.Activity;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private Map<String, List<Feed>> feedsCollection;

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

        setHasOptionsMenu(true);

        expListView = (ExpandableListView)  view.findViewById(R.id.feedsExpandableList);

        FloatingActionButton addFeedButton = (FloatingActionButton)view.findViewById(R.id.addFeedButton);
        addFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onFeedsListListenerCallback.onNewFeedAddAction();
            }
        });


        feedsCollection = new LinkedHashMap<String, List<Feed>>();

        expListAdapter = new ExpandableListAdapter(getActivity());
        expListView.setAdapter(expListAdapter);

        //Set item onclick listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                final Feed selectedFeed = (Feed) expListAdapter.getChild(groupPosition, childPosition);

                Toast.makeText(getActivity(), selectedFeed.getTitle(), Toast.LENGTH_SHORT)
                        .show();

                onFeedsListListenerCallback.onFeedSelected(selectedFeed);

                return true;
            }
        });
    }

    SearchView.OnQueryTextListener feedTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if(query.length() < 3 && expListAdapter != null){
                expListAdapter.filterData(query);
                return false;
            }
            else
                return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if(expListAdapter != null) {
                expListAdapter.filterData(newText);

                //Expand results
                if(newText.length() > 0 && expListAdapter.getGroupCount() > 0) {
                    for(int i = 0; i < expListAdapter.getGroupCount(); i++)
                        expListView.expandGroup(i, false);
                }
                else
                {
                     if(expListAdapter.getGroupCount() > 0) {
                         //Expand undecided tags group
                         expListView.expandGroup(0, false);
                         //Collapse all other tags
                         for(int i = 1; i < expListAdapter.getGroupCount(); i++)
                             expListView.collapseGroup(i);
                     }
                }
            }
            return false;
        }
    };

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (menu.findItem(R.id.menu_search) != null)
            menu.findItem(R.id.menu_search).setVisible(true);

        if (menu.findItem(R.id.menu_refresh) != null)
            menu.findItem(R.id.menu_refresh).setVisible(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Get the SearchView on the app bar
        SearchView filterFeedsSearchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        if (filterFeedsSearchView != null) {
            //SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
            //filterFeedsSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            filterFeedsSearchView.setOnQueryTextListener(feedTextListener);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        feedViewModel = ViewModelProviders.of(getActivity(), feedViewModelFactory).get(FeedViewModel.class);
    }

    @Override
    public void onStart() {
        super.onStart();

        //Load feed channels. Update expandable list data.
        feedViewModel.getFeeds().observe(this, this::onGetFeeds);

        //Load feeds with tags. Update expandable list data.
        feedViewModel.getFeedsWithTags().observe(this, this::onGetFeedsWithTags);

        //Load feeds with no tag. Update expandable list data.
        feedViewModel.getFeedsWithNoTag().observe(this, this::onGetFeedsWithNoTag);

        //Load tags. Update expandable list data.
        feedViewModel.getTags().observe(this, this::onGetTags);
        //Load available stories count for all feed channels. Update expandable list data.
        feedViewModel.getFeedStoriesCount().observe(this, this::onGetFeedStoriesCount);
    }

    @Override
    public void onStop() {
        //Unsubscribe all livedata observers
        feedViewModel.getFeeds().removeObservers(this);
        feedViewModel.getFeedsWithTags().removeObservers(this);
        feedViewModel.getFeedsWithNoTag().removeObservers(this);
        feedViewModel.getTags().removeObservers(this);
        feedViewModel.getFeedStoriesCount().removeObservers(this);

        super.onStop();
    }

    private void onGetFeeds(Resource<List<Feed>> resource) {
        // update UI
        if (resource != null) {

            if(resource.data != null && resource.data.size() > 0) {
                //For now do nothing
            }

            swipeRefreshLayout.setRefreshing(false);
        }
        else
            swipeRefreshLayout.setRefreshing(false);
    }

    private void onGetFeedsWithTags(Resource<List<TagFeedPair>> resource) {
        // update UI
        if (resource != null && resource.data != null) {

            //Add map with tag - feed list pairs
            //feedsCollection.clear();
            //Clear old lists
            for(TagFeedPair tagFeedPair: resource.data)
            {
                List<Feed> feedList = feedsCollection.get(tagFeedPair.tag.getName());
                if (feedList != null) {
                    feedList.clear();
                }
            }

            //Add new tags and lists
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
                expListAdapter.replaceFeedsCollection(feedsCollection);

            swipeRefreshLayout.setRefreshing(false);
        }
        else
            swipeRefreshLayout.setRefreshing(false);
    }

    private void onGetFeedsWithNoTag(Resource<List<Feed>> resource) {
        // update UI
        if (resource != null && resource.data != null) {

            //Check if the map has the tag
            List<Feed> feedList = feedsCollection.get(getString(R.string.tag_undecided));
            if (feedList != null) {
                feedList.clear();
                feedList.addAll(resource.data);
            } else {
                // Key might be present
                if (feedsCollection.containsKey(getString(R.string.tag_undecided))) {
                    // Okay, there's a key but the value is null
                    feedList = new ArrayList<Feed>();
                    feedList.addAll(resource.data);
                    feedsCollection.put(getString(R.string.tag_undecided),feedList);
                } else {
                    // Definitely no such key
                    feedList = new ArrayList<Feed>();
                    feedList.addAll(resource.data);
                    feedsCollection.put(getString(R.string.tag_undecided),feedList);
                }
            }

            if(expListAdapter != null)
                expListAdapter.replaceFeedsCollection(feedsCollection);

            //Expand list
            if(expListAdapter.getGroupCount() > 0)
                expListView.expandGroup(0,false);

            swipeRefreshLayout.setRefreshing(false);
        }
        else
            swipeRefreshLayout.setRefreshing(false);
    }

    private void onGetTags(Resource<List<Tag>> resource) {
        // update UI
        if (resource != null && resource.data != null) {

            //Add tags
            List<String> tagList = new ArrayList<String>();
            //Add default group for feeds with no tags
            tagList.add(getString(R.string.tag_undecided));

            //Add tags
            for(Tag tag: resource.data)
            {
                tagList.add(tag.getName());
            }

            //Update list
            if(expListAdapter != null)
                expListAdapter.replaceTagsList(tagList);

            swipeRefreshLayout.setRefreshing(false);
        }
        else
            swipeRefreshLayout.setRefreshing(false);
    }

    private void onGetFeedStoriesCount(Resource<List<FeedStoriesCount>> resource) {
        // update UI
        if (resource != null && resource.data != null) {
            //Add stories count to show on the list
            Map<Integer, Integer> feedsStoriesCountMap = new HashMap<Integer, Integer>();

            for(FeedStoriesCount count: resource.data) {
                feedsStoriesCountMap.put(count.getFeedId(), count.getStoriesCountTotal());
            }

            expListAdapter.replaceFeedsStoriesCountMap(feedsStoriesCountMap);
        }
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
    }
}
