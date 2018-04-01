package news.zomia.zomianews;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import news.zomia.zomianews.data.model.Story;
import news.zomia.zomianews.data.service.DataRepository;
import news.zomia.zomianews.data.service.HostSelectionInterceptor;
import news.zomia.zomianews.data.service.NetworkConnectionInterceptorListener;
import news.zomia.zomianews.data.service.UnauthorizedInterceptorListener;
import news.zomia.zomianews.data.service.UserSessionInfo;
import news.zomia.zomianews.fragments.FeedStoriesFragment;
import news.zomia.zomianews.fragments.FeedsListFragment;
import news.zomia.zomianews.fragments.LoginFragment;
import news.zomia.zomianews.fragments.NewFeedFragment;
import news.zomia.zomianews.fragments.SettingsFragment;
import news.zomia.zomianews.fragments.StoryViewerFragment;

import news.zomia.zomianews.data.model.User;
import news.zomia.zomianews.data.model.Token;

import android.content.res.Configuration;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnSuccessAuthorizationListener,
        FeedsListFragment.OnFeedsListListener,
        FeedStoriesFragment.OnStorySelectedListener,
        NewFeedFragment.OnFeedAddedListener,
        SettingsFragment.OnSettingsChangedListener,
        StoryViewerFragment.OnStoryViewerListener,
        UnauthorizedInterceptorListener,
        NetworkConnectionInterceptorListener,
        HasSupportFragmentInjector
{
    @Inject DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;
    @Inject HostSelectionInterceptor urlChangeInterceptor;
    @Inject UserSessionInfo userSessionInfo;
    @Inject SharedPreferences sharedPref;
    @Inject DataRepository dataRepo;

    private TextView mResponse;
    //private APIService apiService;
    public User user;
    public Token userToken;
    private static final String TAG = "ZomiaMainActivity";
    private boolean twoPaneMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);

        //Search layout for a two pane frame for feeds container
        if (findViewById(R.id.feeds_container) != null)
            twoPaneMode = true;
        else
            twoPaneMode = false;

        Toolbar myToolbar = (Toolbar) findViewById(R.id.action_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar actionBar = getSupportActionBar();
        // Enable the Up button
        actionBar.setDisplayHomeAsUpEnabled(true);

        ((ZomiaApp) getApplication()).setUnauthorizedInterceptorListener(this);
        ((ZomiaApp) getApplication()).setNetworkConnectionInterceptorListener(this);

        //Read saved session token
        String token = sharedPref.getString(getString(R.string.preferences_token), "");
        //Set current session token
        userSessionInfo.setToken(token);

        updateZomiaUrl();

        if(savedInstanceState == null) {
            if (token.isEmpty())
                LoadLoginFragment();
            else
                LoadFeedsListFragment();
        }
        else
        {
            //Delete a feeds list fragment that was created in the portrait mode. We see that fragment in the landscape mode
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if(fragment instanceof FeedsListFragment) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.remove(fragment);
                fragmentTransaction.commit();
            }

            //Check if we returned from portrtait mode to landscape and don't have feeds list in the left side, then create it
            if(twoPaneMode) {
                fragment = getSupportFragmentManager().findFragmentById(R.id.feeds_container);
                if(fragment == null)
                    LoadFeedsListFragment();
            }
        }
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

        //remove login dragment
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if(fragment instanceof LoginFragment) {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(fragment);
            fragmentTransaction.commit();
        }

        //Load feeds list
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
        showFeedStoriesFragment(true);
    }

    public void showFeedStoriesFragment(boolean slideInRightSlideOutLeft)
    {
        if (findViewById(R.id.fragment_container) != null) {
            removeBottomPadding();

            // Create fragment
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            if(slideInRightSlideOutLeft)
                fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
            else
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

            FeedStoriesFragment feedStoriesFragment = new FeedStoriesFragment();
            fragmentTransaction.replace(R.id.fragment_container, feedStoriesFragment);
            fragmentTransaction.addToBackStack("feedStoriesFragment");
            fragmentTransaction.commit();
        }
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
            fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
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
            fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
            fragmentTransaction.replace(R.id.fragment_container, newFeedFragment);
            fragmentTransaction.addToBackStack("newFeedFragment");
            fragmentTransaction.commit();
        }
    }

    public void onStorySelected(Story story)
    {
        loadStoryFragment();
    }

    public void loadStoryFragment()
    {
        if (findViewById(R.id.fragment_container) != null) {

            ShowToolbar();

            removeBottomPadding();

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
            StoryViewerFragment storyViewerFragment = new StoryViewerFragment();
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

    public void goBackToStoriesList()
    {
        showFeedStoriesFragment(false);
    }

    public void LoadLoginFragment()
    {
        if (findViewById(R.id.fragment_container) != null) {

            //Collapse left framelayout when landscape mode
            /*Guideline guideLine = (Guideline) findViewById(R.id.guideline);
            if(guideLine != null) {
                ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) guideLine.getLayoutParams();
                lp.guidePercent = 0;
                guideLine.setLayoutParams(lp);
            }*/

            removeBottomPadding();

            LoginFragment loginFragment;
            loginFragment = new LoginFragment();

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            fragmentTransaction.replace(R.id.fragment_container, loginFragment);
            //fragmentTransaction.addToBackStack("loginFragment);
            fragmentTransaction.commit();
        }
    }

    public void LoadFeedsListFragment()
    {
        /*Guideline guideLine = (Guideline) findViewById(R.id.guideline);
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) guideLine.getLayoutParams();
        lp.guidePercent = 30;
        guideLine.setLayoutParams(lp);*/

        if (!twoPaneMode && findViewById(R.id.fragment_container) != null) {
            addBottomPadding();

            FeedsListFragment feedsListFragment = new FeedsListFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
            fragmentTransaction.replace(R.id.fragment_container, feedsListFragment);
            fragmentTransaction.addToBackStack("feedsListFragment");
            fragmentTransaction.commit();
        }

        if (twoPaneMode) {
            addBottomPadding();
            FeedsListFragment feedsListFragment = new FeedsListFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
            fragmentTransaction.replace(R.id.feeds_container, feedsListFragment);
            fragmentTransaction.addToBackStack("feedsListFragment");
            fragmentTransaction.commit();
        }
    }

    public void ShowSettingsFragment()
    {
        removeBottomPadding();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
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

            case R.id.logout:
                logOut();
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

    private void logOut()
    {
        //Reset token in the preferences
        String token = "";
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.preferences_token), token);
        editor.commit();

        //Load token from preferences
        token = sharedPref.getString(getString(R.string.preferences_token), "");

        //Set current session token
        userSessionInfo.setToken(token);

        //Delete all data in the database
        dataRepo.deleteAllData();

        //Show login form
        LoadLoginFragment();
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
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    //Create a feeds list when we press back in the portratit mode after changin orientation from the landscape mode
                    if(!twoPaneMode)
                        LoadFeedsListFragment();
                    ShowToolbar();
                    //Add bottom padding if we returned back to the feeds list fragment
                    addBottomPadding();
                }
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
        ((ZomiaApp) getApplication()).removeUnauthorizedInterceptorListener();
        ((ZomiaApp) getApplication()).removeNetworkConnectionInterceptorListener();
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

    @Override
    public void onUnauthorizedEvent() {

        LoadLoginFragment();

        //Show error message. We ru it on ui thread because we call this event from another thread,
        // otherwise we get exception
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                Toast.makeText(getApplicationContext(), getString(R.string.invalid_token), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onNetworkUnavailable() {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                Toast.makeText(getApplicationContext(), getString(R.string.network_unavailable), Toast.LENGTH_LONG).show();
            }
        });
    }
}
