package news.zomia.zomianews;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import news.zomia.zomianews.data.model.Story;
import news.zomia.zomianews.data.service.DataRepository;
import news.zomia.zomianews.data.service.HostSelectionInterceptor;
import news.zomia.zomianews.data.service.NetworkConnectionInterceptorListener;
import news.zomia.zomianews.data.service.UnauthorizedInterceptorListener;
import news.zomia.zomianews.data.service.UserSessionInfo;
import news.zomia.zomianews.fragments.DummyFragment;
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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnSuccessAuthorizationListener,
        FeedsListFragment.OnFeedsListListener,
        FeedStoriesFragment.OnStorySelectedListener,
        NewFeedFragment.OnFeedAddedListener,
        SettingsFragment.OnSettingsChangedListener,
        StoryViewerFragment.OnStoryViewerListener,
        UnauthorizedInterceptorListener,
        NetworkConnectionInterceptorListener,
        DummyFragment.OnDummyFragmentListener,
        HasSupportFragmentInjector
{
    @Inject DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;
    @Inject HostSelectionInterceptor urlChangeInterceptor;
    @Inject UserSessionInfo userSessionInfo;
    @Inject SharedPreferences sharedPref;
    @Inject DataRepository dataRepo;

    private TextView mResponse;
    public User user;
    public Token userToken;
    private static final String TAG = "ZomiaMainActivity";

    private boolean ON_ROTATION_LEFT_FRAMELAYOUT_VISIBLE = true;

    private final int ON_ROTATION_FRAGMENTS_STATE_FEED_LIST = 1; //When show stories list
    private final int ON_ROTATION_FRAGMENTS_STATE_STORIES_LIST = 2; //When show stories list
    private final int ON_ROTATION_FRAGMENTS_STATE_STORY_VIEWER = 3; //When show story
    private final int ON_ROTATION_FRAGMENTS_STATE_FEED_EDIT = 4; //When show edit feed
    private final int ON_ROTATION_FRAGMENTS_STATE_SETTINGS = 5; //When show settings
    private int ON_ROTATION_ACTIVE_FEED_FRAME;

    private int containerLeftId;
    private int dataContainerId;
    boolean isTablet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);

        containerLeftId = R.id.left_container;
        dataContainerId = R.id.data_container;

        //Check if the device is a smartphone then use one pane mode
        isTablet = getResources().getBoolean(R.bool.isTablet);

        /*//Disable appbar scroll flags for tablets in the landscape mode
        if(getLandscapeOrientationTablet()) {
            AppBarLayout.LayoutParams params =
                    (AppBarLayout.LayoutParams) myToolbar.getLayoutParams();
            params.setScrollFlags(0);
        }*/

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
            else {
                LoadFeedsListFragment();
                LoadDummyFragment();
            }
        }
        else
        {
            //Get the previous state panel mode
            ON_ROTATION_LEFT_FRAMELAYOUT_VISIBLE = savedInstanceState.getBoolean("ON_ROTATION_LEFT_FRAMELAYOUT_VISIBLE");
            ON_ROTATION_ACTIVE_FEED_FRAME = savedInstanceState.getInt("ON_ROTATION_ACTIVE_FEED_FRAME");

            if(isTablet)
                HideLeftFrameLayout(ON_ROTATION_LEFT_FRAMELAYOUT_VISIBLE);
        }
    }

    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("ON_ROTATION_LEFT_FRAMELAYOUT_VISIBLE", ON_ROTATION_LEFT_FRAMELAYOUT_VISIBLE);
        savedInstanceState.putInt("ON_ROTATION_ACTIVE_FEED_FRAME", ON_ROTATION_ACTIVE_FEED_FRAME);
    }

    private boolean getLandscapeOrientationTablet()
    {
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        //return ((getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) && isTablet);
        return isTablet;
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

        //remove login fragment
        Fragment fragment = getSupportFragmentManager().findFragmentById(dataContainerId);
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
        ON_ROTATION_ACTIVE_FEED_FRAME = ON_ROTATION_FRAGMENTS_STATE_STORIES_LIST;
        if(getLandscapeOrientationTablet()) {
            // Create fragment
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            if (slideInRightSlideOutLeft)
                fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
            else
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

            FeedStoriesFragment feedStoriesFragment = new FeedStoriesFragment();
            Bundle data = new Bundle();
            data.putBoolean("showArrow", false);
            data.putBoolean("showBurger", true);
            feedStoriesFragment.setArguments(data);
            fragmentTransaction.replace(dataContainerId, feedStoriesFragment);
            fragmentTransaction.addToBackStack("feedStoriesFragment");
            fragmentTransaction.commit();
        }else {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            if (slideInRightSlideOutLeft)
                fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
            else
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            FeedStoriesFragment feedStoriesFragment = new FeedStoriesFragment();
            fragmentTransaction.replace(dataContainerId, feedStoriesFragment);
            fragmentTransaction.addToBackStack("feedStoriesFragment");
            fragmentTransaction.commit();
        }

        ShowToolbar();
    }

    public void onNewFeedAddAction()
    {
        ON_ROTATION_ACTIVE_FEED_FRAME = ON_ROTATION_FRAGMENTS_STATE_FEED_EDIT;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Bundle data = new Bundle();
        //mode = 0: add new feed
        data.putInt("mode", 0);

        NewFeedFragment newFeedFragment = new NewFeedFragment();
        newFeedFragment.setArguments(data);
        fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
        fragmentTransaction.replace(dataContainerId, newFeedFragment);
        fragmentTransaction.addToBackStack("newFeedFragment");
        fragmentTransaction.commit();
    }

    public void onFeedEdit()
    {
        ON_ROTATION_ACTIVE_FEED_FRAME = ON_ROTATION_FRAGMENTS_STATE_FEED_EDIT;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Bundle data = new Bundle();
        //mode = 1: edit a feed
        data.putInt("mode", 1);

        NewFeedFragment newFeedFragment = new NewFeedFragment();
        newFeedFragment.setArguments(data);
        fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
        fragmentTransaction.replace(dataContainerId, newFeedFragment);
        fragmentTransaction.addToBackStack("newFeedFragment");
        fragmentTransaction.commit();
    }

    @Override
    public void onSettings() {
        ShowSettingsFragment();
    }

    @Override
    public void onLogOut() {
        logOut();
    }

    public void onStorySelected(Story story)
    {
        loadStoryFragment();
    }

    public void loadStoryFragment()
    {
        ON_ROTATION_ACTIVE_FEED_FRAME = ON_ROTATION_FRAGMENTS_STATE_STORY_VIEWER;

        if(getLandscapeOrientationTablet()) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            //Check if we already have stories list fragment in the left container. If not, add it to the left container
            Fragment fragment = getSupportFragmentManager().findFragmentById(containerLeftId);
            if(!(fragment instanceof FeedStoriesFragment)) {
                fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
                FeedStoriesFragment feedStoriesFragment = new FeedStoriesFragment();
                Bundle data = new Bundle();
                data.putBoolean("showBurger", false);
                data.putBoolean("showArrow", true);
                feedStoriesFragment.setArguments(data);
                fragmentTransaction.replace(containerLeftId, feedStoriesFragment);
                fragmentTransaction.addToBackStack("feedStoriesFragment");
            }

            //Add stories container
            fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
            StoryViewerFragment storyViewerFragment = new StoryViewerFragment();
            Bundle data2 = new Bundle();
            data2.putBoolean("showBurger", true);
            data2.putBoolean("showArrow", false);
            storyViewerFragment.setArguments(data2);
            fragmentTransaction.replace(dataContainerId, storyViewerFragment);
            fragmentTransaction.addToBackStack("storyViewerFragment");
            fragmentTransaction.commit();
        }
        else {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
            StoryViewerFragment storyViewerFragment = new StoryViewerFragment();
            fragmentTransaction.replace(dataContainerId, storyViewerFragment);
            fragmentTransaction.addToBackStack("storyViewerFragment");
            fragmentTransaction.commit();
        }
        ShowToolbar();
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

    public void clearFrameLayoutView(int viewId)
    {
        //Clear view
        FrameLayout fl = (FrameLayout) findViewById(viewId);
        fl.removeAllViews();
    }

    public void LoadLoginFragment()
    {
        LoginFragment loginFragment;
        loginFragment = new LoginFragment();

        HideLeftFrameLayout(true);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fragmentTransaction.replace(dataContainerId, loginFragment);
        fragmentTransaction.commit();
    }

    public void HideLeftFrameLayout(boolean state)
    {
        if (findViewById(R.id.left_container) != null) {
            ON_ROTATION_LEFT_FRAMELAYOUT_VISIBLE = state;
            if (state)
                setFrameLayoutVisibility(R.id.left_container, View.GONE);
            else
                setFrameLayoutVisibility(R.id.left_container, View.VISIBLE);
        }
    }

    public void LoadFeedsListFragment()
    {
        showFeedListFragment(true);
    }

    public void showFeedListFragment(boolean slideInRightSlideOutLeft)
    {
        ON_ROTATION_ACTIVE_FEED_FRAME = ON_ROTATION_FRAGMENTS_STATE_FEED_LIST;

        if(getLandscapeOrientationTablet()) {
            HideLeftFrameLayout(false);

            FeedsListFragment feedsListFragment = new FeedsListFragment();
            Bundle data = new Bundle();
            data.putBoolean("showBurger", false);
            feedsListFragment.setArguments(data);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
            fragmentTransaction.replace(containerLeftId, feedsListFragment);
            fragmentTransaction.addToBackStack("feedsListFragment");
            fragmentTransaction.commit();
        }
        else {
            FeedsListFragment feedsListFragment = new FeedsListFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
            if (slideInRightSlideOutLeft)
                fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
            else
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

            fragmentTransaction.replace(dataContainerId, feedsListFragment);
            fragmentTransaction.addToBackStack("feedsListFragment");
            fragmentTransaction.commit();
        }
        ShowToolbar();
    }

    public void LoadDummyFragment()
    {
        if(getLandscapeOrientationTablet()) {
            DummyFragment dummyFragment = new DummyFragment();
            Bundle data = new Bundle();
            data.putBoolean("showBurger", true);
            data.putBoolean("showArrow", false);
            dummyFragment.setArguments(data);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            //fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
            fragmentTransaction.replace(dataContainerId, dummyFragment);
            fragmentTransaction.addToBackStack("dummyFragment");
            fragmentTransaction.commit();
        }
        else {
        }
        ShowToolbar();
    }

    private void setFrameLayoutVisibility(int viewId, int visibilityState)
    {
        FrameLayout fl = (FrameLayout) findViewById(viewId);
        fl.setVisibility(visibilityState);
    }

    public void ShowSettingsFragment()
    {
        ON_ROTATION_ACTIVE_FEED_FRAME = ON_ROTATION_FRAGMENTS_STATE_SETTINGS;

        HideLeftFrameLayout(true);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
        SettingsFragment settingsFragment = new SettingsFragment();
        fragmentTransaction.replace(R.id.data_container, settingsFragment);
        fragmentTransaction.addToBackStack("settingsFragment");
        fragmentTransaction.commit();

        ShowToolbar();
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
            if(getLandscapeOrientationTablet()) {
                Log.d(TAG, "ON_ROTATION_ACTIVE_FEED_FRAME: " + ON_ROTATION_ACTIVE_FEED_FRAME);
                switch (ON_ROTATION_ACTIVE_FEED_FRAME) {
                    case ON_ROTATION_FRAGMENTS_STATE_FEED_LIST:

                        break;
                    case ON_ROTATION_FRAGMENTS_STATE_STORIES_LIST:
                        break;
                    case ON_ROTATION_FRAGMENTS_STATE_FEED_EDIT:
                        super.onBackPressed();
                        break;
                    case ON_ROTATION_FRAGMENTS_STATE_SETTINGS:
                        HideLeftFrameLayout(false);
                        super.onBackPressed();
                        break;

                    case ON_ROTATION_FRAGMENTS_STATE_STORY_VIEWER:
                        LoadFeedsListFragment();
                        goBackToStoriesList();
                        break;
                    default:
                        super.onBackPressed();
                        break;
                }
            }
            else {
                switch (ON_ROTATION_ACTIVE_FEED_FRAME) {
                    case ON_ROTATION_FRAGMENTS_STATE_FEED_LIST:
                        //super.onBackPressed();
                        break;
                    case ON_ROTATION_FRAGMENTS_STATE_STORIES_LIST:
                        showFeedListFragment(false);
                        break;
                    case ON_ROTATION_FRAGMENTS_STATE_STORY_VIEWER:
                        goBackToStoriesList();
                        break;
                    default:
                        super.onBackPressed();
                        break;
                }
            }
        }
    }

    private void ShowToolbar()
    {
       // AppBarLayout appBar = (AppBarLayout) findViewById(R.id.appbar);
       // appBar.setExpanded(true, true);
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

    //Padding for a floating buttons
    /*private void addBottomPadding(int containerId)
    {
        if(getLandscapeOrientationTablet())
            return;

        int navHeight = getNavHeight();
        if (navHeight > 0) {
            (findViewById(containerId)).setPadding(0, 0, 0, (int)(navHeight * 1.2));
        }
    }

    private void removeBottomPadding()
    {
        (findViewById(containercentralId)).setPadding(0, 0, 0, 0);
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
    }*/

    @Override
    public void onUnauthorizedEvent() {
        //Show error message. We run it on the ui thread because we call this event from another thread,
        // otherwise we get exception
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                LoadLoginFragment();
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
