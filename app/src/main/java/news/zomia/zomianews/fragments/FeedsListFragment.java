package news.zomia.zomianews.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import news.zomia.zomianews.Lists.ExpandableListAdapter;
import news.zomia.zomianews.R;
import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.Token;
import news.zomia.zomianews.data.service.APIService;
import news.zomia.zomianews.data.service.ApiUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedsListFragment extends Fragment {

    private APIService apiService;
    private View rootView;

    List<String> tagList;
    Map<String, List<Feed>> feedsCollection;
    List<Feed> childList;
    ExpandableListView expListView;
    ExpandableListAdapter expListAdapter;

    OnFeedSelectedListener onFeedSelectedListenerCallback;

    public FeedsListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.layout_feeds_list, container, false);

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiUtils.getAPIService();

        tagList = new ArrayList<String>();
        createTagList();
        feedsCollection = new LinkedHashMap<String, List<Feed>>();

        expListView = (ExpandableListView)  view.findViewById(R.id.feedsExpandableList);
        expListAdapter = new ExpandableListAdapter(
                getActivity(), tagList, feedsCollection);

        expListView.setAdapter(expListAdapter);

        LoadFeeds();

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                final Feed selectedFeed = (Feed) expListAdapter.getChild(groupPosition, childPosition);

                Toast.makeText(getActivity(), selectedFeed.getTitle(), Toast.LENGTH_LONG)
                        .show();

                onFeedSelectedListenerCallback.onFeedSelected(selectedFeed);

                return true;
            }
        });
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
                onFeedSelectedListenerCallback = (OnFeedSelectedListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnFeedSelectedListener");
            }
        }
    }

    // Container Activity must implement this interface
    public interface OnFeedSelectedListener {
        public void onFeedSelected(Feed feed);
    }

    public void LoadFeeds()
    {
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
                            break;
                        default:

                            break;
                    }
                }
                else
                {
                    //Connection problem
                    Toast.makeText(getActivity(), getString(R.string.connection_problem), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Feed>> call, Throwable t) {
                Toast.makeText(getActivity(), getString(R.string.no_server_connection), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void ShowFeeds(List<Feed> feedsList)
    {
        //Update data sets for tags and feeds
        //createTagList();
        createCollection(feedsList);

        //Notify adapter that data is updated
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

        //feedsCollection = new LinkedHashMap<String, List<Feed>>();
        feedsCollection.clear();
        childList = new ArrayList<Feed>();
        childList.addAll(feedsList);
        String tagValue = tagList.get(0);

        feedsCollection.put(tagValue, childList);
    }

    @Override
    public void onResume() {
        super.onResume();
        //feedsCollection.clear();
        //Get items ...
        createTagList();
        LoadFeeds();
        //Update list
        expListAdapter.notifyDataSetChanged();
    }
}
