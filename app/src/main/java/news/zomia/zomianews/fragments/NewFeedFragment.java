package news.zomia.zomianews.fragments;


import android.app.Activity;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import javax.inject.Inject;

import news.zomia.zomianews.R;
import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.service.DataRepository;
import news.zomia.zomianews.di.Injectable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewFeedFragment extends Fragment implements
        AdapterView.OnItemSelectedListener,
        LifecycleRegistryOwner,
        Injectable {

    private View rootView;
    TextView feedSourcePathTextView;

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    OnFeedAddedListener onFeedAddedListenerCallback;
    @Inject
    DataRepository dataRepo;

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

        feedSourcePathTextView = (TextView)  view.findViewById(R.id.feedSourcePathTextView);

        Button addTagButton = (Button) view.findViewById(R.id.addTagButton);
        ListView tagsListView = (ListView) view.findViewById(R.id.tagsListView);

        FloatingActionButton addFeedButton = (FloatingActionButton)view.findViewById(R.id.addFeedButton);
        addFeedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (feedSourcePathTextView != null) {
                    String feedUrl = feedSourcePathTextView.getText().toString();
                    if (!feedUrl.isEmpty())
                    {
                        Feed feed = new Feed();
                        feed.setUrl(feedUrl);
                        dataRepo.getZomiaService().addNewFeed(feed).enqueue(new Callback<Feed>() {
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

        Spinner feedTypeList = (Spinner) view.findViewById(R.id.feedTypeList);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.channel_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feedTypeList.setAdapter(adapter);
        feedTypeList.setOnItemSelectedListener(this);
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
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

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        Log.d("ZOMIA", "Selected: " + parent.getItemAtPosition(pos));
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
