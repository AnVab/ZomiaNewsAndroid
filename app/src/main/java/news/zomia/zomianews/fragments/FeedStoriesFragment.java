package news.zomia.zomianews.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import news.zomia.zomianews.Lists.storyadapter.StoriesAdapter;
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
public class FeedStoriesFragment extends Fragment implements StoriesAdapter.StoryViewHolder.ClickListener{

    private static final String TAG = "ZomiaFStoriesFragment";
    private APIService apiService;
    private View rootView;
    private int feedId;

    private RecyclerView storiesListView;
    private List<Result> storiesList;
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

        storiesListView = (RecyclerView) view.findViewById(R.id.storiesListView);

        storiesList = new ArrayList<Result>();
        storiesAdapter = new StoriesAdapter(getActivity(), storiesList, this);
        storiesListView.setAdapter(storiesAdapter);
        storiesListView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        storiesListView.setLayoutManager(llm);

        LoadFeedStories(feedId);
    }

    @Override
    public void onItemClicked(int position) {
        Result selectedStory = (Result) storiesAdapter.getItem(position);
        onStorySelectedListenerCallback.onStorySelected(selectedStory);
    }

    @Override
    public boolean onItemLongClicked(int position) {

        toggleSelection(position);

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
    private void toggleSelection(int position) {
        storiesAdapter.toggleSelection(position);
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
        public void onStorySelected(Result story);
    }
}
