package news.zomia.zomianews;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.Story;
import news.zomia.zomianews.data.service.HostSelectionInterceptor;
import news.zomia.zomianews.data.service.UserSessionInfo;
import news.zomia.zomianews.fragments.FeedStoriesFragment;
import news.zomia.zomianews.fragments.FeedsListFragment;
import news.zomia.zomianews.fragments.LoginFragment;
import news.zomia.zomianews.fragments.NewFeedFragment;
import news.zomia.zomianews.fragments.SettingsFragment;
import news.zomia.zomianews.fragments.StoryViewerFragment;

import news.zomia.zomianews.data.model.User;
import news.zomia.zomianews.data.model.Token;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.support.design.widget.AppBarLayout;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnSuccessAuthorizationListener,
        FeedsListFragment.OnFeedsListListener,
        FeedStoriesFragment.OnStorySelectedListener,
        NewFeedFragment.OnFeedAddedListener,
        SettingsFragment.OnSettingsChangedListener,
        StoryViewerFragment.OnStoryViewerListener,
        HasSupportFragmentInjector
{
    @Inject DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;
    @Inject HostSelectionInterceptor urlChangeInterceptor;
    @Inject UserSessionInfo userSessionInfo;
    @Inject SharedPreferences sharedPref;

    private TextView mResponse;
    //private APIService apiService;
    public User user;
    public Token userToken;
    private static final String TAG = "ZomiaMainActivity";

    StoryViewerFragment storyViewerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.action_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar actionBar = getSupportActionBar();
        // Enable the Up button
        actionBar.setDisplayHomeAsUpEnabled(true);

        storyViewerFragment = new StoryViewerFragment();

        //Read saved session token
        String token = sharedPref.getString(getString(R.string.preferences_token), "");
        //Set current session token
        userSessionInfo.setToken(token);

        updateZomiaUrl();

        if(token.isEmpty())
            LoadLoginFragment();
        else
            LoadFeedsListFragment();
    }

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

    public void onSuccessAuthorization() {

        //Read saved session token
        String token = sharedPref.getString(getString(R.string.preferences_token), "");
        //Set current session token
        userSessionInfo.setToken(token);

        LoadFeedsListFragment();
    }

    public void onFeedAdded()
    {
        LoadFeedsListFragment();
    }

    public void onFeedUpdated()
    {
        LoadFeedsListFragment();
    }

    public void onFeedSelected()
    {
      /*  FeedStoriesFragment fStoriesFrag = (FeedStoriesFragment)
                getSupportFragmentManager().findFragmentById(R.id.feed_stories_fragment);

        if (fStoriesFrag != null) {
            // If article frag is available, we're in two-pane layout
            // Call a method in the FeedStoriesFragment to update its content
            fStoriesFrag.updateStoriesView(feed.getFeedId());
        } else {
            // Otherwise, we're in the one-pane layout and must swap frags
*/
            if (findViewById(R.id.fragment_container) != null) {

                removeBottomPadding();

                FeedStoriesFragment feedStoriesFragment;
                feedStoriesFragment = new FeedStoriesFragment();
                // Create fragment
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                //fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_in_right, 0, 0);
                fragmentTransaction.replace(R.id.fragment_container, feedStoriesFragment);
                fragmentTransaction.addToBackStack("feedStoriesFragment");
                fragmentTransaction.commit();
            }
       // }
    }

    public void onNewFeedAddAction()
    {
        if (findViewById(R.id.fragment_container) != null) {

            addBottomPadding();

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            Bundle data = new Bundle();
            //mode = 0: add new feed
            data.putInt("mode", 0);

            NewFeedFragment newFeedFragment = new NewFeedFragment();
            newFeedFragment.setArguments(data);
            fragmentTransaction.replace(R.id.fragment_container, newFeedFragment);
            fragmentTransaction.addToBackStack("newFeedFragment");
            fragmentTransaction.commit();
        }
    }

    public void onFeedEdit()
    {
        if (findViewById(R.id.fragment_container) != null) {

            addBottomPadding();

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            Bundle data = new Bundle();
            //mode = 1: edit a feed
            data.putInt("mode", 1);

            NewFeedFragment newFeedFragment = new NewFeedFragment();
            newFeedFragment.setArguments(data);
            fragmentTransaction.replace(R.id.fragment_container, newFeedFragment);
            fragmentTransaction.addToBackStack("newFeedFragment");
            fragmentTransaction.commit();
        }
    }

    public void onTagEdit()
    {

    }

    public void onStorySelected(Story story)
    {
        if (findViewById(R.id.fragment_container) != null) {

            ShowToolbar();

            removeBottomPadding();

            // Create fragment and give it an argument for the selected article
            Bundle args = new Bundle();
            args.putString("date", story.getDate().toString());
            args.putString("title", story.getTitle());
            args.putString("content", story.getContent());

            storyViewerFragment.setArguments(args);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            //fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_in_right, 0, 0);
            fragmentTransaction.replace(R.id.fragment_container, storyViewerFragment);
            fragmentTransaction.addToBackStack("storyViewerFragment");
            fragmentTransaction.commit();
        }
    }

    public void onSettingsUpdated(String key)
    {
        if (key.equals(getString(R.string.preferences_serverAddress)) ||
                key.equals(getString(R.string.preferences_serverPort)))
        {
            updateZomiaUrl();
        }
    }

    public void nextStoryRequest(Integer currentStoryId)
    {

    }

    public void goBackToStoriesList()
    {
        onFeedSelected();
    }

    public void LoadLoginFragment()
    {
        if (findViewById(R.id.fragment_container) != null) {

            removeBottomPadding();

            LoginFragment loginFragment;
            loginFragment = new LoginFragment();

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            //fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_in_right, 0, 0);
            fragmentTransaction.replace(R.id.fragment_container, loginFragment);
            //fragmentTransaction.addToBackStack("loginFragment);
            fragmentTransaction.commit();
        }
    }

    public void LoadFeedsListFragment()
    {
        if (findViewById(R.id.fragment_container) != null) {

            addBottomPadding();

            FeedsListFragment feedsListFragment = new FeedsListFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            //fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_in_right, 0, 0);

            fragmentTransaction.replace(R.id.fragment_container, feedsListFragment);
            fragmentTransaction.addToBackStack("feedsListFragment");
            fragmentTransaction.commit();
        }
    }

    public void ShowSettingsFragment()
    {
        removeBottomPadding();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_in_right, 0, 0);
        SettingsFragment settingsFragment = new SettingsFragment();
        fragmentTransaction.replace(R.id.fragment_container, settingsFragment);
        fragmentTransaction.addToBackStack("settingsFragment");
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                ShowSettingsFragment();
                return true;

            case R.id.menu_search:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void updateZomiaUrl()
    {
        String serverAddress = sharedPref.getString(getString(R.string.preferences_serverAddress), getString(R.string.preferences_serverAddress_default));
        String serverPort = sharedPref.getString(getString(R.string.preferences_serverPort), getString(R.string.preferences_serverPort_default));

        String url = "http://" + serverAddress + ":" + serverPort + "/";
        urlChangeInterceptor.setInterceptor(url);

        Log.d(TAG, "Updated url: " + url);
    }

    @Override
    public void onBackPressed() {
        //Check if we get back to the FragmentTransaction on the top then exit app
        if (getSupportFragmentManager().getBackStackEntryCount() == 1){
            finish();
        }
        else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment instanceof FeedsListFragment) {
                super.onBackPressed();
                ShowToolbar();
                //Add bottom padding if we returned back to the feeds list fragment
                addBottomPadding();
            } else if (fragment instanceof StoryViewerFragment) {
                getSupportFragmentManager().popBackStack("feedStoriesFragment", 0);
                ShowToolbar();
            }
            else if (fragment instanceof FeedStoriesFragment) {
                getSupportFragmentManager().popBackStack("feedsListFragment", 0);
                ShowToolbar();
                //Add bottom padding if we returned back to the feeds list fragment
                addBottomPadding();
            }
            else {
                super.onBackPressed();
                ShowToolbar();
            }
        }
    }

    private void ShowToolbar()
    {
        AppBarLayout appBar = (AppBarLayout) findViewById(R.id.appbar);
        appBar.setExpanded(true, true);
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    private void addBottomPadding()
    {
        int navHeight = getNavHeight();
        if (navHeight > 0) {
            (findViewById(R.id.fragment_container)).setPadding(0, 0, 0, navHeight);
        }
    }

    private void removeBottomPadding()
    {
        (findViewById(R.id.fragment_container)).setPadding(0, 0, 0, 0);
    }

    private int getNavHeight() {
        //Fix for a bottom padding when action bar is added to the screen. If you just add bottom padding to the fragment in the xml, then while scrolling up there will be bottom space.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return 0;
        try {
            Resources resources = getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
        } catch (Exception ex) {
            return 0;
        }
        return 0;
    }
}
