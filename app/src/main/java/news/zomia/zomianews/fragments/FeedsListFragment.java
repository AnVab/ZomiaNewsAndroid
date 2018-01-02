package news.zomia.zomianews.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
    Map<String, List<String>> feedsCollection;
    List<String> childList;
    ExpandableListView expListView;

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

        createTagList();
        createCollection();

        expListView = (ExpandableListView)  view.findViewById(R.id.feedsExpandableList);
        final ExpandableListAdapter expListAdapter = new ExpandableListAdapter(
                getActivity(), tagList, feedsCollection);

        expListView.setAdapter(expListAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                final String selected = (String) expListAdapter.getChild(
                        groupPosition, childPosition);

                /*Toast.makeText(getBaseContext(), selected, Toast.LENGTH_LONG)
                        .show();*/
                LoadFeeds();

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

        //Activity activity = context instanceof Activity ? (Activity) context : null;
        Activity activity = getActivity();

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        /*if(activity != null) {
            try {
                onSuccessAuthorizationCallback = (LoginFragment.OnSuccessAuthorizationListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnSuccessAuthorizationListener");
            }
        }*/
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
        //feedsExpandableList
    }

    private void createTagList() {
        tagList = new ArrayList<String>();
        tagList.add("Top");
        tagList.add("Fun");
        tagList.add("News");
        tagList.add("Games");
        tagList.add("Bookmarks");
    }

    private void createCollection() {
        // preparing laptops collection(child)
        String[] hpModels = { "HP Pavilion G6-2014TX", "ProBook HP 4540",
                "HP Envy 4-1025TX" };
        String[] hclModels = { "HCL S2101", "HCL L2102", "HCL V2002" };
        String[] lenovoModels = { "IdeaPad Z Series", "Essential G Series",
                "ThinkPad X Series", "Ideapad Z Series" };
        String[] sonyModels = { "VAIO E Series", "VAIO Z Series",
                "VAIO S Series", "VAIO YB Series" };
        String[] dellModels = { "Inspiron", "Vostro", "XPS" };
        String[] samsungModels = { "NP Series", "Series 5", "SF Series" };

        feedsCollection = new LinkedHashMap<String, List<String>>();

        for (String tagValue : tagList) {
            if (tagValue.equals("HP")) {
                loadChild(hpModels);
            } else if (tagValue.equals("Dell"))
                loadChild(dellModels);
            else if (tagValue.equals("Sony"))
                loadChild(sonyModels);
            else if (tagValue.equals("HCL"))
                loadChild(hclModels);
            else if (tagValue.equals("Samsung"))
                loadChild(samsungModels);
            else
                loadChild(lenovoModels);

            feedsCollection.put(tagValue, childList);
        }
    }

    private void loadChild(String[] feedsList) {
        childList = new ArrayList<String>();
        for (String feed : feedsList)
            childList.add(feed);
    }
}
