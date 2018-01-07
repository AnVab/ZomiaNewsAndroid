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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import news.zomia.zomianews.R;
import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.service.APIService;
import news.zomia.zomianews.data.service.ApiUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewFeedFragment extends Fragment {

    private APIService apiService;
    private View rootView;
    TextView feedSourcePathTextView;

    OnFeedAddedListener onFeedAddedListenerCallback;

    public NewFeedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.layout_add_news_source, container, false);

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

        feedSourcePathTextView = (TextView)  view.findViewById(R.id.feedSourcePathTextView);
        ExpandableListView feedTypeList = (ExpandableListView) view.findViewById(R.id.feedTypeList);
        Button addTagButton = (Button) view.findViewById(R.id.addTagButton);
        ListView tagsListView = (ListView) view.findViewById(R.id.tagsListView);

        Button addFeedButton = (Button) view.findViewById(R.id.addFeedButton);
        addFeedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (feedSourcePathTextView != null) {
                    String feedUrl = feedSourcePathTextView.getText().toString();
                    if (!feedUrl.isEmpty())
                    {
                        Feed feed = new Feed();
                        feed.setUrl(feedUrl);
                        apiService.addNewFeed(feed).enqueue(new Callback<Feed>() {
                            @Override
                            public void onResponse(Call<Feed> call, Response<Feed> response) {
                                //To get the status code
                                if (response.isSuccessful()) {
                                    switch (response.code()) {
                                        case 200:
                                            //No errors
                                            Toast.makeText(getActivity(), getString(R.string.success), Toast.LENGTH_LONG).show();
                                            // Send the event to the host activity
                                            onFeedAddedListenerCallback.onFeedAdded();
                                            break;
                                        default:

                                            break;
                                    }
                                } else {
                                    //Connection problem
                                    Toast.makeText(getActivity(), getString(R.string.connection_problem), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Feed> call, Throwable t) {
                                Toast.makeText(getActivity(), getString(R.string.no_server_connection), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = getActivity();

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        if(activity != null) {
            try {
                onFeedAddedListenerCallback = (OnFeedAddedListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnFeedAddedListener");
            }
        }
    }

    // Container Activity must implement this interface
    public interface OnFeedAddedListener {
        public void onFeedAdded();
    }
}
