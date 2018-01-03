package news.zomia.zomianews.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import news.zomia.zomianews.Lists.StoriesAdapter;
import news.zomia.zomianews.R;
import news.zomia.zomianews.data.model.Result;
import news.zomia.zomianews.data.model.Stories;
import news.zomia.zomianews.data.service.APIService;
import news.zomia.zomianews.data.service.ApiUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedStoriesFragment extends Fragment {

    private static final String TAG = "ZomiaFStoriesFragment";
    private APIService apiService;
    private View rootView;
    private int feedId;

    TextView textView2;
    ListView storiesListView;
    List<Result> storiesList;
    StoriesAdapter storiesAdapter;
    public FeedStoriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.layout_feed_stories, container, false);
        feedId = getArguments().getInt("feedId");

        return rootView;
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiUtils.getAPIService();

        textView2 = (TextView) view.findViewById(R.id.textView2);
        storiesListView = (ListView) view.findViewById(R.id.storiesListView);
        storiesList = new ArrayList<Result>();
        storiesAdapter = new StoriesAdapter(getActivity(), storiesList);
        storiesListView.setAdapter(storiesAdapter);

        storiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

            }
        });

        LoadFeedStories(feedId);
    }

    public void updateStoriesView(int feedId)
    {

    }

    private void LoadFeedStories(int feedId)
    {
        apiService.getStories(feedId).enqueue(new Callback<Stories>() {
            @Override
            public void onResponse(Call<Stories> call, Response<Stories> response) {
                //To get the status code
                if(response.isSuccessful())
                {
                    switch(response.code())
                    {
                        case 200:
                            //No errors
                            //Toast.makeText(getActivity(), getString(R.string.success), Toast.LENGTH_LONG).show();
                            // Send the event to the host activity
                            ShowStories(response.body());
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
            public void onFailure(Call<Stories> call, Throwable t) {
                Toast.makeText(getActivity(), getString(R.string.no_server_connection), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void ShowStories(Stories stories)
    {
        storiesList.clear();
        storiesList.addAll(stories.getResults());
        storiesAdapter.notifyDataSetChanged();
    }
}
