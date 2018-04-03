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

    private int PANEL_MODE_STATE;
    private final int PANEL_MODE_ONE_PANE_LEFT = 0;
    private final int PANEL_MODE_ONE_PANE_CENTRAL = 1;
    private final int PANEL_MODE_ONE_PANE_CENTRAL_SERVICE = 2;
    private final int PANEL_MODE_ONE_PANE_RIGHT = 3;
    private final int PANEL_MODE_TWO_PANE_LEFT = 4;
    private final int PANEL_MODE_TWO_PANE_RIGHT = 5;
    private int SERVICE_PANEL_MODE_STATE_PREVIOUS;

    private Guideline guidelineLeft;
    private Guideline guidelineRight;
    private int containerLeftId;
    private int containercentralId;
    private int containerRightId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);

        guidelineLeft = (Guideline) findViewById(R.id.guidelineLeft);
        guidelineRight = (Guideline) findViewById(R.id.guidelineRight);
        containerLeftId = R.id.containerLeft;
        containercentralId = R.id.containerCentral;
        containerRightId = R.id.containerRight;

        //Check if the device is a smartphone then use one pane mode
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if(!isTablet)
            setOnePaneLeftMode();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.action_toolbar);
        setSupportActionBar(myToolbar);

        //Disable appbar scroll flags for tablets in the landscape mode
        if(getLandscapeOrientation()) {
            AppBarLayout.LayoutParams params =
                    (AppBarLayout.LayoutParams) myToolbar.getLayoutParams();
            params.setScrollFlags(0);
        }
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
            //Get the previous state panel mode
            int PANEL_MODE_STATE_PREVIOUS = savedInstanceState.getInt("PANEL_MODE_STATE");
            //Get the previous service panel mode if we have settings fragment or other service frame displayed on the screen
            //SERVICE_PANEL_MODE_STATE_PREVIOUS = savedInstanceState.getInt("SERVICE_PANEL_MODE_STATE_PREVIOUS");
            Log.d(TAG, "PANEL_MODE_STATE_PREVIOUS: " + PANEL_MODE_STATE_PREVIOUS);
            //If currently we a in the landscape mode
            if(getLandscapeOrientation())
            {
                //Previous orientation was portrait
                switch(PANEL_MODE_STATE_PREVIOUS)
                {
                    case PANEL_MODE_ONE_PANE_LEFT:
                        setTwoPaneLeftMode();
                        break;
                    case PANEL_MODE_ONE_PANE_CENTRAL:
                        setTwoPaneRightMode();
                        break;
                    case PANEL_MODE_ONE_PANE_RIGHT:
                        setTwoPaneRightMode();
                        break;
                    case PANEL_MODE_ONE_PANE_CENTRAL_SERVICE:
                        setOnePaneCentralServiceMode();
                        break;
                    default:
                        break;
                }
            }else {
                //If currently we a in the portrait mode
                //Previous orientation was landscape
                switch(PANEL_MODE_STATE_PREVIOUS)
                {
                    case PANEL_MODE_ONE_PANE_CENTRAL:
                        setOnePaneCentralMode();
                        break;
                    case PANEL_MODE_TWO_PANE_LEFT:
                        setOnePaneCentralMode();
                        break;
                    case PANEL_MODE_TWO_PANE_RIGHT:
                        setOnePaneRightMode();
                        break;
                    case PANEL_MODE_ONE_PANE_CENTRAL_SERVICE:
                        setOnePaneCentralServiceMode();
                        break;
                    default:
                        break;
                }

                //Add padding for left frame. If we don't do that, then after we change orientation,
                // floating button would be below screen edge
                addBottomPadding(containerLeftId);
            }
        }
    }

    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("PANEL_MODE_STATE", PANEL_MODE_STATE);
        savedInstanceState.putInt("SERVICE_PANEL_MODE_STATE_PREVIOUS", SERVICE_PANEL_MODE_STATE_PREVIOUS);
    }

    private void setOnePaneLeftMode()
    {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guidelineLeft.getLayoutParams();
        params.guidePercent = 1.0f;
        guidelineLeft.setLayoutParams(params);

        ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) guidelineRight.getLayoutParams();
        params2.guidePercent = 1.0f;
        guidelineRight.setLayoutParams(params2);

        FrameLayout leftLayout = (FrameLayout) findViewById(containerLeftId);
        leftLayout.setVisibility(View.VISIBLE);

        FrameLayout centralLayout = (FrameLayout) findViewById(containercentralId);
        centralLayout.setVisibility(View.GONE);

        FrameLayout rightLayout = (FrameLayout) findViewById(containerRightId);
        rightLayout.setVisibility(View.GONE);

        PANEL_MODE_STATE = PANEL_MODE_ONE_PANE_LEFT;
    }

    private void setOnePaneCentralServiceMode()
    {
        setOnePaneCentralMode();

        PANEL_MODE_STATE = PANEL_MODE_ONE_PANE_CENTRAL_SERVICE;
    }

    private void setOnePaneCentralMode()
    {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guidelineLeft.getLayoutParams();
        params.guidePercent = 0.0f;
        guidelineLeft.setLayoutParams(params);

        ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) guidelineRight.getLayoutParams();
        params2.guidePercent = 1.0f;
        guidelineRight.setLayoutParams(params2);

        FrameLayout leftLayout = (FrameLayout) findViewById(containerLeftId);
        leftLayout.setVisibility(View.GONE);

        FrameLayout centralLayout = (FrameLayout) findViewById(containercentralId);
        centralLayout.setVisibility(View.VISIBLE);

        FrameLayout rightLayout = (FrameLayout) findViewById(containerRightId);
        rightLayout.setVisibility(View.GONE);

        PANEL_MODE_STATE = PANEL_MODE_ONE_PANE_CENTRAL;
    }

    private void setOnePaneRightMode()
    {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guidelineLeft.getLayoutParams();
        params.guidePercent = 0.0f;
        guidelineLeft.setLayoutParams(params);

        ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) guidelineRight.getLayoutParams();
        params2.guidePercent = 0.0f;
        guidelineRight.setLayoutParams(params2);

        FrameLayout leftLayout = (FrameLayout) findViewById(containerLeftId);
        leftLayout.setVisibility(View.GONE);

        FrameLayout centralLayout = (FrameLayout) findViewById(containercentralId);
        centralLayout.setVisibility(View.GONE);

        FrameLayout rightLayout = (FrameLayout) findViewById(containerRightId);
        rightLayout.setVisibility(View.VISIBLE);

        PANEL_MODE_STATE = PANEL_MODE_ONE_PANE_RIGHT;
    }

    private boolean getLandscapeOrientation()
    {
        boolean is600dp = getResources().getBoolean(R.bool.isTablet);

        return ((getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) && is600dp);
    }

    private void setTwoPaneLeftMode()
    {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guidelineLeft.getLayoutParams();
        params.guidePercent = 0.3f;
        guidelineLeft.setLayoutParams(params);

        ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) guidelineRight.getLayoutParams();
        params2.guidePercent = 1.0f;
        guidelineRight.setLayoutParams(params2);

        FrameLayout leftLayout = (FrameLayout) findViewById(containerLeftId);
        leftLayout.setVisibility(View.VISIBLE);

        FrameLayout centralLayout = (FrameLayout) findViewById(containercentralId);
        centralLayout.setVisibility(View.VISIBLE);

        FrameLayout rightLayout = (FrameLayout) findViewById(containerRightId);
        rightLayout.setVisibility(View.GONE);

        PANEL_MODE_STATE = PANEL_MODE_TWO_PANE_LEFT;
    }

    private void setTwoPaneRightMode()
    {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guidelineLeft.getLayoutParams();
        params.guidePercent = 0.0f;
        guidelineLeft.setLayoutParams(params);

        ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) guidelineRight.getLayoutParams();
        params2.guidePercent = 0.3f;
        guidelineRight.setLayoutParams(params2);

        FrameLayout leftLayout = (FrameLayout) findViewById(containerLeftId);
        leftLayout.setVisibility(View.GONE);

        FrameLayout centralLayout = (FrameLayout) findViewById(containercentralId);
        centralLayout.setVisibility(View.VISIBLE);

        FrameLayout rightLayout = (FrameLayout) findViewById(containerRightId);
        rightLayout.setVisibility(View.VISIBLE);

        PANEL_MODE_STATE = PANEL_MODE_TWO_PANE_RIGHT;
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

        //Clear view
        FrameLayout fl = (FrameLayout) findViewById(containercentralId);
        fl.removeAllViews();

        //remove login dragment
        Fragment fragment = getSupportFragmentManager().findFragmentById(containercentralId);
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
        if(!getLandscapeOrientation()) {
            setOnePaneCentralMode();
        }

        placeStoriesListToFragment(slideInRightSlideOutLeft);
    }

    private void placeStoriesListToFragment(boolean slideInRightSlideOutLeft)
    {
        removeBottomPadding();

        //Clear view
        FrameLayout fl = (FrameLayout) findViewById(containercentralId);
        fl.removeAllViews();

        // Create fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(slideInRightSlideOutLeft)
            fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
        else
            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        FeedStoriesFragment feedStoriesFragment = new FeedStoriesFragment();
        fragmentTransaction.replace(containercentralId, feedStoriesFragment);
        fragmentTransaction.addToBackStack("feedStoriesFragment");
        fragmentTransaction.commit();
    }
    public void onNewFeedAddAction()
    {
        SERVICE_PANEL_MODE_STATE_PREVIOUS = PANEL_MODE_STATE;
        setOnePaneCentralMode();

        addBottomPadding(containercentralId);

        //Clear view
        FrameLayout fl = (FrameLayout) findViewById(containercentralId);
        fl.removeAllViews();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Bundle data = new Bundle();
        //mode = 0: add new feed
        data.putInt("mode", 0);

        NewFeedFragment newFeedFragment = new NewFeedFragment();
        newFeedFragment.setArguments(data);
        fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
        fragmentTransaction.replace(containercentralId, newFeedFragment);
        fragmentTransaction.addToBackStack("newFeedFragment");
        fragmentTransaction.commit();

    }

    public void onFeedEdit()
    {
        SERVICE_PANEL_MODE_STATE_PREVIOUS = PANEL_MODE_STATE;
        setOnePaneCentralMode();

        addBottomPadding(containercentralId);

        //Clear view
        FrameLayout fl = (FrameLayout) findViewById(containercentralId);
        fl.removeAllViews();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Bundle data = new Bundle();
        //mode = 1: edit a feed
        data.putInt("mode", 1);

        NewFeedFragment newFeedFragment = new NewFeedFragment();
        newFeedFragment.setArguments(data);
        fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
        fragmentTransaction.replace(containercentralId, newFeedFragment);
        fragmentTransaction.addToBackStack("newFeedFragment");
        fragmentTransaction.commit();
    }

    public void onStorySelected(Story story)
    {
        loadStoryFragment();
    }

    public void loadStoryFragment()
    {
        if(getLandscapeOrientation()) {
            setTwoPaneRightMode();
        }
        else {
            setOnePaneRightMode();
        }

        ShowToolbar();

        removeBottomPadding();

        //Clear view
        FrameLayout fl = (FrameLayout) findViewById(containerRightId);
        fl.removeAllViews();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
        StoryViewerFragment storyViewerFragment = new StoryViewerFragment();
        fragmentTransaction.replace(containerRightId, storyViewerFragment);
        fragmentTransaction.addToBackStack("storyViewerFragment");
        fragmentTransaction.commit();
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
        setOnePaneCentralServiceMode();

        removeBottomPadding();

        LoginFragment loginFragment;
        loginFragment = new LoginFragment();

        //Clear view
        FrameLayout fl = (FrameLayout) findViewById(containercentralId);
        fl.removeAllViews();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fragmentTransaction.replace(containercentralId, loginFragment);
        //fragmentTransaction.addToBackStack("loginFragment);
        fragmentTransaction.commit();
    }

    public void LoadFeedsListFragment()
    {
        if(getLandscapeOrientation()) {
            setTwoPaneLeftMode();
        }
        else {
            setOnePaneLeftMode();
        }

        addBottomPadding(containerLeftId);

        //Clear view
        FrameLayout fl = (FrameLayout) findViewById(containerLeftId);
        fl.removeAllViews();

        FeedsListFragment feedsListFragment = new FeedsListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
        fragmentTransaction.replace(containerLeftId, feedsListFragment);
        fragmentTransaction.addToBackStack("feedsListFragment");
        fragmentTransaction.commit();
    }

    public void ShowSettingsFragment()
    {
        SERVICE_PANEL_MODE_STATE_PREVIOUS = PANEL_MODE_STATE;
        setOnePaneCentralServiceMode();

        removeBottomPadding();

        //Clear view
        FrameLayout fl = (FrameLayout) findViewById(containercentralId);
        fl.removeAllViews();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
        SettingsFragment settingsFragment = new SettingsFragment();
        fragmentTransaction.replace(containercentralId, settingsFragment);
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

            //First check if we get back from service windows
            Fragment fragment = getSupportFragmentManager().findFragmentById(containercentralId);
            if (fragment instanceof SettingsFragment){
                switch (SERVICE_PANEL_MODE_STATE_PREVIOUS) {
                    case PANEL_MODE_ONE_PANE_LEFT:
                        setOnePaneLeftMode();
                        break;
                    case PANEL_MODE_ONE_PANE_CENTRAL:
                        setOnePaneCentralMode();
                        break;
                    case PANEL_MODE_ONE_PANE_RIGHT:
                        setOnePaneRightMode();
                        break;
                    case PANEL_MODE_TWO_PANE_LEFT:
                        setTwoPaneLeftMode();
                        break;
                    case PANEL_MODE_TWO_PANE_RIGHT:
                        setTwoPaneRightMode();
                        break;
                    default:
                        super.onBackPressed();
                        ShowToolbar();
                        break;
                }
                //Return back content for the central fragment
                placeStoriesListToFragment(true);
                return;
            } else {

                switch (PANEL_MODE_STATE) {
                    case PANEL_MODE_ONE_PANE_LEFT:
                        finish();
                        break;
                    case PANEL_MODE_ONE_PANE_CENTRAL:
                        setOnePaneLeftMode();
                        break;
                    case PANEL_MODE_ONE_PANE_RIGHT:
                        setOnePaneCentralMode();
                        break;
                    case PANEL_MODE_TWO_PANE_LEFT:
                        finish();
                        break;
                    case PANEL_MODE_TWO_PANE_RIGHT:
                        setTwoPaneLeftMode();
                        break;
                    default:
                        super.onBackPressed();
                        ShowToolbar();
                        break;
                }
            }

           /* Fragment fragment = getSupportFragmentManager().findFragmentById(containercentralId);
            if (fragment instanceof FeedsListFragment) {
                super.onBackPressed();
                ShowToolbar();
                //Add bottom padding if we returned back to the feeds list fragment
                addBottomPadding();*/
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

    //Padding for a floating buttons
    private void addBottomPadding(int containerId)
    {
        int navHeight = getNavHeight();
        if (navHeight > 0) {
            (findViewById(containerId)).setPadding(0, 0, 0, navHeight);
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
