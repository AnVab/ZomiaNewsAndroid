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

        //Create tags array for a map
        createTagList();

        feedsCollection = new LinkedHashMap<String, List<Feed>>();
        feedsStoriesCountMap = new HashMap<Integer, Integer>();
        /*LiveData<Resource<List<Feed>>> repo = feedViewModel.getFeeds();
        if(repo != null) {
            repo.observe(getActivity(), resource -> {

                if (resource != null) {
                    ShowFeeds(resource.data);
                    swipeRefreshLayout.setRefreshing(false);
                }
                else
                {
                    swipeRefreshLayout.setRefreshing(false);
                }
                    //createCollection(resource.data);

            });
        }*/
        expListAdapter = new ExpandableListAdapter(getActivity(), tagList, feedsCollection, feedsStoriesCountMap);
        expListView.setAdapter(expListAdapter);

        //Load feed channels. Update expandable list data.
        feedViewModel.getFeeds().observe(this, resource -> {
            // update UI
            if (resource != null) {
                createCollection(resource.data);
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

                feedsStoriesCountMap.clear();
                for(FeedStoriesCount count: resource.data) {
                    feedsStoriesCountMap.put(count.getFeedId(), count.getStoriesCountTotal());
                    Log.d("ZOMIA", "Feed id: " + count.getFeedId() + "  stories count: " + count.getStoriesCountTotal());
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

    public void LoadFeeds()
    {
        swipeRefreshLayout.setRefreshing(true);
        if(feedViewModel != null)
            feedViewModel.getFeeds();

       /* feedViewModel.getFeeds().observe(this, feeds -> {
            // no null checks for adapter.get() since LiveData guarantees that we'll not receive
            // the event if fragment is now show.
            if (feeds == null) {
                expListAdapter.get().replace(null);
            } else {
                adapter.get().replace(feeds.data);
            }
        });*/

        /*swipeRefreshLayout.setRefreshing(true);

        apiService.getFeedsList().enqueue(new Callback<List<Feed>>() {
            @Override
            public void onResponse(Call<List<Feed>> call,Response<List<Feed>> response) {
                //To get the status code
                if(response.isSuccessful())
                {
                    switch(response.code())
                    {
                        case 200:
                            //No errors
                            Toast.makeText(getActivity(), getString(R.string.success), Toast.LENGTH_LONG).show();
                            // Send the event to the host activity
                            ShowFeeds(response.body());

                            swipeRefreshLayout.setRefreshing(false);
                            break;
                        default:
                            swipeRefreshLayout.setRefreshing(false);
                            break;
                    }
                }
                else
                {
                    //Connection problem
                    Toast.makeText(getActivity(), getString(R.string.connection_problem), Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<List<Feed>> call, Throwable t) {
                Toast.makeText(getActivity(), getString(R.string.no_server_connection), Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });*/
    }

    public void ShowFeeds(List<Feed> feedsList)
    {
        //Update data sets for tags and feeds
        //createTagList();
        createCollection(feedsList);

        //Notify adapter that data is updated
        if(expListAdapter != null)
            expListAdapter.notifyDataSetChanged();
    }

    private void createTagList() {
        if(tagList == null)
            tagList = new ArrayList<String>();
        else
            tagList.clear();

        tagList.add("Top");
        tagList.add("Fun");
        tagList.add("News");
        tagList.add("Games");
        tagList.add("Bookmarks");

        if(expListAdapter != null)
            expListAdapter.notifyDataSetChanged();
    }

    private void createCollection(List<Feed> feedsList) {

        if(feedsList != null) {
            //feedsCollection = new LinkedHashMap<String, List<Feed>>();
            feedsCollection.clear();
            childList = new ArrayList<Feed>();
            childList.addAll(feedsList);
            String tagValue = tagList.get(0);

            feedsCollection.put(tagValue, childList);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //feedsCollection.clear();
        //Get items ...
        createTagList();
        //LoadFeeds();
        //Update list
        if(expListAdapter != null)
            expListAdapter.notifyDataSetChanged();
    }
}
