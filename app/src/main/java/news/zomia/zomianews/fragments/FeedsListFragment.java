package news.zomia.zomianews.fragments;


import android.app.Activity;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

    private View rootView;

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    SwipeRefreshLayout swipeRefreshLayout;

    @Inject
    FeedViewModelFactory feedViewModelFactory;
    private FeedViewModel feedViewModel;
    private Map<String, List<Feed>> feedsCollection;
    private ExpandableListView expListView;
    private ExpandableListAdapter expListAdapter;
    private LiveData<Resource<Boolean>> tagEditLiveData;
    private LiveData<Resource<Boolean>> feedDeleteResultStatus;
    private LiveData<Resource<Boolean>> tagDeleteResultStatus;

    OnFeedsListListener onFeedsListListenerCallback;

    public FeedsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.feeds_list));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.layout_feeds_list, container, false);

        //Pull to refresh layout
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

        //Indicate that this fragment has appbar menu
        setHasOptionsMenu(true);

        //Feeds and tags collection to popullate feeds to the undecided tag group.
        feedsCollection = new LinkedHashMap<String, List<Feed>>();

        //List of tags and feeds
        expListView = (ExpandableListView)  view.findViewById(R.id.feedsExpandableList);
        expListAdapter = new ExpandableListAdapter(getActivity());
        expListView.setAdapter(expListAdapter);
        expListView.setOnChildClickListener(tagListClickListener);

        registerForContextMenu(expListView);
    }

    ExpandableListView.OnChildClickListener tagListClickListener = new ExpandableListView.OnChildClickListener() {

        public boolean onChildClick(ExpandableListView parent, View v,
        int groupPosition, int childPosition, long id) {
            final Feed selectedFeed = (Feed) expListAdapter.getChild(groupPosition, childPosition);

            Toast.makeText(getActivity(), selectedFeed.getTitle(), Toast.LENGTH_SHORT)
                    .show();

            //Set feed as selected on the viewmodel
            feedViewModel.setSelectedFeed(selectedFeed);
            onFeedsListListenerCallback.onFeedSelected();

            if(((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(feedViewModel.getSelectedFeedId().getValue().getTitle());
            }

            return true;
        }
    };

    //Tags and feeds text filter listener
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.feedsExpandableList) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.feeds_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
        int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);

        switch(item.getItemId()) {

            case R.id.edit_feed:
                if(childPos != -1) {
                    //Edit feed
                    Feed selectedFeed = (Feed) expListAdapter.getChild(groupPos, childPos);
                    feedViewModel.setSelectedFeed(selectedFeed);
                    onFeedsListListenerCallback.onFeedEdit();
                }
                else{
                    //Edit tag
                    String selectedTagName = (String) expListAdapter.getGroup(groupPos);
                    showInputNewTagDialog(selectedTagName);
                }
                return true;
            case R.id.delete_feed:
                if(childPos != -1) {
                    //Delete feed
                    Feed selectedFeed = (Feed) expListAdapter.getChild(groupPos, childPos);
                    registerOnFeedDeleteObserver(selectedFeed.getFeedId());
                }
                else{
                    //Delete tag
                    String selectedTagName = (String) expListAdapter.getGroup(groupPos);
                    registerOnTagDeleteObserver(selectedTagName);
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (menu.findItem(R.id.menu_search) != null)
            menu.findItem(R.id.menu_search).setVisible(true);

        if (menu.findItem(R.id.menu_refresh) != null)
            menu.findItem(R.id.menu_refresh).setVisible(true);

        if (menu.findItem(R.id.menu_add_channel) != null)
            menu.findItem(R.id.menu_add_channel).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                onRefresh();
                return true;

            case R.id.menu_add_channel:
                onFeedsListListenerCallback.onNewFeedAddAction();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void showInputNewTagDialog(String tagNameToEdit) {

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.layout_input_tag_name, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);


        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        editText.setText(tagNameToEdit);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String tagNameNew = editText.getText().toString();

                        if (!tagNameNew.isEmpty()) {
                            registerOnTagEditObserver(tagNameToEdit, tagNameNew);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void registerOnTagEditObserver(String oldTagName, String newTagName)
    {
        //Insert new tag name to db and send request to server
        tagEditLiveData = feedViewModel.updateTag(oldTagName, newTagName);
        tagEditLiveData.observe(this, this::onTagEdit);
    }

    private void unregisterOnTagEditObserver()
    {
        if(tagEditLiveData != null)
            tagEditLiveData.removeObservers(this);
    }

    private void onTagEdit(@Nullable Resource<Boolean> resource) {
        // update UI
        if (resource != null && resource.data != null) {
            switch (resource.status) {
                case SUCCESS:
                    Toast.makeText(getActivity(), getString(R.string.tag_edit_success), Toast.LENGTH_LONG).show();
                    unregisterOnTagEditObserver();
                    break;
                case ERROR:
                    Toast.makeText(getActivity(), getString(R.string.tag_edit_error), Toast.LENGTH_LONG).show();
                    unregisterOnTagEditObserver();
                    break;
            }
        }
    }

    private void registerOnTagDeleteObserver(String tagName)
    {
        tagDeleteResultStatus = feedViewModel.deleteTag(tagName);
        tagDeleteResultStatus.observe(this, this::onTagDelete);
    }

    private void unregisterOnTagDeleteObserver()
    {
        if(tagDeleteResultStatus != null)
            tagDeleteResultStatus.removeObservers(this);
    }

    private void onTagDelete(@Nullable Resource<Boolean> resource) {
        // update UI
        if (resource != null && resource.data != null) {
            switch (resource.status) {
                case SUCCESS:
                    Toast.makeText(getActivity(), getString(R.string.tag_delete_success), Toast.LENGTH_LONG).show();
                    unregisterOnTagDeleteObserver();
                    break;
                case ERROR:
                    Toast.makeText(getActivity(), getString(R.string.tag_delete_error), Toast.LENGTH_LONG).show();
                    unregisterOnTagDeleteObserver();
                    break;
            }
        }
    }

    private void registerOnFeedDeleteObserver(Integer feedId)
    {
        feedDeleteResultStatus = feedViewModel.deleteFeed(feedId);
        feedDeleteResultStatus.observe(this, this::onFeedDelete);
    }

    private void unregisterOnFeedDeleteObserver()
    {
        if(feedDeleteResultStatus != null)
            feedDeleteResultStatus.removeObservers(this);
    }

    private void onFeedDelete(@Nullable Resource<Boolean> resource) {
        // update UI
        if (resource != null && resource.data != null) {
            switch (resource.status) {
                case SUCCESS:
                    Toast.makeText(getActivity(), getString(R.string.feed_delete_success), Toast.LENGTH_LONG).show();
                    unregisterOnFeedDeleteObserver();
                    break;
                case ERROR:
                    Toast.makeText(getActivity(), getString(R.string.feed_delete_error), Toast.LENGTH_LONG).show();
                    unregisterOnFeedDeleteObserver();
                    break;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Get the SearchView on the app bar
        SearchView filterFeedsSearchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        if (filterFeedsSearchView != null) {
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

        //Call to receive new live data
        feedViewModel.refreshAll();
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
        //For now do nothing. We added this observer to receive feeds update.

        if(resource != null) {
            switch (resource.status) {
                case LOADING:
                    swipeRefreshLayout.setRefreshing(true);
                    break;
                case ERROR:
                    swipeRefreshLayout.setRefreshing(false);
                    //Show error message
                    Toast.makeText(getActivity(), resource.message, Toast.LENGTH_SHORT)
                            .show();
                    break;
                case SUCCESS:
                    swipeRefreshLayout.setRefreshing(false);
                    break;
            }
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
        if (resource != null) {
            if(resource.data != null) {
                //Add tags
                List<String> tagList = new ArrayList<String>();
                //Add default group for feeds with no tags
                tagList.add(getString(R.string.tag_undecided));

                //Add tags
                for (Tag tag : resource.data) {
                    tagList.add(tag.getName());
                }

                //Update list
                if (expListAdapter != null)
                    expListAdapter.replaceTagsList(tagList);
            }

            //Show pull to refresh status
            switch (resource.status) {
                case LOADING:
                    swipeRefreshLayout.setRefreshing(true);
                    break;
                case ERROR:
                    swipeRefreshLayout.setRefreshing(false);
                    //Show error message
                    Toast.makeText(getActivity(), resource.message, Toast.LENGTH_SHORT)
                            .show();
                    break;
                case SUCCESS:
                    swipeRefreshLayout.setRefreshing(false);
                    break;
            }
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
        feedViewModel.refreshAll();
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
        public void onFeedSelected();
        public void onNewFeedAddAction();
        public void onFeedEdit();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
