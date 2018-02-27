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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import news.zomia.zomianews.Lists.TagListAdapter;
import news.zomia.zomianews.R;
import news.zomia.zomianews.data.model.Tag;
import news.zomia.zomianews.data.service.DataRepository;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.viewmodel.FeedViewModel;
import news.zomia.zomianews.data.viewmodel.FeedViewModelFactory;
import news.zomia.zomianews.di.Injectable;

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
    @Inject
    FeedViewModelFactory feedViewModelFactory;

    private FeedViewModel feedViewModel;
    private ListView tagsListView;
    private LiveData<Resource<Boolean>> tagInsertLiveData;
    private LiveData<Resource<Boolean>> feedInsertLiveData;
    TagListAdapter tagsListViewAdapter;

    //0: new feed; 1: edit feed
    private Integer mode;

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

        Bundle arguments = getArguments();
        mode = arguments.getInt("mode");

        feedSourcePathTextView = (TextView) view.findViewById(R.id.feedSourcePathTextView);

        //Tag list
        tagsListView = (ListView) view.findViewById(R.id.tagsListView);
        tagsListViewAdapter = new TagListAdapter(getActivity());
        tagsListView.setAdapter(tagsListViewAdapter);
        tagsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //Add tag to the list button
        FloatingActionButton addTagButton = (FloatingActionButton) view.findViewById(R.id.addTagButton);
        addTagButton.setOnClickListener(addTagButtonOnClickListener);

        //Add feed button
        FloatingActionButton addFeedButton = (FloatingActionButton) view.findViewById(R.id.addFeedButton);
        addFeedButton.setOnClickListener(addFeedButtonOnClickListener);

        //Feed channel type
        Spinner feedTypeList = (Spinner) view.findViewById(R.id.feedTypeList);
        ArrayAdapter<CharSequence> feedTypeListAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.channel_categories, android.R.layout.simple_spinner_item);
        feedTypeListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feedTypeList.setAdapter(feedTypeListAdapter);
        feedTypeList.setOnItemSelectedListener(this);
    }

    //Click listener for adding new feed
    private View.OnClickListener addTagButtonOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            showInputNewTagDialog();
        }
    };

    protected void showInputNewTagDialog() {

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.layout_input_tag_name, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String tagNameNew = editText.getText().toString();

                        if (!tagNameNew.isEmpty())
                            registerOnTagInsertObserver(tagNameNew);
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

    private void registerOnTagInsertObserver(String tagName)
    {
        //Insert new tag name to db and send request to server
        tagInsertLiveData = feedViewModel.insertNewTag(tagName);
        tagInsertLiveData.observe(this, this::onNewTagInserted);
    }

    private void unregisterOnTagInsertObserver()
    {
        if(tagInsertLiveData != null)
            tagInsertLiveData.removeObservers(this);
    }

    //Click listener for adding new feed
    private View.OnClickListener addFeedButtonOnClickListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            if (feedSourcePathTextView != null) {
                String feedUrl = feedSourcePathTextView.getText().toString();
                if (!feedUrl.isEmpty())
                    registerOnFeedInsertObserver(feedUrl, getSelectedTagNames());
            }
        }
    };

    private String getSelectedTagNames()
    {
        SparseBooleanArray checkedTags = tagsListView.getCheckedItemPositions();
        StringBuilder tagsSb = new StringBuilder();
        for (int i = 0; i < checkedTags.size(); i++) {
            int key = checkedTags.keyAt(i);
            if(checkedTags.get(key)) {
                tagsSb.append(tagsListViewAdapter.getTag(key).getName());
                tagsSb.append(" ");
            }
        }
        return tagsSb.toString();
    }

    private void registerOnFeedInsertObserver(String feedUrl, String tagsList)
    {
        //Insert new tag name to db and send request to server
        feedInsertLiveData = feedViewModel.insertNewFeed(feedUrl, tagsList);
        feedInsertLiveData.observe(this, this::onNewFeedInserted);
    }

    private void unregisterOnFeedInsertObserver()
    {
        if(feedInsertLiveData != null)
            feedInsertLiveData.removeObservers(this);
    }

    //On get tags list observer function
    private void onGetTags(Resource<List<Tag>> resource) {
        // update UI
        if (resource != null && resource.data != null) {
            //Add tags
            tagsListViewAdapter.replace(resource.data);

            //Update list
            if(tagsListViewAdapter != null)
                tagsListViewAdapter.notifyDataSetChanged();

            //For edit mode: get tags for current feed and select them in the list
            if (mode == 1) {
                //set feed link
                feedSourcePathTextView.setText(feedViewModel.getSelectedFeedId().getValue().getLink());
                //get list of tags for the current feed
                List<Tag> tagsOnFeed = feedViewModel.getTagsForCurrentFeed();
                //select tags for the current feed in the list view
                if(tagsListViewAdapter != null && tagsOnFeed != null) {
                    for (int i = 0; i < tagsListViewAdapter.getCount(); i++) {
                        Tag tagInTheList = tagsListViewAdapter.getTag(i);
                        for (Tag tag : tagsOnFeed) {
                            if (tag.getTagId() == tagInTheList.getTagId()) {
                                tagsListView.setItemChecked(i, true);
                            }
                        }
                    }
                }
            }
        }
    }

    private void onNewTagInserted(@Nullable Resource<Boolean> resource) {
        // update UI
        if (resource != null && resource.data != null) {
            switch (resource.status) {
                case SUCCESS:
                    Toast.makeText(getActivity(), getString(R.string.tag_insert_success), Toast.LENGTH_LONG).show();
                    unregisterOnTagInsertObserver();
                    break;
                case ERROR:
                    Toast.makeText(getActivity(), getString(R.string.tag_insert_error), Toast.LENGTH_LONG).show();
                    unregisterOnTagInsertObserver();
                    break;
            }
        }
    }

    private void onNewFeedInserted(@Nullable Resource<Boolean> resource) {
        // update UI
        if (resource != null && resource.data != null) {
            switch (resource.status) {
                case SUCCESS:
                    Toast.makeText(getActivity(), getString(R.string.feed_insert_success), Toast.LENGTH_LONG).show();
                    unregisterOnFeedInsertObserver();

                    // Send the event to the host activity
                    onFeedAddedListenerCallback.onFeedAdded();
                    break;
                case ERROR:
                    Toast.makeText(getActivity(), getString(R.string.feed_insert_error), Toast.LENGTH_LONG).show();
                    unregisterOnFeedInsertObserver();
                    break;
            }
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

        //Load tags. Update expandable list data.
        feedViewModel.getTags().observe(this, this::onGetTags);
    }

    @Override
    public void onStop() {
        //Unsubscribe all livedata observers
        feedViewModel.getTags().removeObservers(this);
        unregisterOnTagInsertObserver();
        unregisterOnFeedInsertObserver();

        super.onStop();
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
