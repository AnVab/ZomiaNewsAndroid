package news.zomia.zomianews.fragments;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.ActivityCompat;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.List;

import javax.inject.Inject;

import news.zomia.zomianews.Lists.TagListAdapter;
import news.zomia.zomianews.R;
import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.Tag;
import news.zomia.zomianews.data.service.DataRepository;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.viewmodel.FeedViewModel;
import news.zomia.zomianews.data.viewmodel.FeedViewModelFactory;
import news.zomia.zomianews.di.Injectable;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewFeedFragment extends Fragment implements
        AdapterView.OnItemSelectedListener,
        LifecycleRegistryOwner,
        Injectable {

    private View rootView;
    private TextView feedSourcePathTextView;
    private ProgressBar opmlmportProgressBar;

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    OnFeedAddedListener onFeedAddedListenerCallback;
    @Inject
    DataRepository dataRepo;
    @Inject
    FeedViewModelFactory feedViewModelFactory;

    private FeedViewModel feedViewModel;
    private LiveData<Resource<Boolean>> tagInsertLiveData;
    private LiveData<Resource<Boolean>> feedInsertLiveData;
    private LiveData<Resource<Boolean>> feedUpdateLiveData;
    private LiveData<Resource<Boolean>> opmlImportLiveData;
    private TagListAdapter tagsListViewAdapter;
    private ChipGroup tagsChipGroup;
    //0: new feed; 1: edit feed
    private Integer mode;

    private static final int OPML_READ_REQUEST_CODE = 716;

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

        //Indicate that this fragment has appbar menu
        setHasOptionsMenu(true);

        Bundle arguments = getArguments();
        mode = arguments.getInt("mode");

        feedSourcePathTextView = (TextView) view.findViewById(R.id.feedSourcePathTextView);

        //Tag list adapter
        tagsListViewAdapter = new TagListAdapter(getActivity());
        tagsChipGroup = (ChipGroup) view.findViewById(R.id.tagsChipGroup);

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

        opmlmportProgressBar = (ProgressBar) view.findViewById(R.id.opmlmportProgressBar);
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
                if (!feedUrl.isEmpty()) {
                    switch (mode) {
                        case 0:
                            registerOnFeedInsertObserver(feedUrl, getSelectedTagNames());
                            break;
                        case 1: {
                            Feed updatedFeed = feedViewModel.getSelectedFeedId().getValue();
                            updatedFeed.setUrl(feedUrl);
                            updatedFeed.setTag(getSelectedTagNames());
                            registerOnFeedUpdateObserver(updatedFeed);
                        }
                            break;
                    }
                }
            }
        }
    };

    private String getSelectedTagNames()
    {
        StringBuilder tagsSb = new StringBuilder();
        for(int index = 0; index < tagsChipGroup.getChildCount(); index++) {
            Chip chip = (Chip) tagsChipGroup.findViewById(tagsListViewAdapter.getTag(index).getTagId());
            if(chip.isChecked())
            {
                tagsSb.append(tagsListViewAdapter.getTag(index).getName());
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

    private void registerOnFeedUpdateObserver(Feed feed)
    {
        //Insert new tag name to db and send request to server
        feedUpdateLiveData = feedViewModel.updateFeed(feed);
        feedUpdateLiveData.observe(this, this::onFeedUpdated);
    }

    private void unregisterOnFeedUpdateObserver()
    {
        if(feedUpdateLiveData != null)
            feedUpdateLiveData.removeObservers(this);
    }

    //On get tags list observer function
    private void onGetTags(Resource<List<Tag>> resource) {
        // update UI
        if (resource != null && resource.data != null) {
            //Remove previously added tags to the chip group
            tagsChipGroup.removeAllViews();
            //Add tags list to the chip group
            for(Tag tag: resource.data) {
                Chip chip = new Chip(getActivity());
                chip.setId(tag.getTagId());
                chip.setChipText(tag.getName());
                chip.setCheckable(true);
                chip.setCheckedIconEnabled(false);
                chip.setChipCornerRadius(10.f);

                int[][] states = new int[][] {
                        new int[] {-android.R.attr.state_checked},
                        new int[] {android.R.attr.state_checked}
                };

                int[] colors = new int[] {
                        getResources().getColor(R.color.tag_list_item_background),
                        getResources().getColor(R.color.tag_list_item_checked_background)
                };

                ColorStateList chipColors = new ColorStateList(states, colors);
                chip.setChipBackgroundColor(chipColors);

                //Add listeners to a chip
                chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                    }
                });

                chip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
                tagsChipGroup.addView(chip);
            }

            //Add tags to the adapter
            tagsListViewAdapter.replace(resource.data);
            if(tagsListViewAdapter != null)
                tagsListViewAdapter.notifyDataSetChanged();

            //For edit mode: get tags for current feed and select them in the list
            if (mode == 1) {
                //set feed link
                feedSourcePathTextView.setText(feedViewModel.getSelectedFeedId().getValue().getUrl());
                //get list of tags for the current feed
                List<Tag> tagsOnFeed = feedViewModel.getTagsForCurrentFeed();

                //select tags for the current feed in the list view
                if(tagsListViewAdapter != null && tagsOnFeed != null) {
                    for (int i = 0; i < tagsListViewAdapter.getCount(); i++) {
                        Tag tagInTheList = tagsListViewAdapter.getTag(i);
                        for (Tag tag : tagsOnFeed) {
                            if (tag.getTagId() == tagInTheList.getTagId()) {
                                tagsChipGroup.check(tag.getTagId());
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

    private void onFeedUpdated(@Nullable Resource<Boolean> resource) {
        // update UI
        if (resource != null && resource.data != null) {
            switch (resource.status) {
                case SUCCESS:
                    Toast.makeText(getActivity(), getString(R.string.feed_insert_success), Toast.LENGTH_LONG).show();
                    unregisterOnFeedUpdateObserver();

                    // Send the event to the host activity
                    onFeedAddedListenerCallback.onFeedUpdated();
                    break;
                case ERROR:
                    Toast.makeText(getActivity(), getString(R.string.feed_insert_error), Toast.LENGTH_LONG).show();
                    unregisterOnFeedUpdateObserver();
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
        unregisterOnFeedUpdateObserver();
        unregisterOnImportOpmlObserver();

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
        public void onFeedUpdated();
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

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (menu.findItem(R.id.menu_import_opml) != null)
            menu.findItem(R.id.menu_import_opml).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_import_opml:
            {
                openOPMLFile();
            }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openOPMLFile()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_opml_file)), OPML_READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == OPML_READ_REQUEST_CODE && resultCode == RESULT_OK) {
            //The uri with the location of the file
            Uri selectedfile = null;
            if (data != null) {
                selectedfile = data.getData();
                try {
                    registerOnImportOpmlObserver(selectedfile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void registerOnImportOpmlObserver(Uri selectedfile) throws FileNotFoundException {
        verifyStoragePermissions(getActivity());

        //Show importing progressbar
        opmlmportProgressBar.setVisibility(View.VISIBLE);
        Toast.makeText(getActivity(), getString(R.string.opml_file_importing), Toast.LENGTH_LONG).show();

        String selectedFilePath = "";

        //Insert new tag name to db and send request to server
        opmlImportLiveData = feedViewModel.importOpml(getActivity().getContentResolver(), selectedfile);
        opmlImportLiveData.observe(this, this::onOpmlImported);
    }

    private void unregisterOnImportOpmlObserver()
    {
        opmlmportProgressBar.setVisibility(View.INVISIBLE);

        if(opmlImportLiveData != null)
            opmlImportLiveData.removeObservers(this);
    }

    private void onOpmlImported(@Nullable Resource<Boolean> resource) {
        // update UI
        if (resource != null && resource.data != null) {
            switch (resource.status) {
                case SUCCESS:
                    Toast.makeText(getActivity(), getString(R.string.opml_import_success), Toast.LENGTH_LONG).show();
                    unregisterOnImportOpmlObserver();
                    break;
                case ERROR:
                    Toast.makeText(getActivity(), getString(R.string.opml_import_error) + " " + resource.message, Toast.LENGTH_LONG).show();
                    unregisterOnImportOpmlObserver();
                    break;
            }
        }
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
